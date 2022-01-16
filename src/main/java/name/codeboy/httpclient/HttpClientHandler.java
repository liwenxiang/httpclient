package name.codeboy.httpclient;

import com.google.common.util.concurrent.RateLimiter;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.ReferenceCountUtil;
import name.codeboy.httpclient.message.RequestMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Sharable
public class HttpClientHandler extends ChannelDuplexHandler {
    private final static Logger logger = LoggerFactory.getLogger(HttpClientHandler.class);

    private final RateLimiter rateLimiter;

    private final BlockingQueue<RequestMessage> msgQueue;
    private final ChannalCloseListener closeChannelListener;

    public HttpClientHandler(BlockingQueue<RequestMessage> queue, ChannalCloseListener listener, RateLimiter rateLimiter) {
        msgQueue = queue;
        this.closeChannelListener = listener;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("conn complete");
        writeMessage(ctx);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        writeMessage(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.debug("socket an read {}", msg.getClass().getName());
        if (msg instanceof HttpResponse) {
            HttpResponse res = (HttpResponse) msg;
            if (res.headers().contains(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE, true)) {
                logger.info("get end socket content");
                ctx.close();
                closeChannelListener.notifyFailed();
            }
        } else {
            writeMessage(ctx);
        }
        ReferenceCountUtil.release(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("exception {}, close ctx", cause);
        cause.printStackTrace();
        ctx.close();
        closeChannelListener.notifyFailed();
    }

    private void writeMessage(ChannelHandlerContext ctx) throws InterruptedException {
        logger.debug("begin to write msg");
        if (!ctx.channel().isWritable()) {
            return;
        }
        if (msgQueue.isEmpty()) {
            return;
        }

        rateLimiter.acquire();

        final RequestMessage msg = msgQueue.poll(0, TimeUnit.SECONDS);
        if (msg == null) {
            logger.debug("get empty msg");
            return;
        }

        Object request = msg.convertRequest();

        logger.trace("write msg {}", request);
        ChannelFuture writeFuture = ctx.writeAndFlush(request);
        writeFuture.addListener(f -> {
            if (writeFuture.isDone() && writeFuture.isSuccess()) {
                logger.debug("Send success");
            } else {
                logger.error("Send failed: " + writeFuture.cause());
                ctx.channel().eventLoop().execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            msgQueue.put(msg);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }
}

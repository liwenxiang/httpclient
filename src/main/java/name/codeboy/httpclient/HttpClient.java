package name.codeboy.httpclient;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import name.codeboy.httpclient.message.RequestMessage;

public class HttpClient implements ChannalCloseListener {
	private final static Logger logger = LoggerFactory.getLogger(HttpClient.class);
	private final BlockingQueue<RequestMessage> msgQueue = new LinkedBlockingQueue<RequestMessage>();
	private final HttpClientHandler sendHandler;
	private final Bootstrap b = new Bootstrap(); 
	private final EventLoopGroup  group;

	private final String remoteHost;
	private final int remotePort;
	private final int maxUseLocalPortNum;

	public HttpClient(String remoteHost, int remotePort, int maxUseLocalPortNum, int threadNum) {
		this.remoteHost = remoteHost;
		this.remotePort = remotePort;
		this.maxUseLocalPortNum = maxUseLocalPortNum;
		sendHandler = new HttpClientHandler(msgQueue, this);
		group = new NioEventLoopGroup(threadNum);
		//group = new EpollEventLoopGroup(threadNum);
	}
	
	public void stop() {
		group.shutdownGracefully();
	}

	public void init() {
		b.group(group);
		b.channel(NioSocketChannel.class);
		//b.channel(EpollSocketChannel.class);
		b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
		b.option(ChannelOption.SO_KEEPALIVE, true);
		b.option(ChannelOption.TCP_NODELAY, true);
		b.option(ChannelOption.SO_REUSEADDR, true);
		b.option(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 32 * 1024);
		b.option(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, 8 * 1024);

        b.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
            	ChannelPipeline p = ch.pipeline();
                //p.addLast("log", new LoggingHandler(LogLevel.INFO));
            	p.addLast("codec", new HttpClientCodec());
            	p.addLast("inflater", new HttpContentDecompressor());
                p.addLast(sendHandler);
                
            }
        });
        
        logger.info("begin to connect {} {}", remoteHost, remotePort);

		for (int i = 0; i < maxUseLocalPortNum; i++) {
			AddNewChannel();
			logger.info("first connect use port num {}", i);
		}
        logger.info("connect end");
	}
	
	public void sendMessage(RequestMessage msg) {
		this.msgQueue.add(msg);
	}
	
	public long todoMsg() {
		return this.msgQueue.size();
	}
	protected void AddNewChannel() {
    	ChannelFuture conFuture = b.connect(this.remoteHost, this.remotePort);
    	logger.info("re conn socket {} {}", this.remoteHost, this.remotePort);
    	conFuture.addListener(f -> {
    		logger.info("re con end");
    		if (!conFuture.isSuccess()) {
    			logger.error("re con error" + conFuture.cause());
    		}
    	});
	}

	@Override
	public void notifyFailed() {
		AddNewChannel();
	}
	
	public boolean isSendEnd() {
		return msgQueue.isEmpty();
	}
}

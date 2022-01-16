package name.codeboy.httpclient.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class HttpRequestMessage implements RequestMessage {

    private HttpRequest req = null;

    public HttpRequestMessage(String content, GlobalConfig globalConfig) throws MalformedURLException {
        req = build(content, globalConfig);
    }

    /**
     * 格式
     * uri
     * GET\turi
     * GET\turi\tk1:v1\tk2:v2
     * GET\turi\tk1:v1\tk2:v2\tpostbody:xxx
     */
    public HttpRequest build(String content, GlobalConfig globalConfig) throws MalformedURLException {

        String method = "GET";
        String path = content;
        ByteBuf body = Unpooled.buffer(0);


        String[] all = content.split("\t");
        if (all.length > 1) {
            method = all[0];
            path = all[1];
        }

        Map<String, String> headers = null;
        if (all.length >= 3) {
            headers = new HashMap<>(all.length - 2);
            for (int i = 2; i < all.length; i++) {
                String[] arr = all[i].split(":", 1);
                String key = arr[0];
                if (key.equals("postbody")) {
                    body = Unpooled.wrappedBuffer("".getBytes()).retain();
                    continue;
                }
                String value = arr[1];
                headers.put(key, value);
            }
        }

        String uri = String.format("%s://%s%s", globalConfig.schema, globalConfig.host, path);
        HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, new HttpMethod(method), uri, body);
        if (globalConfig.isKeepAlive()) {
            HttpHeaders.setHeader(request, HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        }
        if (globalConfig.isReqJsonFormat()) {
            HttpHeaders.setHeader(request, HttpHeaders.Names.CONTENT_TYPE, "application/json");
        }

        HttpHeaders.setHeader(request, HttpHeaders.Names.HOST, globalConfig.host);

        addHeader(request, globalConfig.getDefaultHeaders());
        addHeader(request, headers);

        return request;
    }

    private void addHeader(HttpRequest request, Map<String, String> headers) {
        if (headers == null) {
            return;
        }
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            HttpHeaders.setHeader(request, entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Object convertRequest() {
        return req;
    }

}

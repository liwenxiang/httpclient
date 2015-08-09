package name.codeboy.httpclient.message;

import java.net.MalformedURLException;
import java.net.URL;

public class HttpRequestMessage implements RequestMessage {
	
	private final URL url;
	private final String userAgent;
	
	public HttpRequestMessage(String url) throws MalformedURLException {
		this(url, "abc");
	}

	public HttpRequestMessage(String url, String userAgent) throws MalformedURLException {
		this.url = new URL(url);
		this.userAgent = userAgent;
	}

	@Override
	public String getUri() {
		return url.getFile();
	}

	@Override
	public String getHost() {
		return url.getHost();
	}

	@Override
	public String getUserAgent() {
		return userAgent;
	}

}

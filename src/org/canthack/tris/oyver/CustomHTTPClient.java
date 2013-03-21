package org.canthack.tris.oyver;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRoute;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
/**
 * Provides a single, shared, thread-safe HTTPClient.
 */
public final class CustomHTTPClient {
	private static HttpClient customHttpClient;

	/** A private Constructor prevents any other class from instantiating. */
	private CustomHTTPClient() {
	}

	/**
	 * Get a singleton, thread-safe HttpClient for making HTTP requests.
	 */
	public static synchronized HttpClient getHttpClient() {
		if (customHttpClient == null) {
			final HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
			HttpProtocolParams.setUseExpectContinue(params, false);

			ConnManagerParams.setTimeout(params, 100);

			HttpConnectionParams.setConnectionTimeout(params, 10000);
			HttpConnectionParams.setSoTimeout(params, 10000);

			final SchemeRegistry schReg = new SchemeRegistry();
			schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));


			ConnManagerParams.setMaxConnectionsPerRoute(params, new ConnPerRoute() {
				@Override
				public int getMaxForRoute(final HttpRoute httproute)
				{
					return 20;
				}
			});

			final ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params,schReg);

			customHttpClient = new DefaultHttpClient(conMgr, params);
		}
		return customHttpClient;
	}

	/**
	 * Retrieve the input stream from the specified Url. Can return null if
	 * the URL cannot be found or times out.
	 */
	public static synchronized InputStream retrieveStream(final String url) {
		if(url == null || url.length() <= 4){
			return null;
		}

		final HttpGet getRequest = new HttpGet(url);

		try {
			final HttpResponse response = CustomHTTPClient.getHttpClient().execute(getRequest);
			final int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode != HttpStatus.SC_OK) { 
				return null;
			}

			final HttpEntity getResponseEntity = response.getEntity();

			final InputStream s = getResponseEntity.getContent();

			return s;
		} 
		catch (IOException e) {
			getRequest.abort();
		}

		return null;
	}
}
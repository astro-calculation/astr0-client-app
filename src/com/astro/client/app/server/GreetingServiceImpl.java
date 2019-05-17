package com.astro.client.app.server;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.astro.client.app.client.GreetingService;
import com.astro.client.app.shared.FieldVerifier;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server-side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class GreetingServiceImpl extends RemoteServiceServlet implements GreetingService {

    public String greetServer(String input) throws IllegalArgumentException {
	// Verify that the input is valid.
	if (!FieldVerifier.isValidName(input)) {
	    // If the input is not valid, throw an IllegalArgumentException back to
	    // the client.
	    throw new IllegalArgumentException("Name must be at least 4 characters long");
	}

	HttpClient client = HttpClientBuilder.create().build();
	HttpGet request = new HttpGet("http://localhost:8080/astro-calculation-service/getPanchangamForCurrentDay");
	HttpResponse response = null;
	String responseJson = null;
	try {
	    response = client.execute(request);
	    System.out.println(response.toString());
	    responseJson = EntityUtils.toString(response.getEntity());
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	String serverInfo = getServletContext().getServerInfo();
	String userAgent = getThreadLocalRequest().getHeader("User-Agent");

	// Escape data from the client to avoid cross-site script vulnerabilities.
	input = escapeHtml(input);
	userAgent = escapeHtml(userAgent);

	return responseJson;
    }

    /**
     * Escape an html string. Escaping data received from the client helps to
     * prevent cross-site script vulnerabilities.
     * 
     * @param html the html string to escape
     * @return the escaped string
     */
    private String escapeHtml(String html) {
	if (html == null) {
	    return null;
	}
	return html.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
    }
}

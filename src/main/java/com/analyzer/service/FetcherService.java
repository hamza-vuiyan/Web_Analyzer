package com.analyzer.service;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;

/** Service for fetching web pages using HTTP/HTTPS. */
@Service
public class FetcherService {

    private final CloseableHttpClient httpClient;
    
    @Value("${http.client.timeout}")
    private int httpTimeout;
    
    @Value("${http.client.user-agent}")
    private String httpUserAgent;
    
    @Value("${http.client.accept}")
    private String httpAccept;
    
    @Value("${http.client.accept-language}")
    private String httpAcceptLanguage;
    
    @Value("${http.client.accept-encoding}")
    private String httpAcceptEncoding;
    
    @Value("${http.client.connection}")
    private String httpConnection;

    public FetcherService() {
        this.httpClient = createHttpClient();
    }

    private CloseableHttpClient createHttpClient() {
        try {
            // WARNING: In production, use proper certificate validation.
            SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial((chain, authType) -> true)
                    .build();

            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                sslContext,
                NoopHostnameVerifier.INSTANCE
            );

            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectionRequestTimeout(Timeout.ofMilliseconds(httpTimeout))
                    .setResponseTimeout(Timeout.ofMilliseconds(httpTimeout))
                    .build();

            return HttpClients.custom()
                    .setConnectionManager(
                            org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder.create()
                        .setSSLSocketFactory(sslSocketFactory)
                        .setMaxConnTotal(200)
                        .setMaxConnPerRoute(20)
                                    .build()
                    )
                    .setDefaultRequestConfig(requestConfig)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create HTTP client", e);
        }
    }


    public String normalizeUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return "";
        }
        url = url.trim();
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return "https://" + url;
        }
        return url;
    }

    
    @SuppressWarnings("deprecation")
    public FetchResult fetchPage(String url) {
        try {
            long startTime = System.nanoTime();
            HttpGet request = createRequest(url);
            
            CloseableHttpResponse response = httpClient.execute(request);
            double elapsedMs = (System.nanoTime() - startTime) / 1_000_000.0;
            int statusCode = response.getCode();
            
            if (statusCode >= 200 && statusCode < 400) {
                String htmlContent = null;
                try {
                    htmlContent = EntityUtils.toString(response.getEntity());
                } catch (Exception e) {
                }
                return new FetchResult(response, elapsedMs, htmlContent, null);
            } else {
                try { response.close(); } catch (Exception ignored) {}
                return new FetchResult(null, null, null, "HTTP Status: " + statusCode);
            }
        } catch (Exception e) {
            if (url.toLowerCase().startsWith("https://")) {
                try {
                    String httpUrl = "http://" + url.substring(8);
                    long startTime = System.nanoTime();
                    HttpGet request = createRequest(httpUrl);
                    
                    CloseableHttpResponse response = httpClient.execute(request);
                    double elapsedMs = (System.nanoTime() - startTime) / 1_000_000.0;
                    int statusCode = response.getCode();
                    
                    if (statusCode >= 200 && statusCode < 400) {
                        String htmlContent = null;
                        try {
                            htmlContent = EntityUtils.toString(response.getEntity());
                        } catch (Exception e3) {
                        }
                        return new FetchResult(response, elapsedMs, htmlContent, null);
                    } else {
                        try { response.close(); } catch (Exception ignored) {}
                        return new FetchResult(null, null, null, "Both HTTPS and HTTP failed");
                    }
                } catch (Exception e2) {
                    return new FetchResult(null, null, null, "Both HTTPS and HTTP failed");
                }
            }
            
            return new FetchResult(null, null, null,
                    e.getMessage() != null ? e.getMessage().substring(0, Math.min(100, e.getMessage().length())) : "Connection failed");
        }
    }

    private HttpGet createRequest(String url) {
        HttpGet request = new HttpGet(url);
        request.setHeader(HttpHeaders.ACCEPT, httpAccept);
        request.setHeader(HttpHeaders.ACCEPT_LANGUAGE, httpAcceptLanguage);
        request.setHeader(HttpHeaders.ACCEPT_ENCODING, httpAcceptEncoding);
        request.setHeader(HttpHeaders.CONNECTION, httpConnection);
        request.setHeader(HttpHeaders.USER_AGENT, httpUserAgent);
        return request;
    }

    public static class FetchResult {
        public final CloseableHttpResponse response;
        public final Double elapsedMs;
        public final String htmlContent;
        public final String error;

        public FetchResult(CloseableHttpResponse response, Double elapsedMs, String htmlContent, String error) {
            this.response = response;
            this.elapsedMs = elapsedMs;
            this.htmlContent = htmlContent;
            this.error = error;
        }
    }
}

package com.analyzer.service;

import com.analyzer.config.AnalyzerConfig;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import javax.net.ssl.SSLContext;

/**
 * Service for fetching web pages using HTTP/HTTPS.
 * Handles connection setup, timeouts, SSL, and error recovery.
 */
@Service
public class FetcherService {

    private static final Logger logger = LoggerFactory.getLogger(FetcherService.class);
    private final CloseableHttpClient httpClient;

    public FetcherService() {
        // Initialize HTTP client when service starts
        this.httpClient = createHttpClient();
    }

    /**
     * Create and configure HTTP client with:
     * - SSL support (accepts all certificates for testing)
     * - Connection pooling for better performance
     * - Timeout configuration
     */
    private CloseableHttpClient createHttpClient() {
        try {
            // Create SSL context that accepts all certificates
            // WARNING: In production, use proper certificate validation!
            SSLContext sslContext = SSLContextBuilder.create()
                    .loadTrustMaterial((chain, authType) -> true)  // Accept all certs
                    .build();

            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                    sslContext,
                    NoopHostnameVerifier.INSTANCE  // Skip hostname verification
            );

            // Configure timeouts
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectionRequestTimeout(Timeout.ofMilliseconds(AnalyzerConfig.HTTP_TIMEOUT))
                    .setResponseTimeout(Timeout.ofMilliseconds(AnalyzerConfig.HTTP_TIMEOUT))
                    .build();

            // Build client with connection pooling and SSL
            return HttpClients.custom()
                    .setConnectionManager(
                            org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder.create()
                                    .setSSLSocketFactory(sslSocketFactory)
                                    .setMaxConnTotal(200)           // Max total connections
                                    .setMaxConnPerRoute(20)         // Max connections per route
                                    .build()
                    )
                    .setDefaultRequestConfig(requestConfig)
                    .build();
        } catch (Exception e) {
            logger.error("Failed to create HTTP client", e);
            throw new RuntimeException("Failed to create HTTP client", e);
        }
    }

    /**
     * Normalize URL by adding https:// if no protocol is specified.
     * Examples:
     *   "google.com" -> "https://google.com"
     *   "http://example.com" -> "http://example.com" (unchanged)
     */
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

    /**
     * Fetch a webpage and measure response time.
     * Also extracts HTML content for link analysis.
     * 
     * Strategy:
     * 1. Try HTTPS first
     * 2. If HTTPS fails, try HTTP as fallback
     * 
     * @param url The URL to fetch
     * @return FetchResult with response, time, HTML content, or error message
     */
    @SuppressWarnings("deprecation")  // Using older execute method for compatibility
    public FetchResult fetchPage(String url) {
        // Try HTTPS first
        try {
            long startTime = System.nanoTime();
            HttpGet request = createRequest(url);
            
            CloseableHttpResponse response = httpClient.execute(request);
            double elapsedMs = (System.nanoTime() - startTime) / 1_000_000.0;
            int statusCode = response.getCode();
            
            if (statusCode >= 200 && statusCode < 400) {
                // Extract HTML content from response body
                String htmlContent = null;
                try {
                    htmlContent = EntityUtils.toString(response.getEntity());
                    logger.info("✓ {} | {} | {}ms | {} bytes", url, statusCode, 
                            String.format("%.0f", elapsedMs), htmlContent.length());
                } catch (Exception e) {
                    logger.warn("Could not extract HTML content: {}", e.getMessage());
                }
                return new FetchResult(response, elapsedMs, htmlContent, null);
            } else {
                logger.warn("✗ {} | Status: {}", url, statusCode);
                try { response.close(); } catch (Exception ignored) {}
                return new FetchResult(null, null, null, "HTTP Status: " + statusCode);
            }
        } catch (Exception e) {
            logger.warn("✗ {} (HTTPS) | {}: {}", url, e.getClass().getSimpleName(), 
                    e.getMessage() != null ? e.getMessage().substring(0, Math.min(100, e.getMessage().length())) : "");
            
            // Fallback: try HTTP if URL started with HTTPS
            if (url.toLowerCase().startsWith("https://")) {
                try {
                    String httpUrl = "http://" + url.substring(8);  // Replace https:// with http://
                    long startTime = System.nanoTime();
                    HttpGet request = createRequest(httpUrl);
                    
                    CloseableHttpResponse response = httpClient.execute(request);
                    double elapsedMs = (System.nanoTime() - startTime) / 1_000_000.0;
                    int statusCode = response.getCode();
                    
                    if (statusCode >= 200 && statusCode < 400) {
                        String htmlContent = null;
                        try {
                            htmlContent = EntityUtils.toString(response.getEntity());
                            logger.info("✓ {} (fallback) | {} | {}ms | {} bytes", httpUrl, statusCode, 
                                    String.format("%.0f", elapsedMs), htmlContent.length());
                        } catch (Exception e3) {
                            logger.warn("Could not extract HTML content: {}", e3.getMessage());
                        }
                        return new FetchResult(response, elapsedMs, htmlContent, null);
                    } else {
                        logger.warn("✗ {} (HTTP fallback) | Status: {}", httpUrl, statusCode);
                        try { response.close(); } catch (Exception ignored) {}
                        return new FetchResult(null, null, null, "Both HTTPS and HTTP failed");
                    }
                } catch (Exception e2) {
                    logger.warn("✗ HTTP fallback | {}: {}", e2.getClass().getSimpleName(),
                            e2.getMessage() != null ? e2.getMessage().substring(0, Math.min(100, e2.getMessage().length())) : "");
                    return new FetchResult(null, null, null, "Both HTTPS and HTTP failed");
                }
            }
            
            return new FetchResult(null, null, null,
                    e.getMessage() != null ? e.getMessage().substring(0, Math.min(100, e.getMessage().length())) : "Connection failed");
        }
    }

    /**
     * Create HTTP GET request with standard headers.
     */
    private HttpGet createRequest(String url) {
        HttpGet request = new HttpGet(url);
        // Add all configured headers
        AnalyzerConfig.HTTP_HEADERS.forEach((key, value) -> {
            if (!key.equals(HttpHeaders.USER_AGENT)) {
                request.setHeader(key, value);
            }
        });
        request.setHeader(HttpHeaders.USER_AGENT, AnalyzerConfig.HTTP_USER_AGENT);
        return request;
    }

    /**
     * Container for fetch results.
     * Holds either successful response + timing + HTML, or error message.
     */
    public static class FetchResult {
        public final CloseableHttpResponse response;  // HTTP response (null if failed)
        public final Double elapsedMs;                // Time taken in milliseconds (null if failed)
        public final String htmlContent;              // HTML content of the page (null if failed)
        public final String error;                    // Error message (null if successful)

        public FetchResult(CloseableHttpResponse response, Double elapsedMs, String htmlContent, String error) {
            this.response = response;
            this.elapsedMs = elapsedMs;
            this.htmlContent = htmlContent;
            this.error = error;
        }
    }
}

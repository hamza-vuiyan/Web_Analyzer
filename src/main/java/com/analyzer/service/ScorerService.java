package com.analyzer.service;

import com.analyzer.config.AnalyzerConfig;
import com.analyzer.model.PerformanceDetails;
import com.analyzer.model.ReliabilityDetails;
import com.analyzer.model.SecurityDetails;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for scoring website performance, security, and reliability.
 * Each scoring method examines different aspects and returns a 0-100 score.
 */
@Service
public class ScorerService {

    private static final Logger logger = LoggerFactory.getLogger(ScorerService.class);

    /**
     * Get backend server software and protocol information.
     * Examples: "Apache/2.4.41", "nginx/1.18.0"
     */
    public BackendProtocol getBackendAndProtocol(CloseableHttpResponse response, String url) {
        if (response == null) {
            return new BackendProtocol("Unknown", "Unknown");
        }

        String backend = getBackendInfo(response);
        String protocol = getProtocolInfo(response, url);
        
        return new BackendProtocol(backend, protocol);
    }

    /**
     * Extract backend/server info from response headers.
     */
    private String getBackendInfo(CloseableHttpResponse response) {
        Header serverHeader = response.getFirstHeader("Server");
        Header poweredHeader = response.getFirstHeader("X-Powered-By");
        
        List<String> parts = Arrays.asList(
            serverHeader != null ? serverHeader.getValue() : null,
            poweredHeader != null ? poweredHeader.getValue() : null
        ).stream().filter(s -> s != null && !s.isEmpty()).collect(Collectors.toList());
        
        return parts.isEmpty() ? "Unknown" : String.join(", ", parts);
    }

    /**
     * Get protocol information (HTTP version and encryption).
     */
    private String getProtocolInfo(CloseableHttpResponse response, String url) {
        String httpVersion = "HTTP/1.1"; // Default
        try {
            if (response.getVersion() != null) {
                httpVersion = response.getVersion().format();
            }
        } catch (Exception e) {
            logger.debug("Could not determine HTTP version", e);
        }

        boolean isHttps = url.toLowerCase().startsWith("https://");
        return isHttps ? httpVersion + " over TLS" : httpVersion;
    }

    /**
     * Score performance based on:
     * - Latency (response time)
     * - Compression (gzip, brotli)
     * - Caching headers
     * - Content size
     * - Broken links
     * 
     * @return Score (0-100) and detailed metrics
     */
    public ScoringResult<PerformanceDetails> scorePerformance(Double elapsedMs, CloseableHttpResponse response, String htmlContent, String baseUrl) {
        if (elapsedMs == null) {
            return new ScoringResult<>(0, new PerformanceDetails(
                0, 0, "N/A", "", 0, 0, 0, 0
            ));
        }

        int score = 0;
        int latencyScore = 30; // Default (worst)

        // Latency scoring: faster = better
        for (Map.Entry<Integer, Integer> entry : AnalyzerConfig.PERFORMANCE_THRESHOLDS.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toList())) {
            if (elapsedMs <= entry.getKey()) {
                latencyScore = entry.getValue();
                score = entry.getValue();
                break;
            }
        }

        String compression = "none";
        String cacheControl = "";
        int contentLengthKb = 0;
        int brokenLinks = 0;
        int totalLinks = 0;

        if (response == null) {
            int finalScore = Math.max(0, Math.min(100, score));
            return new ScoringResult<>(finalScore, new PerformanceDetails(
                elapsedMs, latencyScore, compression, cacheControl, contentLengthKb, brokenLinks, totalLinks, finalScore
            ));
        }

        // Check compression (gzip or brotli)
        Header encodingHeader = response.getFirstHeader("Content-Encoding");
        if (encodingHeader != null) {
            String encoding = encodingHeader.getValue().toLowerCase();
            if (encoding.contains("gzip") || encoding.contains("br")) {
                compression = encoding.contains("br") ? "br" : "gzip";
                score += 10;  // Bonus for compression
            } else {
                score -= 10;  // Penalty for no compression
            }
        } else {
            score -= 10;
        }

        // Check caching headers
        Header cacheHeader = response.getFirstHeader("Cache-Control");
        if (cacheHeader != null) {
            cacheControl = cacheHeader.getValue();
            if (cacheControl.contains("max-age")) {
                score += 10;  // Bonus for caching
            }
        }

        // Check content size (smaller is better)
        Header contentLengthHeader = response.getFirstHeader("Content-Length");
        if (contentLengthHeader != null) {
            try {
                long contentLength = Long.parseLong(contentLengthHeader.getValue());
                contentLengthKb = (int) (contentLength / 1024);
                
                // Give bonus points for smaller pages
                for (Map.Entry<Integer, Integer> entry : AnalyzerConfig.CONTENT_SIZE_LIMITS.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .collect(Collectors.toList())) {
                    if (contentLength <= entry.getKey()) {
                        score += entry.getValue();
                        break;
                    }
                }
            } catch (NumberFormatException e) {
                logger.debug("Could not parse Content-Length", e);
            }
        }

        // Check for broken links
        LinkCheckResult linkCheck = checkBrokenLinks(htmlContent, baseUrl);
        brokenLinks = linkCheck.brokenLinks;
        totalLinks = linkCheck.totalLinks;
        
        // Scoring for broken links: 0 broken = +10, 1-2 broken = +5, 3+ broken = 0
        if (brokenLinks == 0 && totalLinks > 0) {
            score += 10;
        } else if (brokenLinks <= 2 && totalLinks > 0) {
            score += 5;
        }

        int finalScore = Math.max(0, Math.min(100, score));
        return new ScoringResult<>(finalScore, new PerformanceDetails(
            elapsedMs, latencyScore, compression, cacheControl, contentLengthKb, brokenLinks, totalLinks, finalScore
        ));
    }

    /**
     * Check for broken links on the page using Jsoup.
     * 
     * Process:
     * 1. Parse HTML with Jsoup
     * 2. Find all <a> tags with href attributes
     * 3. Filter out anchors (#), javascript:, mailto:, tel:
     * 4. Check first 10 unique links (to avoid timeout)
     * 5. Send HEAD request to each link
     * 6. Count how many return 404 or error status
     */
    private LinkCheckResult checkBrokenLinks(String htmlContent, String baseUrl) {
        if (htmlContent == null || htmlContent.isEmpty()) {
            return new LinkCheckResult(0, 0);
        }

        try {
            // Parse HTML with Jsoup
            Document doc = Jsoup.parse(htmlContent, baseUrl);
            
            // Extract all links
            Elements links = doc.select("a[href]");
            Set<String> uniqueLinks = new HashSet<>();
            
            for (Element link : links) {
                String href = link.absUrl("href");
                
                // Filter out non-HTTP links and anchors
                if (href != null && !href.isEmpty() 
                        && (href.startsWith("http://") || href.startsWith("https://"))
                        && !href.contains("javascript:")
                        && !href.contains("mailto:")
                        && !href.contains("tel:")) {
                    uniqueLinks.add(href);
                }
                
                // Limit to 10 links to avoid long processing time
                if (uniqueLinks.size() >= 10) {
                    break;
                }
            }
            
            int brokenCount = 0;
            for (String linkUrl : uniqueLinks) {
                if (!isLinkValid(linkUrl)) {
                    brokenCount++;
                }
            }
            
            logger.debug("Link check: {} broken out of {} links", brokenCount, uniqueLinks.size());
            return new LinkCheckResult(brokenCount, uniqueLinks.size());
            
        } catch (Exception e) {
            logger.warn("Error checking broken links: {}", e.getMessage());
            return new LinkCheckResult(0, 0);
        }
    }

    /**
     * Check if a single link is valid (returns HTTP 200-399).
     * Uses HEAD request for efficiency.
     */
    private boolean isLinkValid(String linkUrl) {
        try {
            URL url = new URL(linkUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(3000);  // 3 second timeout
            connection.setReadTimeout(3000);
            connection.setInstanceFollowRedirects(true);
            
            int responseCode = connection.getResponseCode();
            connection.disconnect();
            
            // Consider 2xx and 3xx as valid
            return responseCode >= 200 && responseCode < 400;
        } catch (Exception e) {
            // Any exception (timeout, DNS error, etc.) = broken link
            return false;
        }
    }

    /**
     * Score security based on HTTPS and security headers.
     * Checks 6 important security practices.
     * 
     * @return Score (0-100) and detailed check results
     */
    public ScoringResult<SecurityDetails> scoreSecurity(String url, CloseableHttpResponse response) {
        if (response == null) {
            return new ScoringResult<>(0, new SecurityDetails(
                false, false, false, false, false, false, 0
            ));
        }

        int score = 0;
        List<String> detailsList = new java.util.ArrayList<>();

        // 1. HTTPS check (30 points)
        boolean https = url.toLowerCase().startsWith("https://");
        if (https) {
            score += 30;
            detailsList.add("HTTPS:✓");
        } else {
            detailsList.add("HTTPS:✗");
        }

        // 2. HSTS (HTTP Strict Transport Security) - forces HTTPS (20 points)
        boolean hsts = response.getFirstHeader("Strict-Transport-Security") != null;
        if (hsts) {
            score += 20;
            detailsList.add("HSTS:✓");
        } else {
            detailsList.add("HSTS:✗");
        }

        // 3. CSP (Content Security Policy) - prevents XSS attacks (20 points)
        boolean csp = response.getFirstHeader("Content-Security-Policy") != null;
        if (csp) {
            score += 20;
            detailsList.add("CSP:✓");
        } else {
            detailsList.add("CSP:✗");
        }

        // 4. X-Content-Type-Options - prevents MIME type sniffing (10 points)
        Header xContentTypeHeader = response.getFirstHeader("X-Content-Type-Options");
        boolean xContentTypeOptions = xContentTypeHeader != null && 
                xContentTypeHeader.getValue().toLowerCase().equals("nosniff");
        if (xContentTypeOptions) {
            score += 10;
            detailsList.add("X-Content-Type-Options:✓");
        } else {
            detailsList.add("X-Content-Type-Options:✗");
        }

        // 5. X-Frame-Options - prevents clickjacking (10 points)
        Header xfoHeader = response.getFirstHeader("X-Frame-Options");
        Header cspHeader = response.getFirstHeader("Content-Security-Policy");
        boolean xFrameOptions = xfoHeader != null || 
                (cspHeader != null && cspHeader.getValue().contains("frame-ancestors"));
        if (xFrameOptions) {
            score += 10;
            detailsList.add("X-Frame-Options:✓");
        } else {
            detailsList.add("X-Frame-Options:✗");
        }

        // 6. Referrer Policy - controls referrer information (10 points)
        boolean referrerPolicy = response.getFirstHeader("Referrer-Policy") != null;
        if (referrerPolicy) {
            score += 10;
            detailsList.add("Referrer-Policy:✓");
        } else {
            detailsList.add("Referrer-Policy:✗");
        }

        int finalScore = Math.max(0, Math.min(100, score));
        logger.info("  Security headers: {} = {}", String.join(" | ", detailsList), finalScore);

        return new ScoringResult<>(finalScore, new SecurityDetails(
            https, hsts, csp, xContentTypeOptions, xFrameOptions, referrerPolicy, finalScore
        ));
    }

    /**
     * Score reliability based on HTTP response.
     * Indicates how well the server responds.
     * 
     * @return Score (0-100) and detailed metrics
     */
    public ScoringResult<ReliabilityDetails> scoreReliability(CloseableHttpResponse response) {
        if (response == null) {
            return new ScoringResult<>(0, new ReliabilityDetails(
                0, 0, false, false, 0
            ));
        }

        int score = 50; // Baseline for successful responses
        int statusCode = response.getCode();

        // Status code check
        if (statusCode >= 200 && statusCode < 300) {
            score += 30;  // Success responses (200 OK, etc.)
        } else if (statusCode >= 300 && statusCode < 400) {
            score += 15;  // Redirects (301, 302, etc.)
        } else {
            score = 20;  // Errors (404, 500, etc.) - low reliability
        }

        // Response headers count (more headers = more complete response)
        int headersCount = response.getHeaders().length;
        if (headersCount > 20) {
            score += 10;
        } else if (headersCount > 10) {
            score += 5;
        }

        // Compression (gzip/brotli)
        Header encodingHeader = response.getFirstHeader("Content-Encoding");
        boolean gzipEnabled = encodingHeader != null && 
                (encodingHeader.getValue().toLowerCase().contains("gzip") || 
                 encodingHeader.getValue().toLowerCase().contains("br"));
        if (gzipEnabled) {
            score += 5;
        }

        // Caching (Cache-Control or ETag present)
        boolean cacheEnabled = response.getFirstHeader("Cache-Control") != null || 
                response.getFirstHeader("ETag") != null;
        if (cacheEnabled) {
            score += 10;
        }

        int finalScore = Math.max(0, Math.min(100, score));
        
        return new ScoringResult<>(finalScore, new ReliabilityDetails(
            statusCode, headersCount, gzipEnabled, cacheEnabled, finalScore
        ));
    }

    /**
     * Container for backend and protocol information.
     */
    public static class BackendProtocol {
        public final String backend;
        public final String protocol;

        public BackendProtocol(String backend, String protocol) {
            this.backend = backend;
            this.protocol = protocol;
        }
    }

    /**
     * Generic scoring result container.
     * Holds both the score (0-100) and detailed breakdown.
     */
    public static class ScoringResult<T> {
        public final int score;
        public final T details;

        public ScoringResult(int score, T details) {
            this.score = score;
            this.details = details;
        }
    }

    /**
     * Container for link check results.
     */
    private static class LinkCheckResult {
        public final int brokenLinks;
        public final int totalLinks;

        public LinkCheckResult(int brokenLinks, int totalLinks) {
            this.brokenLinks = brokenLinks;
            this.totalLinks = totalLinks;
        }
    }
}

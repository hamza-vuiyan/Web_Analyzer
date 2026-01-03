package com.analyzer.service;

import com.analyzer.model.PerformanceDetails;
import com.analyzer.model.SEODetails;
import com.analyzer.model.SecurityDetails;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for scoring website performance, security, and reliability.
 * Each scoring method examines different aspects and returns a 0-100 score.
 */
@Service
public class ScorerService {

    @Value("${analyzer.performance.threshold.200}")
    private int threshold200;
    
    @Value("${analyzer.performance.threshold.500}")
    private int threshold500;
    
    @Value("${analyzer.performance.threshold.1000}")
    private int threshold1000;
    
    @Value("${analyzer.performance.threshold.2000}")
    private int threshold2000;
    
    @Value("${analyzer.performance.default-score}")
    private int defaultScore;
    
    @Value("${analyzer.content-size.threshold.300000}")
    private int contentSizeBonus300k;
    
    @Value("${analyzer.content-size.threshold.1000000}")
    private int contentSizeBonus1m;
    
    /**
     * Get performance thresholds from properties as a sorted Map.
     */
    private Map<Integer, Integer> getPerformanceThresholds() {
        Map<Integer, Integer> thresholds = new HashMap<>();
        thresholds.put(200, threshold200);
        thresholds.put(500, threshold500);
        thresholds.put(1000, threshold1000);
        thresholds.put(2000, threshold2000);
        return thresholds;
    }
    
    /**
     * Get content size limits from properties as a sorted Map.
     */
    private Map<Integer, Integer> getContentSizeLimits() {
        Map<Integer, Integer> limits = new HashMap<>();
        limits.put(300_000, contentSizeBonus300k);
        limits.put(1_000_000, contentSizeBonus1m);
        return limits;
    }

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
            // Could not determine HTTP version
        }

        boolean isHttps = url.toLowerCase().startsWith("https://");
        return isHttps ? httpVersion + " over TLS" : httpVersion;
    }

    /**
     * Score performance with individual feature marks (0-100 each).
     * Features: Latency, Compression, Caching, Content Size
     * Final score = average of all feature marks
     * 
     * @return Score (0-100) and detailed metrics
     */
    public ScoringResult<PerformanceDetails> scorePerformance(Double elapsedMs, CloseableHttpResponse response, String htmlContent, String baseUrl) {
        if (elapsedMs == null) {
            return new ScoringResult<>(0, new PerformanceDetails(
                0, 0, "N/A", "", 0, 0, 0, 0
            ));
        }

        String compression = "none";
        String cacheControl = "";
        int contentLengthKb = 0;
        int latencyScore = 0;
        int compressionScore = 0;
        int cachingScore = 0;
        int contentSizeScore = 0;

        // LATENCY SCORE (0-100): Based on response time
        if (elapsedMs <= 200) {
            latencyScore = 100;  // Excellent: < 200ms
        } else if (elapsedMs <= 500) {
            latencyScore = 80;   // Good: < 500ms
        } else if (elapsedMs <= 1000) {
            latencyScore = 60;   // Fair: < 1s
        } else if (elapsedMs <= 2000) {
            latencyScore = 40;   // Poor: < 2s
        } else {
            latencyScore = 0;    // Very Poor: > 2s
        }

        if (response != null) {
            // COMPRESSION SCORE (0/50/100): gzip/brotli presence
            Header encodingHeader = response.getFirstHeader("Content-Encoding");
            if (encodingHeader != null) {
                String encoding = encodingHeader.getValue().toLowerCase();
                if (encoding.contains("br")) {
                    compressionScore = 100;  // Brotli (best)
                    compression = "br";
                } else if (encoding.contains("gzip")) {
                    compressionScore = 50;   // Gzip (good)
                    compression = "gzip";
                }
            }

            // CACHING SCORE (0/50/100): Cache-Control header
            Header cacheHeader = response.getFirstHeader("Cache-Control");
            if (cacheHeader != null) {
                cacheControl = cacheHeader.getValue();
                if (cacheControl.contains("public") || cacheControl.contains("immutable")) {
                    cachingScore = 100;  // Well configured
                } else if (cacheControl.contains("max-age")) {
                    cachingScore = 50;   // Basic caching
                }
            }

            // CONTENT SIZE SCORE (0/50/100): Page size efficiency
            Header contentLengthHeader = response.getFirstHeader("Content-Length");
            if (contentLengthHeader != null) {
                try {
                    long contentLength = Long.parseLong(contentLengthHeader.getValue());
                    contentLengthKb = (int) (contentLength / 1024);
                    
                    if (contentLength <= 300_000) {
                        contentSizeScore = 100;  // < 300 KB (excellent)
                    } else if (contentLength <= 1_000_000) {
                        contentSizeScore = 50;   // < 1 MB (acceptable)
                    }
                } catch (NumberFormatException e) {
                    // Could not parse Content-Length
                }
            }
        }

        // Final score = average of all 4 features
        int finalScore = (latencyScore + compressionScore + cachingScore + contentSizeScore) / 4;
        finalScore = Math.max(0, Math.min(100, finalScore));
        
        return new ScoringResult<>(finalScore, new PerformanceDetails(
            elapsedMs, latencyScore, compression, cacheControl, contentLengthKb, 0, 0, finalScore
        ));
    }

    /**
     * Score security with individual header marks (0 or 100 each).
     * Features: HTTPS, HSTS, CSP, X-Content-Type-Options, X-Frame-Options
     * Final score = average of all feature marks
     * 
     * @return Score (0-100) and detailed check results
     */
    public ScoringResult<SecurityDetails> scoreSecurity(String url, CloseableHttpResponse response) {
        if (response == null) {
            return new ScoringResult<>(0, new SecurityDetails(
                false, false, false, false, false, false, 0
            ));
        }

        int httpsScore = 0;
        int hstsScore = 0;
        int cspScore = 0;
        int xContentTypeScore = 0;
        int xFrameScore = 0;

        // 1. HTTPS: 0 or 100
        boolean https = url.toLowerCase().startsWith("https://");
        httpsScore = https ? 100 : 0;

        // 2. HSTS: 0 or 100
        boolean hsts = response.getFirstHeader("Strict-Transport-Security") != null;
        hstsScore = hsts ? 100 : 0;

        // 3. CSP: 0 or 100
        boolean csp = response.getFirstHeader("Content-Security-Policy") != null;
        cspScore = csp ? 100 : 0;

        // 4. X-Content-Type-Options: 0 or 100
        Header xContentTypeHeader = response.getFirstHeader("X-Content-Type-Options");
        boolean xContentTypeOptions = xContentTypeHeader != null && 
                xContentTypeHeader.getValue().toLowerCase().equals("nosniff");
        xContentTypeScore = xContentTypeOptions ? 100 : 0;

        // 5. X-Frame-Options: 0 or 100
        Header xfoHeader = response.getFirstHeader("X-Frame-Options");
        Header cspHeader = response.getFirstHeader("Content-Security-Policy");
        boolean xFrameOptions = xfoHeader != null || 
                (cspHeader != null && cspHeader.getValue().contains("frame-ancestors"));
        xFrameScore = xFrameOptions ? 100 : 0;

        // Final score = average of all 5 security headers
        int finalScore = (httpsScore + hstsScore + cspScore + xContentTypeScore + xFrameScore) / 5;
        finalScore = Math.max(0, Math.min(100, finalScore));

        return new ScoringResult<>(finalScore, new SecurityDetails(
            https, hsts, csp, xContentTypeOptions, xFrameOptions, false, finalScore
        ));
    }

    /**
     * Score SEO with individual feature marks (0-100 each).
     * Features: Page Title, Meta Description, Heading Structure, Mobile Friendly, Image Alt Text
     * Final score = average of all feature marks
     * 
     * @return Score (0-100) and detailed metrics
     */
    public ScoringResult<SEODetails> scoreSEO(String htmlContent) {
        if (htmlContent == null || htmlContent.isEmpty()) {
            return new ScoringResult<>(0, new SEODetails(
                false, false, false, false, false, false, 0, false, false, 0
            ));
        }

        int pageTitleScore = 0;
        int metaDescScore = 0;
        int headingScore = 0;
        int mobileScore = 0;
        int altTextScore = 0;

        // Parse HTML
        Document doc = Jsoup.parse(htmlContent);
        
        // PAGE TITLE: 0 if missing, 50 if present, 100 if optimal length (20-60 chars)
        Element titleTag = doc.selectFirst("title");
        boolean hasPageTitle = false;
        if (titleTag != null) {
            int titleLength = titleTag.text().length();
            if (titleLength >= 20 && titleLength <= 60) {
                pageTitleScore = 100;  // Optimal
                hasPageTitle = true;
            } else if (titleLength > 0) {
                pageTitleScore = 50;   // Present but not optimal
            }
        }

        // META DESCRIPTION: 0 if missing, 50 if present, 100 if optimal length (120-160 chars)
        Element metaDescription = doc.selectFirst("meta[name=description]");
        boolean hasMetaTags = metaDescription != null;
        boolean hasMetaDescriptionOptimal = false;
        if (metaDescription != null) {
            String description = metaDescription.attr("content");
            int descLength = description.length();
            if (descLength >= 120 && descLength <= 160) {
                metaDescScore = 100;  // Optimal
                hasMetaDescriptionOptimal = true;
            } else if (descLength > 0) {
                metaDescScore = 50;   // Present but not optimal
            }
        }

        // HEADING STRUCTURE: 0 if no H1, 50 if H1 only, 100 if H1+H2+
        Elements h1Tags = doc.select("h1");
        Elements h2Tags = doc.select("h2");
        boolean hasHeadingStructure = h1Tags.size() > 0 && h2Tags.size() > 0;
        if (h1Tags.size() > 0 && h2Tags.size() > 0) {
            headingScore = 100;  // Proper structure
        } else if (h1Tags.size() > 0) {
            headingScore = 50;   // Basic structure
        }

        // MOBILE FRIENDLY: 0 if no viewport, 100 if viewport meta tag present
        Element viewportTag = doc.selectFirst("meta[name=viewport]");
        boolean isMobileFriendly = viewportTag != null;
        mobileScore = isMobileFriendly ? 100 : 0;

        // IMAGE ALT TEXT: 0-100 based on percentage of images with alt text
        Elements images = doc.select("img");
        int imagesWithAlt = 0;
        for (Element img : images) {
            if (img.hasAttr("alt") && !img.attr("alt").isEmpty()) {
                imagesWithAlt++;
            }
        }
        int imageAltTextPercentage = images.size() > 0 ? 
            Math.round((imagesWithAlt * 100f) / images.size()) : 0;
        altTextScore = imageAltTextPercentage;  // Direct percentage 0-100

        // Final score = average of all 5 SEO features
        int finalScore = (pageTitleScore + metaDescScore + headingScore + mobileScore + altTextScore) / 5;
        finalScore = Math.max(0, Math.min(100, finalScore));
        
        return new ScoringResult<>(finalScore, new SEODetails(
            hasMetaTags, hasHeadingStructure, isMobileFriendly, false,
            false, false, imageAltTextPercentage, hasPageTitle,
            hasMetaDescriptionOptimal, finalScore
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

}

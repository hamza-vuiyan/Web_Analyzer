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

/** Service for scoring website performance, security, and SEO. */
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
    
    private Map<Integer, Integer> getPerformanceThresholds() {
        Map<Integer, Integer> thresholds = new HashMap<>();
        thresholds.put(200, threshold200);
        thresholds.put(500, threshold500);
        thresholds.put(1000, threshold1000);
        thresholds.put(2000, threshold2000);
        return thresholds;
    }
    
    private Map<Integer, Integer> getContentSizeLimits() {
        Map<Integer, Integer> limits = new HashMap<>();
        limits.put(300_000, contentSizeBonus300k);
        limits.put(1_000_000, contentSizeBonus1m);
        return limits;
    }

    public BackendProtocol getBackendAndProtocol(CloseableHttpResponse response, String url) {
        if (response == null) {
            return new BackendProtocol("Unknown", "Unknown");
        }

        String backend = getBackendInfo(response);
        String protocol = getProtocolInfo(response, url);
        
        return new BackendProtocol(backend, protocol);
    }

    private String getBackendInfo(CloseableHttpResponse response) {
        Header serverHeader = response.getFirstHeader("Server");
        Header poweredHeader = response.getFirstHeader("X-Powered-By");
        
        List<String> parts = Arrays.asList(
            serverHeader != null ? serverHeader.getValue() : null,
            poweredHeader != null ? poweredHeader.getValue() : null
        ).stream().filter(s -> s != null && !s.isEmpty()).collect(Collectors.toList());
        
        return parts.isEmpty() ? "Unknown" : String.join(", ", parts);
    }

    private String getProtocolInfo(CloseableHttpResponse response, String url) {
        String httpVersion = "HTTP/1.1";
        try {
            if (response.getVersion() != null) {
                httpVersion = response.getVersion().format();
            }
        } catch (Exception e) {
        }

        boolean isHttps = url.toLowerCase().startsWith("https://");
        return isHttps ? httpVersion + " over TLS" : httpVersion;
    }

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

        if (elapsedMs <= 200) {
            latencyScore = 100;
        } else if (elapsedMs <= 500) {
            latencyScore = 80;
        } else if (elapsedMs <= 1000) {
            latencyScore = 60;
        } else if (elapsedMs <= 2000) {
            latencyScore = 40;
        } else {
            latencyScore = 0;
        }

        if (response != null) {
            Header encodingHeader = response.getFirstHeader("Content-Encoding");
            if (encodingHeader != null) {
                String encoding = encodingHeader.getValue().toLowerCase();
                if (encoding.contains("br")) {
                    compressionScore = 100;
                    compression = "br";
                } else if (encoding.contains("gzip")) {
                    compressionScore = 50;
                    compression = "gzip";
                }
            }

            Header cacheHeader = response.getFirstHeader("Cache-Control");
            if (cacheHeader != null) {
                cacheControl = cacheHeader.getValue();
                if (cacheControl.contains("public") || cacheControl.contains("immutable")) {
                    cachingScore = 100;
                } else if (cacheControl.contains("max-age")) {
                    cachingScore = 50;
                }
            }

            Header contentLengthHeader = response.getFirstHeader("Content-Length");
            if (contentLengthHeader != null) {
                try {
                    long contentLength = Long.parseLong(contentLengthHeader.getValue());
                    contentLengthKb = (int) (contentLength / 1024);
                    
                    if (contentLength <= 300_000) {
                        contentSizeScore = 100;
                    } else if (contentLength <= 1_000_000) {
                        contentSizeScore = 50;
                    }
                } catch (NumberFormatException e) {
                }
            }
        }

        int finalScore = (latencyScore + compressionScore + cachingScore + contentSizeScore) / 4;
        finalScore = Math.max(0, Math.min(100, finalScore));
        
        return new ScoringResult<>(finalScore, new PerformanceDetails(
            elapsedMs, latencyScore, compression, cacheControl, contentLengthKb, 0, 0, finalScore
        ));
    }

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

        boolean https = url.toLowerCase().startsWith("https://");
        httpsScore = https ? 100 : 0;

        boolean hsts = response.getFirstHeader("Strict-Transport-Security") != null;
        hstsScore = hsts ? 100 : 0;

        boolean csp = response.getFirstHeader("Content-Security-Policy") != null;
        cspScore = csp ? 100 : 0;

        Header xContentTypeHeader = response.getFirstHeader("X-Content-Type-Options");
        boolean xContentTypeOptions = xContentTypeHeader != null && 
                xContentTypeHeader.getValue().toLowerCase().equals("nosniff");
        xContentTypeScore = xContentTypeOptions ? 100 : 0;

        Header xfoHeader = response.getFirstHeader("X-Frame-Options");
        Header cspHeader = response.getFirstHeader("Content-Security-Policy");
        boolean xFrameOptions = xfoHeader != null || 
                (cspHeader != null && cspHeader.getValue().contains("frame-ancestors"));
        xFrameScore = xFrameOptions ? 100 : 0;

        int finalScore = (httpsScore + hstsScore + cspScore + xContentTypeScore + xFrameScore) / 5;
        finalScore = Math.max(0, Math.min(100, finalScore));

        return new ScoringResult<>(finalScore, new SecurityDetails(
            https, hsts, csp, xContentTypeOptions, xFrameOptions, false, finalScore
        ));
    }

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

        Document doc = Jsoup.parse(htmlContent);
        
        Element titleTag = doc.selectFirst("title");
        boolean hasPageTitle = false;
        if (titleTag != null) {
            int titleLength = titleTag.text().length();
            if (titleLength >= 20 && titleLength <= 60) {
                pageTitleScore = 100;
                hasPageTitle = true;
            } else if (titleLength > 0) {
                pageTitleScore = 50;
            }
        }

        Element metaDescription = doc.selectFirst("meta[name=description]");
        boolean hasMetaTags = metaDescription != null;
        boolean hasMetaDescriptionOptimal = false;
        if (metaDescription != null) {
            String description = metaDescription.attr("content");
            int descLength = description.length();
            if (descLength >= 120 && descLength <= 160) {
                metaDescScore = 100;
                hasMetaDescriptionOptimal = true;
            } else if (descLength > 0) {
                metaDescScore = 50;
            }
        }

        Elements h1Tags = doc.select("h1");
        Elements h2Tags = doc.select("h2");
        boolean hasHeadingStructure = h1Tags.size() > 0 && h2Tags.size() > 0;
        if (h1Tags.size() > 0 && h2Tags.size() > 0) {
            headingScore = 100;
        } else if (h1Tags.size() > 0) {
            headingScore = 50;
        }

        Element viewportTag = doc.selectFirst("meta[name=viewport]");
        boolean isMobileFriendly = viewportTag != null;
        mobileScore = isMobileFriendly ? 100 : 0;

        Elements images = doc.select("img");
        int imagesWithAlt = 0;
        for (Element img : images) {
            if (img.hasAttr("alt") && !img.attr("alt").isEmpty()) {
                imagesWithAlt++;
            }
        }
        int imageAltTextPercentage = images.size() > 0 ? 
            Math.round((imagesWithAlt * 100f) / images.size()) : 0;
        altTextScore = imageAltTextPercentage;

        int finalScore = (pageTitleScore + metaDescScore + headingScore + mobileScore + altTextScore) / 5;
        finalScore = Math.max(0, Math.min(100, finalScore));
        
        return new ScoringResult<>(finalScore, new SEODetails(
            hasMetaTags, hasHeadingStructure, isMobileFriendly, false,
            false, false, imageAltTextPercentage, hasPageTitle,
            hasMetaDescriptionOptimal, finalScore
        ));
    }

    public static class BackendProtocol {
        public final String backend;
        public final String protocol;

        public BackendProtocol(String backend, String protocol) {
            this.backend = backend;
            this.protocol = protocol;
        }
    }

    public static class ScoringResult<T> {
        public final int score;
        public final T details;

        public ScoringResult(int score, T details) {
            this.score = score;
            this.details = details;
        }
    }

}

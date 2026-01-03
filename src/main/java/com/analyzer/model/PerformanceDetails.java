package com.analyzer.model;

/**
 * Detailed performance metrics for a website including:
 * - Response time (latency)
 * - Compression status (gzip, brotli)
 * - Caching headers
 * - Content size
 * - Broken links count
 */
public class PerformanceDetails {
    private double latencyMs;          // Response time in milliseconds
    private int latencyScore;          // Score based on latency (0-100)
    private String compression;        // Compression type: gzip, br, or none
    private String cacheControl;       // Cache-Control header value
    private int contentLengthKb;       // Page size in kilobytes
    private int brokenLinks;           // Number of broken links found
    private int totalLinks;            // Total number of links checked
    private int overallScore;          // Total performance score (0-100)

    // Default constructor
    public PerformanceDetails() {
    }

    // Constructor with all fields
    public PerformanceDetails(double latencyMs, int latencyScore, String compression, 
                            String cacheControl, int contentLengthKb, int brokenLinks,
                            int totalLinks, int overallScore) {
        this.latencyMs = latencyMs;
        this.latencyScore = latencyScore;
        this.compression = compression;
        this.cacheControl = cacheControl;
        this.contentLengthKb = contentLengthKb;
        this.brokenLinks = brokenLinks;
        this.totalLinks = totalLinks;
        this.overallScore = overallScore;
    }

    // Getters and Setters
    public double getLatencyMs() { return latencyMs; }
    public void setLatencyMs(double latencyMs) { this.latencyMs = latencyMs; }

    public int getLatencyScore() { return latencyScore; }
    public void setLatencyScore(int latencyScore) { this.latencyScore = latencyScore; }

    public String getCompression() { return compression; }
    public void setCompression(String compression) { this.compression = compression; }

    public String getCacheControl() { return cacheControl; }
    public void setCacheControl(String cacheControl) { this.cacheControl = cacheControl; }

    public int getContentLengthKb() { return contentLengthKb; }
    public void setContentLengthKb(int contentLengthKb) { this.contentLengthKb = contentLengthKb; }

    public int getBrokenLinks() { return brokenLinks; }
    public void setBrokenLinks(int brokenLinks) { this.brokenLinks = brokenLinks; }

    public int getTotalLinks() { return totalLinks; }
    public void setTotalLinks(int totalLinks) { this.totalLinks = totalLinks; }

    public int getOverallScore() { return overallScore; }
    public void setOverallScore(int overallScore) { this.overallScore = overallScore; }
}

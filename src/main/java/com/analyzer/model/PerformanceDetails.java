package com.analyzer.model;

/** Performance metrics for a website. */
public class PerformanceDetails {
    private double latencyMs;
    private int latencyScore;
    private String compression;
    private String cacheControl;
    private int contentLengthKb;
    private int brokenLinks;
    private int totalLinks;
    private int overallScore;

    public PerformanceDetails() {
    }

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

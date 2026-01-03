package com.analyzer.model;

/**
 * Server reliability metrics based on HTTP response.
 * Indicates how well the server responds and handles requests.
 */
public class ReliabilityDetails {
    private int statusCode;              // HTTP status code (200, 404, etc.)
    private int responseHeadersCount;    // Number of response headers
    private boolean gzipEnabled;         // Uses compression
    private boolean cacheEnabled;        // Has caching headers
    private int overallScore;            // Total reliability score (0-100)

    // Default constructor
    public ReliabilityDetails() {
    }

    // Constructor with all fields
    public ReliabilityDetails(int statusCode, int responseHeadersCount, 
                            boolean gzipEnabled, boolean cacheEnabled, int overallScore) {
        this.statusCode = statusCode;
        this.responseHeadersCount = responseHeadersCount;
        this.gzipEnabled = gzipEnabled;
        this.cacheEnabled = cacheEnabled;
        this.overallScore = overallScore;
    }

    // Getters and Setters
    public int getStatusCode() { return statusCode; }
    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }

    public int getResponseHeadersCount() { return responseHeadersCount; }
    public void setResponseHeadersCount(int count) { this.responseHeadersCount = count; }

    public boolean isGzipEnabled() { return gzipEnabled; }
    public void setGzipEnabled(boolean gzipEnabled) { this.gzipEnabled = gzipEnabled; }

    public boolean isCacheEnabled() { return cacheEnabled; }
    public void setCacheEnabled(boolean cacheEnabled) { this.cacheEnabled = cacheEnabled; }

    public int getOverallScore() { return overallScore; }
    public void setOverallScore(int overallScore) { this.overallScore = overallScore; }
}

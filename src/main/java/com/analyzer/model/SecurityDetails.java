package com.analyzer.model;

/**
 * Security assessment based on HTTPS and security headers.
 * Checks for important security practices that protect users.
 */
public class SecurityDetails {
    private boolean https;                  // Uses HTTPS protocol
    private boolean hsts;                   // Has Strict-Transport-Security header
    private boolean csp;                    // Has Content-Security-Policy header
    private boolean xContentTypeOptions;    // Has X-Content-Type-Options: nosniff
    private boolean xFrameOptions;          // Has X-Frame-Options header
    private boolean referrerPolicy;         // Has Referrer-Policy header
    private int overallScore;               // Total security score (0-100)

    // Default constructor
    public SecurityDetails() {
    }

    // Constructor with all fields
    public SecurityDetails(boolean https, boolean hsts, boolean csp, 
                          boolean xContentTypeOptions, boolean xFrameOptions, 
                          boolean referrerPolicy, int overallScore) {
        this.https = https;
        this.hsts = hsts;
        this.csp = csp;
        this.xContentTypeOptions = xContentTypeOptions;
        this.xFrameOptions = xFrameOptions;
        this.referrerPolicy = referrerPolicy;
        this.overallScore = overallScore;
    }

    // Getters and Setters
    public boolean isHttps() { return https; }
    public void setHttps(boolean https) { this.https = https; }

    public boolean isHsts() { return hsts; }
    public void setHsts(boolean hsts) { this.hsts = hsts; }

    public boolean isCsp() { return csp; }
    public void setCsp(boolean csp) { this.csp = csp; }

    public boolean isXContentTypeOptions() { return xContentTypeOptions; }
    public void setXContentTypeOptions(boolean xContentTypeOptions) { 
        this.xContentTypeOptions = xContentTypeOptions; 
    }

    public boolean isXFrameOptions() { return xFrameOptions; }
    public void setXFrameOptions(boolean xFrameOptions) { this.xFrameOptions = xFrameOptions; }

    public boolean isReferrerPolicy() { return referrerPolicy; }
    public void setReferrerPolicy(boolean referrerPolicy) { this.referrerPolicy = referrerPolicy; }

    public int getOverallScore() { return overallScore; }
    public void setOverallScore(int overallScore) { this.overallScore = overallScore; }
}

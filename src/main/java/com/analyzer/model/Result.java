package com.analyzer.model;

/**
 * Complete analysis result for a single website.
 * This is what gets returned to the frontend for display.
 */
public class Result {
    private String url;                              // Website URL
    private int performance;                         // Performance score (0-100)
    private int security;                           // Security score (0-100)
    private int seo;                                // SEO score (0-100)
    private int total;                              // Average of all scores
    private String backend;                         // Server software (Apache, nginx, etc.)
    private String protocols;                       // HTTP version and encryption
    private String responseTime;                    // Response time string (e.g., "234 ms")
    private PerformanceDetails performanceDetails;  // Detailed performance info
    private SecurityDetails securityDetails;        // Detailed security info
    private SEODetails seoDetails;                  // Detailed SEO info

    // Default constructor
    public Result() {
    }

    // Constructor with all fields
    public Result(String url, int performance, int security, int seo, int total,
                 String backend, String protocols, String responseTime,
                 PerformanceDetails performanceDetails, SecurityDetails securityDetails,
                 SEODetails seoDetails) {
        this.url = url;
        this.performance = performance;
        this.security = security;
        this.seo = seo;
        this.total = total;
        this.backend = backend;
        this.protocols = protocols;
        this.responseTime = responseTime;
        this.performanceDetails = performanceDetails;
        this.securityDetails = securityDetails;
        this.seoDetails = seoDetails;
    }

    // Getters and Setters
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public int getPerformance() { return performance; }
    public void setPerformance(int performance) { this.performance = performance; }

    public int getSecurity() { return security; }
    public void setSecurity(int security) { this.security = security; }

    public int getSeo() { return seo; }
    public void setSeo(int seo) { this.seo = seo; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public String getBackend() { return backend; }
    public void setBackend(String backend) { this.backend = backend; }

    public String getProtocols() { return protocols; }
    public void setProtocols(String protocols) { this.protocols = protocols; }

    public String getResponseTime() { return responseTime; }
    public void setResponseTime(String responseTime) { this.responseTime = responseTime; }

    public PerformanceDetails getPerformanceDetails() { return performanceDetails; }
    public void setPerformanceDetails(PerformanceDetails details) { 
        this.performanceDetails = details; 
    }

    public SecurityDetails getSecurityDetails() { return securityDetails; }
    public void setSecurityDetails(SecurityDetails details) { 
        this.securityDetails = details; 
    }

    public SEODetails getSeoDetails() { return seoDetails; }
    public void setSeoDetails(SEODetails details) { 
        this.seoDetails = details; 
    }
}

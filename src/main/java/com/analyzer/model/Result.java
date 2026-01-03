package com.analyzer.model;

/**
 * Complete analysis result for a single website.
 * This is what gets returned to the frontend for display.
 */
public class Result {
    private String url;                              // Website URL
    private int performance;                         // Performance score (0-100)
    private int security;                           // Security score (0-100)
    private int reliability;                        // Reliability score (0-100)
    private int total;                              // Average of all scores
    private String backend;                         // Server software (Apache, nginx, etc.)
    private String protocols;                       // HTTP version and encryption
    private String responseTime;                    // Response time string (e.g., "234 ms")
    private PerformanceDetails performanceDetails;  // Detailed performance info
    private SecurityDetails securityDetails;        // Detailed security info
    private ReliabilityDetails reliabilityDetails;  // Detailed reliability info

    // Default constructor
    public Result() {
    }

    // Constructor with all fields
    public Result(String url, int performance, int security, int reliability, int total,
                 String backend, String protocols, String responseTime,
                 PerformanceDetails performanceDetails, SecurityDetails securityDetails,
                 ReliabilityDetails reliabilityDetails) {
        this.url = url;
        this.performance = performance;
        this.security = security;
        this.reliability = reliability;
        this.total = total;
        this.backend = backend;
        this.protocols = protocols;
        this.responseTime = responseTime;
        this.performanceDetails = performanceDetails;
        this.securityDetails = securityDetails;
        this.reliabilityDetails = reliabilityDetails;
    }

    // Getters and Setters
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public int getPerformance() { return performance; }
    public void setPerformance(int performance) { this.performance = performance; }

    public int getSecurity() { return security; }
    public void setSecurity(int security) { this.security = security; }

    public int getReliability() { return reliability; }
    public void setReliability(int reliability) { this.reliability = reliability; }

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

    public ReliabilityDetails getReliabilityDetails() { return reliabilityDetails; }
    public void setReliabilityDetails(ReliabilityDetails details) { 
        this.reliabilityDetails = details; 
    }
}

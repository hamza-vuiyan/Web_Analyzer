package com.analyzer.config;

import org.springframework.context.annotation.Configuration;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration constants for the Website Analyzer.
 * This class holds all the settings used for analyzing websites.
 */
@Configuration
public class AnalyzerConfig {
    
    // ============ HTTP Client Settings ============
    
    /** Timeout for HTTP requests in milliseconds (15 seconds) */
    public static final int HTTP_TIMEOUT = 15000;
    
    /** User agent string to identify our analyzer */
    public static final String HTTP_USER_AGENT = 
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
        "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    
    /** Standard HTTP headers to send with requests */
    public static final Map<String, String> HTTP_HEADERS = new HashMap<>() {{
        put("User-Agent", HTTP_USER_AGENT);
        put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        put("Accept-Language", "en-US,en;q=0.9");
        put("Accept-Encoding", "gzip, deflate, br");
        put("Connection", "keep-alive");
    }};
    
    // ============ Performance Scoring Thresholds ============
    
    /** 
     * Performance scoring based on latency (response time).
     * Key = maximum latency in ms, Value = score awarded
     * Example: ≤200ms gets 100 points, ≤500ms gets 85 points
     */
    public static final Map<Integer, Integer> PERFORMANCE_THRESHOLDS = new HashMap<>() {{
        put(200, 100);   // Excellent: ≤200ms
        put(500, 85);    // Good: ≤500ms
        put(1000, 70);   // Fair: ≤1000ms (1 second)
        put(2000, 50);   // Poor: ≤2000ms (2 seconds)
        // Anything over 2 seconds gets 30 points (very poor)
    }};
    
    /** 
     * Bonus points for small content size.
     * Key = maximum size in bytes, Value = bonus points
     */
    public static final Map<Integer, Integer> CONTENT_SIZE_LIMITS = new HashMap<>() {{
        put(300_000, 10);    // Small page: ≤300KB gets +10 points
        put(1_000_000, 5);   // Medium page: ≤1MB gets +5 points
        // Larger pages get no bonus
    }};
}

package com.analyzer.model;

import java.util.List;

/**
 * Request body containing list of URLs to analyze.
 * This is what the frontend sends to the /analyze endpoint.
 */
public class URLRequest {
    private List<String> urls;

    // Constructor
    public URLRequest() {
    }

    // Getter - Spring uses this to return JSON
    public List<String> getUrls() {
        return urls;
    }

    // Setter - Spring uses this to parse incoming JSON
    public void setUrls(List<String> urls) {
        this.urls = urls;
    }
}

package com.analyzer.controller;

import com.analyzer.model.*;
import com.analyzer.service.FetcherService;
import com.analyzer.service.ScorerService;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * REST Controller that handles website analysis requests.
 * 
 * Main endpoint: POST /analyze
 * - Receives list of URLs from frontend
 * - Fetches each website
 * - Scores performance, security, and reliability
 * - Returns detailed results
 */
@RestController
@RequestMapping("/analyze")
public class AnalyzerController {

    private static final Logger logger = LoggerFactory.getLogger(AnalyzerController.class);

    private final FetcherService fetcherService;  // Handles HTTP requests
    private final ScorerService scorerService;    // Calculates scores

    @Autowired
    public AnalyzerController(FetcherService fetcherService, ScorerService scorerService) {
        this.fetcherService = fetcherService;
        this.scorerService = scorerService;
    }

    /**
     * Analyze multiple websites and return their scores.
     * This is the main API endpoint called by the frontend.
     */
    @PostMapping
    public List<Result> analyze(@RequestBody URLRequest urlRequest) {
        List<Result> results = new ArrayList<>();

        // Process each URL from the request
        for (String rawUrl : urlRequest.getUrls()) {
            // Step 1: Normalize URL (add https:// if missing)
            String url = fetcherService.normalizeUrl(rawUrl);
            logger.info("Analyzing: {}", url);

            // Step 2: Fetch the website
            FetcherService.FetchResult fetchResult = fetcherService.fetchPage(url);
            CloseableHttpResponse response = fetchResult.response;
            Double elapsedMs = fetchResult.elapsedMs;
            String error = fetchResult.error;

            Result result;

            if (response != null && error == null) {
                // SUCCESS CASE: Website was fetched successfully
                
                // Get server info (Apache, nginx, etc.)
                ScorerService.BackendProtocol backendProtocol = 
                        scorerService.getBackendAndProtocol(response, url);
                String responseTimeText = String.format("%.0f ms", elapsedMs);

                // Calculate scores for three categories
                ScorerService.ScoringResult<PerformanceDetails> perfResult = 
                        scorerService.scorePerformance(elapsedMs, response, fetchResult.htmlContent, url);
                ScorerService.ScoringResult<SecurityDetails> secResult = 
                        scorerService.scoreSecurity(url, response);
                ScorerService.ScoringResult<ReliabilityDetails> relResult = 
                        scorerService.scoreReliability(response);

                int performance = perfResult.score;
                int security = secResult.score;
                int reliability = relResult.score;
                // Total score is average of three categories
                int total = Math.round((performance + security + reliability) / 3.0f);

                logger.info("  → Performance: {} | Security: {} | Reliability: {} | Total: {}", 
                        performance, security, reliability, total);

                // Build result object
                result = new Result(
                    url,
                    performance,
                    security,
                    reliability,
                    total,
                    backendProtocol.backend,
                    backendProtocol.protocol,
                    responseTimeText,
                    perfResult.details,
                    secResult.details,
                    relResult.details
                );

                // Clean up HTTP connection
                try {
                    response.close();
                } catch (Exception e) {
                    logger.error("Error closing response", e);
                }
            } else {
                // FAILURE CASE: Could not fetch website - return 0 scores
                
                // Create default details with 0 scores for invalid/unreachable URLs
                PerformanceDetails perfDetails = new PerformanceDetails(
                    0, 0, "N/A", "", 0, 0, 0, 0
                );
                SecurityDetails secDetails = new SecurityDetails(
                    false, false, false, false, false, false, 0
                );
                ReliabilityDetails relDetails = new ReliabilityDetails(
                    0, 0, false, false, 0
                );

                logger.error("Fetch failed for {}: {}", url, error);

                result = new Result(
                    url,
                    0,  // All scores are 0 for invalid/unreachable URLs
                    0,
                    0,
                    0,
                    "N/A",
                    "N/A",
                    "Invalid URL",
                    perfDetails,
                    secDetails,
                    relDetails
                );
            }

            results.add(result);
        }

        return results;  // Spring automatically converts to JSON
    }
}

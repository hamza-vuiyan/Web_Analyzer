package com.analyzer.controller;

import com.analyzer.model.*;
import com.analyzer.service.FetcherService;
import com.analyzer.service.ScorerService;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/analyze")
public class AnalyzerController {


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
                ScorerService.ScoringResult<SEODetails> seoResult = 
                        scorerService.scoreSEO(fetchResult.htmlContent);

                int performance = perfResult.score;
                int security = secResult.score;
                int seo = seoResult.score;
                // Total score is average of three categories
                int total = Math.round((performance + security + seo) / 3.0f);

                

                // Build result object
                result = new Result(
                    url,
                    performance,
                    security,
                    seo,
                    total,
                    backendProtocol.backend,
                    backendProtocol.protocol,
                    responseTimeText,
                    perfResult.details,
                    secResult.details,
                    seoResult.details
                );

                // Clean up HTTP connection
                try {
                    response.close();
                } catch (Exception e) {
                    // response close failed; ignoring as cleanup
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
                SEODetails seoDetails = new SEODetails(
                    false, false, false, false, false, false, 0, false, false, 0
                );

                // Fetch failed for URL: error ignored in response

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
                    seoDetails
                );
            }

            results.add(result);
        }

        return results;  // Spring automatically converts to JSON
    }
}

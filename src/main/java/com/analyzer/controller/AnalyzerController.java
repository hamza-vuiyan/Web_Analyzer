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

    private final FetcherService fetcherService;
    private final ScorerService scorerService;

    @Autowired
    public AnalyzerController(FetcherService fetcherService, ScorerService scorerService) {
        this.fetcherService = fetcherService;
        this.scorerService = scorerService;
    }

        /** Main API endpoint for analyzing multiple websites. */
    @PostMapping
    public List<Result> analyze(@RequestBody URLRequest urlRequest) {
        
        List<Result> results = new ArrayList<>();

        for (String rawUrl : urlRequest.getUrls()) {
            String url = fetcherService.normalizeUrl(rawUrl);

            FetcherService.FetchResult fetchResult = fetcherService.fetchPage(url);
            CloseableHttpResponse response = fetchResult.response;
            Double elapsedMs = fetchResult.elapsedMs;
            String error = fetchResult.error;

            Result result;

            if (response != null && error == null) {
                ScorerService.BackendProtocol backendProtocol = 
                        scorerService.getBackendAndProtocol(response, url);
                String responseTimeText = String.format("%.0f ms", elapsedMs);

                ScorerService.ScoringResult<PerformanceDetails> perfResult = 
                        scorerService.scorePerformance(elapsedMs, response, fetchResult.htmlContent, url);
                ScorerService.ScoringResult<SecurityDetails> secResult = 
                        scorerService.scoreSecurity(url, response);
                ScorerService.ScoringResult<SEODetails> seoResult = 
                        scorerService.scoreSEO(fetchResult.htmlContent);

                int performance = perfResult.score;
                int security = secResult.score;
                int seo = seoResult.score;
                int total = Math.round((performance + security + seo) / 3.0f);

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

                try {
                    response.close();
                } catch (Exception e) {
                }
            } else {
                PerformanceDetails perfDetails = new PerformanceDetails(
                    0, 0, "N/A", "", 0, 0, 0, 0
                );
                SecurityDetails secDetails = new SecurityDetails(
                    false, false, false, false, false, false, 0
                );
                SEODetails seoDetails = new SEODetails(
                    false, false, false, false, false, false, 0, false, false, 0
                );

                result = new Result(
                    url,
                    0,
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

        return results;
    }
}

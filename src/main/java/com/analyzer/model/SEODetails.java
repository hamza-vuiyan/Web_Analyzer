package com.analyzer.model;

/**
 * SEO (Search Engine Optimization) metrics for a website.
 * Evaluates how well-optimized a website is for search engines.
 */
public class SEODetails {
    private boolean hasMetaTags;            // Has meta description and keywords
    private boolean hasHeadingStructure;    // Has proper H1, H2, H3 structure
    private boolean isMobileFriendly;       // Has mobile viewport meta tag
    private boolean hasCanonicalTag;        // Has canonical URL tag
    private boolean hasRobotsTxt;           // Has robots.txt accessible
    private boolean hasSitemapXml;          // Has sitemap.xml
    private int imageAltTextPercentage;     // Percentage of images with alt text
    private boolean hasPageTitle;           // Has proper page title (3-60 chars)
    private boolean hasMetaDescription;     // Has meta description (120-160 chars)
    private int overallScore;               // Total SEO score (0-100)

    // Default constructor
    public SEODetails() {
    }

    // Constructor with all fields
    public SEODetails(boolean hasMetaTags, boolean hasHeadingStructure, 
                      boolean isMobileFriendly, boolean hasCanonicalTag,
                      boolean hasRobotsTxt, boolean hasSitemapXml,
                      int imageAltTextPercentage, boolean hasPageTitle,
                      boolean hasMetaDescription, int overallScore) {
        this.hasMetaTags = hasMetaTags;
        this.hasHeadingStructure = hasHeadingStructure;
        this.isMobileFriendly = isMobileFriendly;
        this.hasCanonicalTag = hasCanonicalTag;
        this.hasRobotsTxt = hasRobotsTxt;
        this.hasSitemapXml = hasSitemapXml;
        this.imageAltTextPercentage = imageAltTextPercentage;
        this.hasPageTitle = hasPageTitle;
        this.hasMetaDescription = hasMetaDescription;
        this.overallScore = overallScore;
    }

    // Getters and Setters
    public boolean isHasMetaTags() { return hasMetaTags; }
    public void setHasMetaTags(boolean hasMetaTags) { this.hasMetaTags = hasMetaTags; }

    public boolean isHasHeadingStructure() { return hasHeadingStructure; }
    public void setHasHeadingStructure(boolean hasHeadingStructure) { this.hasHeadingStructure = hasHeadingStructure; }

    public boolean isMobileFriendly() { return isMobileFriendly; }
    public void setMobileFriendly(boolean mobileFriendly) { this.isMobileFriendly = mobileFriendly; }

    public boolean isHasCanonicalTag() { return hasCanonicalTag; }
    public void setHasCanonicalTag(boolean hasCanonicalTag) { this.hasCanonicalTag = hasCanonicalTag; }

    public boolean isHasRobotsTxt() { return hasRobotsTxt; }
    public void setHasRobotsTxt(boolean hasRobotsTxt) { this.hasRobotsTxt = hasRobotsTxt; }

    public boolean isHasSitemapXml() { return hasSitemapXml; }
    public void setHasSitemapXml(boolean hasSitemapXml) { this.hasSitemapXml = hasSitemapXml; }

    public int getImageAltTextPercentage() { return imageAltTextPercentage; }
    public void setImageAltTextPercentage(int imageAltTextPercentage) { this.imageAltTextPercentage = imageAltTextPercentage; }

    public boolean isHasPageTitle() { return hasPageTitle; }
    public void setHasPageTitle(boolean hasPageTitle) { this.hasPageTitle = hasPageTitle; }

    public boolean isHasMetaDescription() { return hasMetaDescription; }
    public void setHasMetaDescription(boolean hasMetaDescription) { this.hasMetaDescription = hasMetaDescription; }

    public int getOverallScore() { return overallScore; }
    public void setOverallScore(int overallScore) { this.overallScore = overallScore; }
}

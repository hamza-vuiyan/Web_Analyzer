package com.analyzer.model;

/** SEO (Search Engine Optimization) metrics for a website. */
public class SEODetails {
    private boolean hasMetaTags;
    private boolean hasHeadingStructure;
    private boolean isMobileFriendly;
    private boolean hasCanonicalTag;
    private boolean hasRobotsTxt;
    private boolean hasSitemapXml;
    private int imageAltTextPercentage;
    private boolean hasPageTitle;
    private boolean hasMetaDescription;
    private int overallScore;

    public SEODetails() {
    }

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

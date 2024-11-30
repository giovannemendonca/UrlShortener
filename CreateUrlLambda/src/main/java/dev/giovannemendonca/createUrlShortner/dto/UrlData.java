package dev.giovannemendonca.createUrlShortner.dto;


public class UrlData {

    private String originalUrl;

    private long expirationTime;


    public UrlData(String originalUrl, long expirationTime) {
        this.originalUrl = originalUrl;
        this.expirationTime = expirationTime;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public long getExpirationTime() {
        return expirationTime;
    }
}

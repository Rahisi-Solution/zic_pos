package com.example.zicapp.utils;

public class OfflineDepartureRequest {
    private long id;
    private String authToken;
    private String referenceNumber;

    public OfflineDepartureRequest(String authToken, String referenceNumber) {
        this.authToken = authToken;
        this.referenceNumber = referenceNumber;
    }

    //Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber() {
        this.referenceNumber = referenceNumber;
    }
}

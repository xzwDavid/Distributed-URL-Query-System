package edu.lantonia.tinyurl.backend.model;

public class NewRangeResponse {
    public final long start;
    public final long end;

    public NewRangeResponse(long start, long end) {
        this.start = start;
        this.end = end;
    }
}

package ru.practicum.moviehub.api;

import java.util.List;

public class ErrorResponse {
    public String error;
    public List<String> details;

    public ErrorResponse(String error, List<String> details) {
        this.details = details;
        this.error = error;
    }

    public ErrorResponse(String error) {
        this.error = error;
    }
}
package org.example.adresarzam.response;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class ResponseWrapper<T> {
    private final T data;
    private final String message;
    private final List<Map<String, String>> errors;
    private final Instant timestamp;

    public ResponseWrapper(String message, T data, List<Map<String, String>> errors) {
        this.data = data;
        this.message = message;
        this.errors = errors;
        this.timestamp = Instant.now();
    }

    public T getData() { return data; }
    public String getMessage() { return message; }
    public List<Map<String, String>> getErrors() { return errors; }
    public Instant getTimestamp() { return timestamp; }
}

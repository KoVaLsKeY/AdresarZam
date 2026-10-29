package org.example.adresarzam.exception;

import org.example.adresarzam.response.ResponseFactory;
import org.example.adresarzam.response.ResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final ResponseFactory response;

    public GlobalExceptionHandler(ResponseFactory response) {
        this.response = response;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleValidation(MethodArgumentNotValidException ex) {
        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> Map.of("field", error.getField(), "message",
                        Objects.requireNonNullElse(error.getDefaultMessage(), "Neplatna hodnota")))
                .toList();
        return response.error(HttpStatus.BAD_REQUEST, "Validacne chyby", errors);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return response.error(HttpStatus.BAD_REQUEST,
                "Parameter '" + ex.getName() + "' ma neplatnu hodnotu");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleUnreadableBody(HttpMessageNotReadableException ex) {
        return response.error(HttpStatus.BAD_REQUEST,
                "Neplatny JSON alebo nespravny typ hodnoty (datum musi mat format YYYY-MM-DD)");
    }

    @ExceptionHandler(ObjectNotFoundException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleObjectNotFound(ObjectNotFoundException ex) {
        return response.error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleEmailExists(EmailAlreadyExistsException ex) {
        return response.error(HttpStatus.CONFLICT, ex.getMessage(),
                List.of(Map.of("field", "email", "message", ex.getMessage())));
    }

    @ExceptionHandler(InvalidPhoneNumberException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleInvalidPhone(InvalidPhoneNumberException ex) {
        return response.error(HttpStatus.BAD_REQUEST, ex.getMessage(),
                List.of(Map.of("field", "phone", "message", ex.getMessage())));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseWrapper<Void>> handleGeneral(Exception ex) {
        // Preserve framework statuses such as 404, 405 and 415 in the same JSON format.
        if (ex instanceof ErrorResponse error) {
            return ResponseEntity.status(error.getStatusCode()).headers(error.getHeaders())
                    .body(new ResponseWrapper<>(
                            Objects.requireNonNullElse(error.getBody().getDetail(), "Chyba poziadavky"),
                            null, null));
        }
        log.error("Neocakavana chyba pri spracovani poziadavky", ex);
        return response.error(HttpStatus.INTERNAL_SERVER_ERROR, "Nastala neocakavana chyba");
    }
}

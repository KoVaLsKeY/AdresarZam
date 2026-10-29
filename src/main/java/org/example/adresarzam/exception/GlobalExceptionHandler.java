package org.example.adresarzam.exception;


import org.example.adresarzam.response.ResponseFactory;
import org.example.adresarzam.response.ResponseWrapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;


import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

        private final ResponseFactory response;

        public GlobalExceptionHandler(ResponseFactory response) {
            this.response = response;
        }

        // Validačné chyby
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ResponseWrapper<Void>> handleValidation(MethodArgumentNotValidException ex) {
            List<Map<String, String>> errors = ex.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .map(error -> Map.of(
                            "field", error.getField(),
                            "message", error.getDefaultMessage()
                    ))
                    .toList();

            return response.error(HttpStatus.BAD_REQUEST, "Validačné chyby", errors);
        }

        // Nesprávny typ parametra, napr. /students/abc namiesto číselného ID
        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ResponseWrapper<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
            String message = "Parameter '" + ex.getName() + "' má neplatnú hodnotu";
            return response.error(HttpStatus.BAD_REQUEST, message);
        }

        // sem doplníme dalšie metódy neskôr ...

        // Ostatné chyby
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ResponseWrapper<Void>> handleGeneral(Exception ex) {
            return response.error(HttpStatus.INTERNAL_SERVER_ERROR, "Nastala neočakávaná chyba");
        }

    // chýbajúce metódy v GlobalExceptionHandler

    // Objekt nenájdený - genericky pre všetky entity
    @ExceptionHandler(ObjectNotFoundException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleObjectNotFound(ObjectNotFoundException ex) {
        return response.error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // Email už existuje
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleEmailExists(EmailAlreadyExistsException ex) {
        return response.error(HttpStatus.CONFLICT, ex.getMessage());
    }
    }

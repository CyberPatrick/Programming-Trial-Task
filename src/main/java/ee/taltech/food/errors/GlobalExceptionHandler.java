package ee.taltech.food.errors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Date;
import java.util.Objects;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler
    public ResponseEntity<ErrorObject> handlerErrors(Exception ex) {

        ErrorObject error = new ErrorObject();
        error.setTimestamp(new Date());
        if (ex instanceof ServiceError) {
            error.setMessage(ex.getMessage());
        } else if (ex instanceof MethodArgumentTypeMismatchException methodArgumentTypeMismatchException) {
            if (Objects.nonNull(methodArgumentTypeMismatchException.getRootCause())) {
                error.setMessage("Argument type mismatch - " + methodArgumentTypeMismatchException.getRootCause().getMessage());
            } else {
                error.setMessage("Argument type mismatch");
            }
        } else if (ex instanceof MissingServletRequestParameterException) {
            error.setMessage("Argument is missing");
        } else {
            error.setMessage("Bad request");
        }

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}

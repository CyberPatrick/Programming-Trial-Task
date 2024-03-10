package ee.taltech.food.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ServiceError extends Exception {
    public ServiceError(String message) {
        super(message);
    }
}

package ee.taltech.food.errors;

import lombok.Data;

import java.util.Date;

@Data
public class ErrorObject {
    private String message;
    private Date timestamp;
}

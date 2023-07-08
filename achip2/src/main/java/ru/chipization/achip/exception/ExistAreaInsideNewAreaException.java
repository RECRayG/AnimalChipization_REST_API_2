package ru.chipization.achip.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST) // 400
public class ExistAreaInsideNewAreaException extends RuntimeException {
    public ExistAreaInsideNewAreaException(String message) {
        super(message);
    }
}

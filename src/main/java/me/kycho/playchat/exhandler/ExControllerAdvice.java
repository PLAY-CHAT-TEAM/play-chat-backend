package me.kycho.playchat.exhandler;

import me.kycho.playchat.exception.DuplicatedEmailException;
import me.kycho.playchat.exhandler.dto.ErrorDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExControllerAdvice {

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler
    public ErrorDto duplicatedEmailEx(DuplicatedEmailException ex) {
        return new ErrorDto(HttpStatus.CONFLICT.value(), ex.getMessage());
    }
}

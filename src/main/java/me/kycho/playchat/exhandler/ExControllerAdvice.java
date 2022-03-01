package me.kycho.playchat.exhandler;

import java.util.List;
import me.kycho.playchat.exception.DuplicatedEmailException;
import me.kycho.playchat.exception.MemberNotFoundException;
import me.kycho.playchat.exhandler.dto.ErrorDto;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
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

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler
    public ErrorDto memberNotFoundException(MemberNotFoundException ex) {
        return new ErrorDto(HttpStatus.NOT_FOUND.value(), ex.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public ErrorDto methodArgumentNotValidException(BindException ex) {
        BindingResult result = ex.getBindingResult();
        List<FieldError> fieldErrors = result.getFieldErrors();
        return processFieldErrors(fieldErrors);
    }

    private ErrorDto processFieldErrors(List<FieldError> fieldErrors) {
        ErrorDto errorDto = new ErrorDto(HttpStatus.BAD_REQUEST.value(), "Binding Error.");
        for (FieldError fieldError : fieldErrors) {
            errorDto.addFieldError(fieldError);
        }
        return errorDto;
    }
}

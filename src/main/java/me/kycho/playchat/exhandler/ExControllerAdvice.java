package me.kycho.playchat.exhandler;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.kycho.playchat.exception.DuplicatedEmailException;
import me.kycho.playchat.exception.MemberNotFoundException;
import me.kycho.playchat.exhandler.dto.ErrorDto;
import me.kycho.playchat.validator.UpdatePasswordRequestValidator;
import me.kycho.playchat.validator.UpdateProfileRequestValidator;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RequiredArgsConstructor
@RestControllerAdvice
public class ExControllerAdvice {

    private final UpdateProfileRequestValidator updateProfileRequestValidator;
    private final UpdatePasswordRequestValidator updatePasswordRequestValidator;

    @InitBinder("updateProfileRequestDto")
    public void addUpdateProfileRequestValidator(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(updateProfileRequestValidator);
    }

    @InitBinder("updatePasswordRequestDto")
    public void addUpdatePasswordRequestValidator(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(updatePasswordRequestValidator);
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler
    public ErrorDto accessDeniedEx(AccessDeniedException ex) {
        return new ErrorDto(HttpStatus.FORBIDDEN.value(), ex.getMessage());
    }

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
    public ErrorDto methodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        return new ErrorDto(HttpStatus.BAD_REQUEST.value(), "잘못된 요청입니다.");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public ErrorDto methodArgumentNotValidException(BindException ex) {
        ObjectError globalError = ex.getGlobalError();
        if (globalError != null) {
            return new ErrorDto(HttpStatus.BAD_REQUEST.value(), globalError.getDefaultMessage());
        }
        return processFieldErrors(ex.getBindingResult().getFieldErrors());
    }

    private ErrorDto processFieldErrors(List<FieldError> fieldErrors) {
        ErrorDto errorDto = new ErrorDto(HttpStatus.BAD_REQUEST.value(), "입력 값이 잘못되었습니다.");
        for (FieldError fieldError : fieldErrors) {
            errorDto.addFieldError(fieldError);
        }
        return errorDto;
    }
}

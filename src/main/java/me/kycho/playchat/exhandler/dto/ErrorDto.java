package me.kycho.playchat.exhandler.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.validation.FieldError;

@AllArgsConstructor
@Getter
public class ErrorDto {

    private Integer status;
    private String message;
    private List<FieldErrorDto> fieldErrors = new ArrayList<>();

    public ErrorDto(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public void addFieldError(FieldError error) {
        FieldErrorDto fieldErrorDto =
            new FieldErrorDto(error.getField(), error.getDefaultMessage(), error.getRejectedValue());
        fieldErrors.add(fieldErrorDto);
    }

    @AllArgsConstructor
    @Getter
    static class FieldErrorDto {
        private String field;
        private String defaultMessage;
        private Object rejectedValue;
    }
}

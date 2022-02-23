package me.kycho.playchat.exhandler.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class ErrorDto {

    private Integer status;
    private String message;
}

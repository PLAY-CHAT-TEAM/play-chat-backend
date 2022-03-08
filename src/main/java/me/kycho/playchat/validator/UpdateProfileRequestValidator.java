package me.kycho.playchat.validator;

import me.kycho.playchat.controller.dto.UpdateProfileRequestDto;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class UpdateProfileRequestValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return UpdateProfileRequestDto.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        UpdateProfileRequestDto dto = (UpdateProfileRequestDto) target;
        if (dto.getNickname() == null && dto.getProfileImage() == null) {
            errors.reject(null, "수정할 회원정보가 없습니다.");
        }
    }
}

package me.kycho.playchat.validator;

import me.kycho.playchat.controller.dto.UpdatePasswordRequestDto;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class UpdatePasswordRequestValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return UpdatePasswordRequestDto.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        if (errors.hasErrors()) {
            return;
        }

        UpdatePasswordRequestDto dto = (UpdatePasswordRequestDto) target;

        String newPassword = dto.getNewPassword();
        String newPasswordConfirm = dto.getNewPasswordConfirm();

        if (!newPassword.equals(newPasswordConfirm)) {
            errors.reject(null, "새로운 비밀번호와 비밀번호 확인값이 일치하지 않습니다.");
        }
    }
}

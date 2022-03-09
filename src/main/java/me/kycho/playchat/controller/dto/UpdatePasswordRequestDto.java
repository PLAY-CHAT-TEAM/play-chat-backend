package me.kycho.playchat.controller.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePasswordRequestDto {

    @NotBlank(message = "현재 비밀번호는 필수 값입니다.")
    private String currentPassword;

    @NotBlank(message = "변경할 비밀번호는 필수 값입니다.")
    @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[$@!%*#?&]).{8,16}",
        message = "비밀번호는 영문자, 숫자, 특수기호($@!%*#?&)가 적어도 1개 이상씩 포함된 길이 8~16인 글자여야 합니다.")
    private String newPassword;

    @NotBlank(message = "변경할 비밀번호 확인값은 필수 값입니다.")
    private String newPasswordConfirm;

}

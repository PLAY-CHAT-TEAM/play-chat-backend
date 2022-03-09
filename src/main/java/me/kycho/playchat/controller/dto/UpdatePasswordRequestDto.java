package me.kycho.playchat.controller.dto;

import javax.validation.constraints.NotBlank;
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
    private String newPassword;

    @NotBlank(message = "변경할 비밀번호 확인값은 필수 값입니다.")
    private String newPasswordConfirm;

}

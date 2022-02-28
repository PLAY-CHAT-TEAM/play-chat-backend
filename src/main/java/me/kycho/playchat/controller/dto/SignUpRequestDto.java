package me.kycho.playchat.controller.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.kycho.playchat.domain.Member;
import org.springframework.web.multipart.MultipartFile;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SignUpRequestDto {

    @Email(message = "이메일 형식이어야 합니다.")
    @NotBlank(message = "이메일은 필수값입니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수값입니다.")
    @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[$@!%*#?&]).{8,16}",
        message = "비밀번호는 영문자, 숫자, 특수기호($@!%*#?&)가 적어도 1개 이상씩 포함된 길이 8~16인 글자여야 합니다.")
    private String password;

    @NotBlank
    private String nickname;

    @NotNull  // TODO : 좀더 확인해봐야함
    private MultipartFile profileImage;

    private String profileImageFileName;

    public Member toMemberEntity() {
        return Member.builder()
            .email(email)
            .password(password)
            .nickname(nickname)
            .imageUrl(profileImageFileName)
            .build();
    }
}

package me.kycho.playchat.controller.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.kycho.playchat.domain.Member;
import org.springframework.web.multipart.MultipartFile;

@Builder
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class JoinRequestDto {

    @Email
    @NotBlank
    private String email;

    @NotBlank
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

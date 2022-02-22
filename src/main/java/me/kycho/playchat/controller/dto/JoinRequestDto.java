package me.kycho.playchat.controller.dto;

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

    private String email;
    private String password;
    private String name;
    private MultipartFile profileImage;
    private String profileImageFileName;

    public Member toMemberEntity() {
        return Member.builder()
            .email(email)
            .password(password)
            .name(name)
            .imageUrl(profileImageFileName)
            .build();
    }
}

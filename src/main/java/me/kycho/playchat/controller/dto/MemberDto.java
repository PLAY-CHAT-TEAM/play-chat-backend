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
public class MemberDto {

    private String email;
    private String password;
    private String name;
    private MultipartFile profileImage;
    private String profileImageFileName;

    public Member toEntity() {
        return Member.builder()
            .email(email)
            .password(password)
            .name(name)
            .imageUrl(profileImageFileName)
            .build();
    }
}

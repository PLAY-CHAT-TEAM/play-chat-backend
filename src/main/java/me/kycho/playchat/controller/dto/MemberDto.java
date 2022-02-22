package me.kycho.playchat.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.kycho.playchat.domain.Member;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MemberDto {

    private String email;
    private String password;
    private String name;
    private String imageUrl;

    public Member toEntity() {
        return Member.builder()
            .email(email)
            .password(password)
            .name(name)
            .imageUrl(imageUrl)
            .build();
    }
}

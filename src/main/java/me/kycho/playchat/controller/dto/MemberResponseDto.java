package me.kycho.playchat.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.kycho.playchat.domain.Member;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MemberResponseDto {

    private Long id;
    private String email;
    private String nickname;
    private String imageUrl;

    public static MemberResponseDto from(Member member) {
        return MemberResponseDto.builder()
            .id(member.getId())
            .email(member.getEmail())
            .nickname(member.getNickname())
            .imageUrl(member.getImageUrl())
            .build();
    }
}

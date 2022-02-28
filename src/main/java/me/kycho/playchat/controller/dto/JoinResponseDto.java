package me.kycho.playchat.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.kycho.playchat.domain.Member;

@Getter
@AllArgsConstructor
public class JoinResponseDto {

    private String email;
    private String nickname;

    public static JoinResponseDto from(Member member) {
        return new JoinResponseDto(member.getEmail(), member.getNickname());
    }
}

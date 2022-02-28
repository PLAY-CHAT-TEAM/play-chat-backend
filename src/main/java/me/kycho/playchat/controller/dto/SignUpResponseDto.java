package me.kycho.playchat.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.kycho.playchat.domain.Member;

@Getter
@AllArgsConstructor
public class SignUpResponseDto {

    private String email;
    private String nickname;

    public static SignUpResponseDto from(Member member) {
        return new SignUpResponseDto(member.getEmail(), member.getNickname());
    }
}

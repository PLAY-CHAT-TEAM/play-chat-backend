package me.kycho.playchat.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.kycho.playchat.domain.Member;

@Getter
@AllArgsConstructor
public class JoinResultDto {

    private String email;
    private String name;

    public static JoinResultDto from(Member member) {
        return new JoinResultDto(member.getEmail(), member.getName());
    }
}

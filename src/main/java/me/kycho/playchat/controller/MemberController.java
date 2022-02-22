package me.kycho.playchat.controller;

import lombok.RequiredArgsConstructor;
import me.kycho.playchat.controller.dto.JoinResultDto;
import me.kycho.playchat.controller.dto.MemberDto;
import me.kycho.playchat.domain.Member;
import me.kycho.playchat.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    @PostMapping()
    public ResponseEntity<JoinResultDto> join(@RequestBody MemberDto memberDto) {
        // TODO : 예외 처리
        Member joinedMember = memberService.join(memberDto.toEntity());
        JoinResultDto result = JoinResultDto.from(joinedMember);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}

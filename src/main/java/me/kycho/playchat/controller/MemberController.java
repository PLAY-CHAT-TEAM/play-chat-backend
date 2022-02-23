package me.kycho.playchat.controller;

import java.io.IOException;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.kycho.playchat.common.FileStore;
import me.kycho.playchat.controller.dto.JoinResponseDto;
import me.kycho.playchat.controller.dto.JoinRequestDto;
import me.kycho.playchat.domain.Member;
import me.kycho.playchat.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;
    private final FileStore fileStore;

    @PostMapping("/join")
    public ResponseEntity<JoinResponseDto> join(@Valid @ModelAttribute JoinRequestDto joinRequestDto)
        throws IOException {
        // TODO : 예외 처리

        MultipartFile profileImage = joinRequestDto.getProfileImage();
        String storedFileName = fileStore.storeFile(profileImage);
        joinRequestDto.setProfileImageFileName(storedFileName);

        Member joinedMember = memberService.join(joinRequestDto.toMemberEntity());
        JoinResponseDto response = JoinResponseDto.from(joinedMember);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

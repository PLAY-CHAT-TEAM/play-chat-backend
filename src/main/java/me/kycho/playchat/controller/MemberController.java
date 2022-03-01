package me.kycho.playchat.controller;

import java.io.IOException;
import java.util.Optional;
import javax.validation.Valid;
import me.kycho.playchat.common.FileStore;
import me.kycho.playchat.controller.dto.MemberResponseDto;
import me.kycho.playchat.controller.dto.SignUpRequestDto;
import me.kycho.playchat.controller.dto.SignUpResponseDto;
import me.kycho.playchat.domain.Member;
import me.kycho.playchat.service.MemberService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;
    private final FileStore fileStore;
    private final String backendUrl;

    public MemberController(
        MemberService memberService, FileStore fileStore, @Value("${backend.url}") String backendUrl
    ) {
        this.memberService = memberService;
        this.fileStore = fileStore;
        this.backendUrl = backendUrl;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<SignUpResponseDto> signUp(
        @Valid @ModelAttribute SignUpRequestDto signUpRequestDto) throws IOException {

        MultipartFile profileImage = signUpRequestDto.getProfileImage();
        String storedFileName = fileStore.storeFile(profileImage);

        String profileImageUrl = backendUrl;
        if (storedFileName == null) {
            profileImageUrl += "/images/default-profile.png";
        } else {
            profileImageUrl += "/api/members/profile-image/" + storedFileName;
        }

        signUpRequestDto.setProfileImageUrl(profileImageUrl);

        Member signedUpMember = memberService.signUp(signUpRequestDto.toMemberEntity());
        SignUpResponseDto response = SignUpResponseDto.from(signedUpMember);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<MemberResponseDto> getMember(@PathVariable Long memberId) {
        Optional<Member> member = memberService.getMember(memberId);

        if (member.isPresent()) {
            return ResponseEntity.ok().body(MemberResponseDto.from(member.get()));
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/profile-image/{filename}")
    public ResponseEntity<Resource> downloadImage(@PathVariable String filename)
        throws IOException {

        UrlResource urlResource = new UrlResource("file:" + fileStore.getFullPath(filename));
        if (urlResource.exists()) {
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE)
                .body(urlResource);
        }
        return ResponseEntity.notFound().build();
    }
}

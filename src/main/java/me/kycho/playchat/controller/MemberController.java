package me.kycho.playchat.controller;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import me.kycho.playchat.controller.dto.MemberResponseDto;
import me.kycho.playchat.controller.dto.SignUpRequestDto;
import me.kycho.playchat.controller.dto.SignUpResponseDto;
import me.kycho.playchat.controller.dto.UpdatePasswordRequestDto;
import me.kycho.playchat.controller.dto.UpdateProfileRequestDto;
import me.kycho.playchat.domain.Member;
import me.kycho.playchat.service.MemberService;
import me.kycho.playchat.utils.FileStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @GetMapping("/me")
    public ResponseEntity<MemberResponseDto> getMyInfo(@AuthenticationPrincipal User currentUser) {
        String currentMemberEmail = currentUser.getUsername();
        Member currentMember = memberService.getMemberByEmail(currentMemberEmail);
        return ResponseEntity.ok().body(MemberResponseDto.from(currentMember));
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<MemberResponseDto> getMember(@PathVariable Long memberId) {
        Member member = memberService.getMember(memberId);
        return ResponseEntity.ok().body(MemberResponseDto.from(member));
    }

    @GetMapping("/list")
    public ResponseEntity<List<MemberResponseDto>> getMemberList() {
        List<Member> members = memberService.getMemberAll();

        List<MemberResponseDto> collect = members.stream().map(MemberResponseDto::from)
            .collect(Collectors.toList());

        return ResponseEntity.ok().body(collect);
    }

    @PostMapping("/{memberId}/update")
    public ResponseEntity updateProfile(
        @AuthenticationPrincipal User currentUser, @PathVariable Long memberId,
        @Valid @ModelAttribute UpdateProfileRequestDto updateProfileRequestDto) throws IOException {

        Member currentMember = memberService.getMemberByEmail(currentUser.getUsername());
        if (!currentMember.getId().equals(memberId)) {
            throw new AccessDeniedException("수정 권한이 없습니다.");
        }

        MultipartFile profileImage = updateProfileRequestDto.getProfileImage();
        if (profileImage != null) {
            String storedFileName = fileStore.storeFile(profileImage);

            String profileImageUrl = backendUrl;
            if (storedFileName == null) {
                profileImageUrl += "/images/default-profile.png";
            } else {
                profileImageUrl += "/api/members/profile-image/" + storedFileName;
            }

            updateProfileRequestDto.setProfileImageUrl(profileImageUrl);

            String imageUrl = currentMember.getImageUrl();
            if (!imageUrl.endsWith("/images/default-profile.png")) {
                String existingImageFileName = imageUrl
                    .substring(backendUrl.length() + "/api/members/profile-image/".length());
                fileStore.deleteFile(existingImageFileName);
            }
        }

        memberService.updateProfile(memberId, updateProfileRequestDto.toMemberEntity());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{memberId}/password")
    public ResponseEntity updatePassword(
        @AuthenticationPrincipal User currentUser, @PathVariable Long memberId,
        @Valid @RequestBody UpdatePasswordRequestDto dto) {

        Member currentMember = memberService.getMemberByEmail(currentUser.getUsername());
        if (!currentMember.getId().equals(memberId)) {
            throw new AccessDeniedException("수정 권한이 없습니다.");
        }

        memberService.updatePassword(memberId, dto.getCurrentPassword(), dto.getNewPassword());

        return ResponseEntity.noContent().build();
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

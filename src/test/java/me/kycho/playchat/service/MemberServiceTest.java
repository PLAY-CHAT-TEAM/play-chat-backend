package me.kycho.playchat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import me.kycho.playchat.domain.Member;
import me.kycho.playchat.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MemberServiceTest {

    @MockBean
    MemberRepository memberRepository;

    @Autowired
    MemberService memberService;

    @Test
    @DisplayName("회원 가입 테스트")
    void joinTest() {
        // given
        Member member = Member.builder()
            .email("member@email.com")
            .password("password")
            .name("name")
            .imageUrl("image_url")
            .build();

        Member savedMember = Member.builder()
            .id(1L)
            .email(member.getEmail())
            .password(member.getPassword())
            .name(member.getName())
            .imageUrl(member.getImageUrl())
            .build();
        given(memberRepository.save(member)).willReturn(savedMember);

        // when
        Member joinResultMember = memberService.join(member);

        // then
        assertThat(joinResultMember).usingRecursiveComparison().isEqualTo(savedMember);
    }

    @Test
    @DisplayName("회원 가입 시 중복된 email이면 에러발생")
    void joinErrorTest_duplicated_email() {
        // given
        Member member = Member.builder()
            .email("member@email.com")
            .password("password")
            .name("name")
            .imageUrl("image_url")
            .build();

        Member savedMember = Member.builder()
            .id(1L)
            .email(member.getEmail())
            .password(member.getPassword())
            .name(member.getName())
            .imageUrl(member.getImageUrl())
            .build();

        given(memberRepository.findByEmail(member.getEmail())).willReturn(Optional.of(savedMember));

        // when & then
        assertThrows(IllegalStateException.class, () -> {
            memberService.join(member);
        }, "기존에 가입된 email로 가입할 수 없습니다.");
    }
}

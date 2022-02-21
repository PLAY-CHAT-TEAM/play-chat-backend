package me.kycho.playchat.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.persistence.EntityManager;
import me.kycho.playchat.domain.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;

@DataJpaTest
class MemberRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;

    @Test
    @DisplayName("member 저장 및 조회 테스트")
    void saveTest() throws NotFoundException {
        // given
        String email = "member@email.com";
        String password = "password";
        String name = "member";
        String imageUrl = "image_url";

        // when
        Member newMember = createMember(email, password, name, imageUrl);
        Member savedMember = memberRepository.save(newMember);
        em.flush();
        em.clear();

        Member findMember = memberRepository.findById(savedMember.getId())
            .orElseThrow(NotFoundException::new);

        // then
        assertThat(findMember.getId()).isGreaterThan(0);
        assertThat(findMember.getEmail()).isEqualTo(email);
        assertThat(findMember.getPassword()).isEqualTo(password);
        assertThat(findMember.getName()).isEqualTo(name);
        assertThat(findMember.getImageUrl()).isEqualTo(imageUrl);
    }

    @DisplayName("member 저장시 누락된 정보 있을 경우 에러 발생")
    @ParameterizedTest(name = "{index}: {0} 누락 테스트")
    @CsvSource({
        "email     ,                 , password, name, image_url",
        "password  , member@email.com,         , name, image_url",
        "name      , member@email.com, password,     , image_url",
        "imageUrl  , member@email.com, password, name,          "
    })
    void saveErrorTest_insufficient_data(
        String target, String email, String password, String name, String imageUrl) {

        // given
        Member member = createMember(email, password, name, imageUrl);

        // when & then
        assertThrows(DataIntegrityViolationException.class, () -> {
            memberRepository.save(member);
        }, target + " 값은 필수 입니다.");
    }

    @Test
    @DisplayName("member 저장시 중복된 email 에러 발생")
    void savedErrorTest_duplicated_email() {
        // given
        String duplicatedEmail = "member@email.com";
        Member member = createMember(duplicatedEmail, "password", "name", "image_url");
        memberRepository.save(member);
        em.flush();
        em.clear();

        // when & then
        Member duplicatedEmailMember =
            createMember(duplicatedEmail, "diff_password", "diff_name", "diff_image_url");

        assertThrows(DataIntegrityViolationException.class, () -> {
            memberRepository.save(duplicatedEmailMember);
        }, "email 값은 중복을 허용하지 않습니다.");
    }

    @Test
    @DisplayName("email로 Member 조회")
    void findByEmailTest() throws NotFoundException {
        // given
        List<Member> members = createMembers(1);
        memberRepository.saveAll(members);

        String email = "member@email.com";
        String password = "password";
        String name = "member";
        String imageUrl = "image_url";
        Member newMember = createMember(email, password, name, imageUrl);
        memberRepository.save(newMember);

        em.flush();
        em.clear();

        // when
        Member findMember = memberRepository.findByEmail(email)
            .orElseThrow(NotFoundException::new);

        // then
        assertThat(findMember.getId()).isGreaterThan(0);
        assertThat(findMember.getEmail()).isEqualTo(email);
        assertThat(findMember.getPassword()).isEqualTo(password);
        assertThat(findMember.getName()).isEqualTo(name);
        assertThat(findMember.getImageUrl()).isEqualTo(imageUrl);
    }


    private Member createMember(String email, String password, String name, String imageUrl) {
        return Member.builder()
            .email(email)
            .password(password)
            .name(name)
            .imageUrl(imageUrl)
            .build();
    }

    private List<Member> createMembers(int memberNum) {
        return IntStream.rangeClosed(1, memberNum).mapToObj((idx) -> {
            System.out.println("idx = " + idx);
            String email = "member" + idx + "@email.com";
            String password = "pass";
            String name = "member" + idx;
            String imageUrl = "image_url" + idx;
            return createMember(email, password, name, imageUrl);
        }).collect(Collectors.toList());
    }
}

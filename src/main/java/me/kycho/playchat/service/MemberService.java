package me.kycho.playchat.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import me.kycho.playchat.domain.Member;
import me.kycho.playchat.exception.DuplicatedEmailException;
import me.kycho.playchat.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public Member join(Member member) {
        validateDuplicateMember(member);
        return memberRepository.save(member);
    }

    private void validateDuplicateMember(Member member) {
        Optional<Member> findMember = memberRepository.findByEmail(member.getEmail());
        if (findMember.isPresent()) {
            throw new DuplicatedEmailException();
        }
    }
}

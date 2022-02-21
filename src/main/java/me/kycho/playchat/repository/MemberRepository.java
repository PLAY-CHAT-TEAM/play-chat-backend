package me.kycho.playchat.repository;

import java.util.Optional;
import me.kycho.playchat.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);
}

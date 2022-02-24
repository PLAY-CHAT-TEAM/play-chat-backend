package me.kycho.playchat.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.kycho.playchat.controller.dto.SignInRequestDto;
import me.kycho.playchat.domain.Member;
import me.kycho.playchat.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("인증 요청 테스트 정상")
    void authenticate() throws Exception {

        // given
        String email = "kycho@naver.com";
        String password = "password";

        Member member = Member.builder()
            .email(email)
            .password(passwordEncoder.encode(password))
            .name("kycho")
            .imageUrl("image_url")
            .build();
        memberRepository.save(member);

        SignInRequestDto signInRequestDto = new SignInRequestDto(email, password);

        // when & then
        mockMvc.perform(
            post("/api/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signInRequestDto))
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("token").exists())
        ;
    }
}

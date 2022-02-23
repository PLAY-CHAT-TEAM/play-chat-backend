package me.kycho.playchat.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileInputStream;
import me.kycho.playchat.common.FileStore;
import me.kycho.playchat.domain.Member;
import me.kycho.playchat.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MemberControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    FileStore fileStore;

    @Test
    @DisplayName("회원가입 테스트 정상")
    void joinTest() throws Exception {

        // given
        String email = "member@email.com";
        String name = "member";
        String password = "password";
        MockMultipartFile profileImage = new MockMultipartFile(
            "profileImage", "imageForTest.png", MediaType.IMAGE_PNG_VALUE,
            new FileInputStream("./src/test/resources/static/imageForTest.png")
        );

        given(fileStore.storeFile(profileImage)).willReturn("storeFileName");

        // when & then
        mockMvc.perform(
                multipart("/api/members/join")
                    .file(profileImage)
                    .param("email", email)
                    .param("name", name)
                    .param("password", password)
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                    .accept(MediaType.APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("email").value(email))
            .andExpect(jsonPath("name").value(name))
        ;
    }

    @Test
    @DisplayName("회원가입 테스트 ERROR(이메일 중복)")
    void joinErrorTest_duplicatedEmail() throws Exception {

        // given
        String duplicatedEmail = "member@email.com";
        memberRepository.save(Member.builder()
            .email(duplicatedEmail)
            .name("member")
            .password("password")
            .imageUrl("image_url")
            .build());

        MockMultipartFile profileImage = new MockMultipartFile(
            "profileImage", "imageForTest.png", MediaType.IMAGE_PNG_VALUE,
            new FileInputStream("./src/test/resources/static/imageForTest.png")
        );
        given(fileStore.storeFile(profileImage)).willReturn("storeFileName");

        // when & then
        mockMvc.perform(
                multipart("/api/members/join")
                    .file(profileImage)
                    .param("email", duplicatedEmail)
                    .param("name", "name")
                    .param("password", "password")
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                    .accept(MediaType.APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().isConflict())
            .andExpect(jsonPath("status").value(HttpStatus.CONFLICT.value()))
            .andExpect(jsonPath("message").value("Duplicated Email."))
        ;
    }
}

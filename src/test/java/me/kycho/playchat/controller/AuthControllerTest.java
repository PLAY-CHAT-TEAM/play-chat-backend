package me.kycho.playchat.controller;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import me.kycho.playchat.controller.dto.SignInRequestDto;
import me.kycho.playchat.domain.Member;
import me.kycho.playchat.repository.MemberRepository;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.operation.preprocess.ContentModifier;
import org.springframework.restdocs.operation.preprocess.ContentModifyingOperationPreprocessor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@AutoConfigureRestDocs
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
    void signIn() throws Exception {

        // given
        String email = "member@naver.com";
        String password = "password";

        Member member = Member.builder()
            .email(email)
            .password(passwordEncoder.encode(password))
            .nickname("kycho")
            .imageUrl("image_url")
            .build();
        memberRepository.save(member);

        SignInRequestDto signInRequestDto = new SignInRequestDto(email, password);

        // when & then
        mockMvc.perform(
                post("/api/auth/sign-in")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signInRequestDto))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("token").exists())
            .andDo(
                document("auth-signIn",
                    preprocessRequest(
                        prettyPrint()
                    ),
                    preprocessResponse(
                        new ContentModifyingOperationPreprocessor(JWTContentModifier()),
                        prettyPrint()
                    ),
                    requestHeaders(
                        headerWithName(HttpHeaders.CONTENT_TYPE)
                            .description("요청 메시지의 콘텐츠 타입 +" + "\n" + MediaType.APPLICATION_JSON),
                        headerWithName(HttpHeaders.ACCEPT)
                            .description("응답받을 콘텐츠 타입 +" + "\n" + MediaType.APPLICATION_JSON)
                    ),
                    requestFields(
                        fieldWithPath("email").description("인증을 위한 이메일 (필수)"),
                        fieldWithPath("password").description("인증을 위한 비밀번호 (필수)")
                    ),
                    responseFields(
                        fieldWithPath("token").description("인증이 필요한 리소스 요청에 사용가능한 JWT 토큰")
                    )
                )
            );
    }

    private ContentModifier JWTContentModifier() {
        return (originalContent, contentType) -> {
            String ogContent = new String(originalContent);
            try {
                JSONObject jsonObject = new JSONObject(ogContent);
                jsonObject.put("token", "XXXXXXX.YYYYYYYYYY.ZZZZZZ");
                String content = jsonObject.toString();
                return content.getBytes(StandardCharsets.UTF_8);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return "<<< ERROR >>>".getBytes(StandardCharsets.UTF_8);
        };
    }
}

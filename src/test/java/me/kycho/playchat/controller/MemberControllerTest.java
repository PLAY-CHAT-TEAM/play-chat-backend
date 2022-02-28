package me.kycho.playchat.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import me.kycho.playchat.common.FileStore;
import me.kycho.playchat.domain.Member;
import me.kycho.playchat.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.operation.OperationRequest;
import org.springframework.restdocs.operation.OperationRequestFactory;
import org.springframework.restdocs.operation.OperationRequestPart;
import org.springframework.restdocs.operation.OperationRequestPartFactory;
import org.springframework.restdocs.operation.preprocess.OperationPreprocessorAdapter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@AutoConfigureRestDocs
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
    void signUpTest() throws Exception {

        // given
        String email = "member@email.com";
        String nickname = "member";
        String password = "aaaaaa1!";
        MockMultipartFile profileImage = new MockMultipartFile(
            "profileImage", "imageForTest.png", MediaType.IMAGE_PNG_VALUE,
            new FileInputStream("./src/test/resources/static/imageForTest.png")
        );

        given(fileStore.storeFile(profileImage)).willReturn("storeFileName");

        // when & then
        mockMvc.perform(
                multipart("/api/members/sign-up")
                    .file(profileImage)
                    .param("email", email)
                    .param("nickname", nickname)
                    .param("password", password)
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                    .accept(MediaType.APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("email").value(email))
            .andExpect(jsonPath("nickname").value(nickname))
            .andDo(
                document("member-signup",
                    preprocessRequest(
                        new PartContentModifyingPreprocessor()
                    ),
                    preprocessResponse(
                        prettyPrint()
                    ),
                    requestHeaders(
                        headerWithName(HttpHeaders.CONTENT_TYPE)
                            .description("요청 메시지의 콘텐츠 타입 +" + "\n" + MediaType.MULTIPART_FORM_DATA),
                        headerWithName(HttpHeaders.ACCEPT)
                            .description("응답받을 콘텐츠 타입 +" + "\n" + MediaType.APPLICATION_JSON)
                    ),
                    requestParameters(
                        parameterWithName("email").description("회원가입에 사용할 이메일 (필수)"),
                        parameterWithName("password").description("회원가입에 사용할 비빌번호 (필수)"),
                        parameterWithName("nickname").description("회원가입에 사용할 닉네임 (필수)")
                    ),
                    requestParts(
                        partWithName("profileImage").description("프로필 사진으로 사용될 이미지 파일 (필수)")
                    ),
                    responseFields(
                        fieldWithPath("email").description("회원가입이 왼료된 회원의 이메일"),
                        fieldWithPath("nickname").description("회원가입이 왼료된 회원의 닉네임")
                    )
                )
            );
    }

    @Test
    @DisplayName("회원가입 테스트 ERROR(이메일 중복)")
    void signUpErrorTest_duplicatedEmail() throws Exception {

        // given
        String duplicatedEmail = "member@email.com";
        memberRepository.save(Member.builder()
            .email(duplicatedEmail)
            .nickname("member")
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
                multipart("/api/members/sign-up")
                    .file(profileImage)
                    .param("email", duplicatedEmail)
                    .param("nickname", "nickname")
                    .param("password", "aaaaaa1!")
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                    .accept(MediaType.APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().isConflict())
            .andExpect(jsonPath("status").value(HttpStatus.CONFLICT.value()))
            .andExpect(jsonPath("message").value("Duplicated Email."))
        ;
    }

    @DisplayName("회원가입 테스트 ERROR(잘못된 이메일)")
    @ParameterizedTest(name = "{index}: 잘못된 이메일 : {0}")
    @NullAndEmptySource
    @ValueSource(strings = {"aaa", "aaa@", "@bbb"})
    void signUpErrorTest_wrongEmail(String wrongEmail) throws Exception {

        // given
        MockMultipartFile profileImage = new MockMultipartFile(
            "profileImage", "imageForTest.png", MediaType.IMAGE_PNG_VALUE,
            new FileInputStream("./src/test/resources/static/imageForTest.png")
        );

        given(fileStore.storeFile(profileImage)).willReturn("storeFileName");

        // when & then
        mockMvc.perform(
                multipart("/api/members/sign-up")
                    .file(profileImage)
                    .param("email", wrongEmail)
                    .param("nickname", "nickname")
                    .param("password", "aaaaaa1!")
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                    .accept(MediaType.APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("status").value(400))
            .andExpect(jsonPath("message").value("Binding Error."))
            .andExpect(jsonPath("fieldErrors[0].field").value("email"))
            .andExpect(jsonPath("fieldErrors[0].defaultMessage").exists())
            .andExpect(jsonPath("fieldErrors[0].rejectedValue").value(wrongEmail))
//            TODO : docs
//            .andDo(
//                document("member-signup-error")
//            )
        ;
    }

    @DisplayName("회원가입 테스트 ERROR(잘못된 비밀번호)")
    @ParameterizedTest(name = "{index}: 잘못된 비밀번호 : {0}")
    @NullAndEmptySource
    @ValueSource(strings = {"aaaa!@2", "ccc@3cccccccccccc", "bbbbbbbb1", "AAAAAAAAA!", "@11111111"})
    void signUpErrorTest_wrongPassword(String wrongPassword) throws Exception {

        // given
        MockMultipartFile profileImage = new MockMultipartFile(
            "profileImage", "imageForTest.png", MediaType.IMAGE_PNG_VALUE,
            new FileInputStream("./src/test/resources/static/imageForTest.png")
        );

        given(fileStore.storeFile(profileImage)).willReturn("storeFileName");

        // when & then
        mockMvc.perform(
                multipart("/api/members/sign-up")
                    .file(profileImage)
                    .param("email", "member@email.com")
                    .param("nickname", "nickname")
                    .param("password", wrongPassword)
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                    .accept(MediaType.APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("status").value(400))
            .andExpect(jsonPath("message").value("Binding Error."))
            .andExpect(jsonPath("fieldErrors[0].field").value("password"))
            .andExpect(jsonPath("fieldErrors[0].defaultMessage").exists())
            .andExpect(jsonPath("fieldErrors[0].rejectedValue").value(wrongPassword))
        ;
    }


    @DisplayName("회원가입 테스트 ERROR(잘못된 닉네임)")
    @ParameterizedTest(name = "{index}: 잘못된 닉네임 : {0}")
    @NullAndEmptySource
    @ValueSource(strings = {"aaaaaaaaaabbbbbbbbbbccccccccccddddddddddeeeeeeeeeef"})
    void signUpErrorTest_wrongNickname(String wrongNickname) throws Exception {

        // given
        MockMultipartFile profileImage = new MockMultipartFile(
            "profileImage", "imageForTest.png", MediaType.IMAGE_PNG_VALUE,
            new FileInputStream("./src/test/resources/static/imageForTest.png")
        );

        given(fileStore.storeFile(profileImage)).willReturn("storeFileName");

        // when & then
        mockMvc.perform(
                multipart("/api/members/sign-up")
                    .file(profileImage)
                    .param("email", "member@email.com")
                    .param("nickname", wrongNickname)
                    .param("password", "aaaaaaaa1!")
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                    .accept(MediaType.APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("status").value(400))
            .andExpect(jsonPath("message").value("Binding Error."))
            .andExpect(jsonPath("fieldErrors[0].field").value("nickname"))
            .andExpect(jsonPath("fieldErrors[0].defaultMessage").exists())
            .andExpect(jsonPath("fieldErrors[0].rejectedValue").value(wrongNickname))
        ;
    }

    static final class PartContentModifyingPreprocessor extends OperationPreprocessorAdapter {

        private final OperationRequestPartFactory partFactory = new OperationRequestPartFactory();
        private final OperationRequestFactory requestFactory = new OperationRequestFactory();

        @Override
        public OperationRequest preprocess(OperationRequest request) {
            List<OperationRequestPart> parts = new ArrayList<>();
            for (OperationRequestPart part : request.getParts()) {
                parts.add(partFactory.create(part.getName(), part.getSubmittedFileName(),
                    "<< binary data >>".getBytes(), part.getHeaders()));
            }
            return requestFactory.create(request.getUri(), request.getMethod(),
                request.getContent(), request.getHeaders(), request.getParameters(), parts);
        }
    }
}

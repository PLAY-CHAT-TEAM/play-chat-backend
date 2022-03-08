package me.kycho.playchat.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import me.kycho.playchat.utils.FileStore;
import me.kycho.playchat.domain.Member;
import me.kycho.playchat.repository.MemberRepository;
import me.kycho.playchat.security.jwt.JwtTokenProvider;
import me.kycho.playchat.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.restdocs.operation.preprocess.ContentModifyingOperationPreprocessor;
import org.springframework.restdocs.operation.preprocess.OperationPreprocessorAdapter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@AutoConfigureRestDocs
@ActiveProfiles("test")
class MemberControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    MemberService memberService;

    @Autowired
    JwtTokenProvider tokenProvider;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    FileStore fileStore;

    @Value("${backend.url}")
    String backendUrl;

    @Value("${file.dir}")
    String uploadFileDir;

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
            .andExpect(status().isCreated())
            .andExpect(jsonPath("email").value(email))
            .andExpect(jsonPath("nickname").value(nickname))
            .andDo(
                document("member-signUp",
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
                        partWithName("profileImage")
                            .description("프로필 사진으로 사용될 이미지 파일 +" + "\n" + "(없으면 기본이미지 사용)")
                    ),
                    responseFields(
                        fieldWithPath("email").description("회원가입이 왼료된 회원의 이메일"),
                        fieldWithPath("nickname").description("회원가입이 왼료된 회원의 닉네임")
                    )
                )
            );
    }

    @Test
    @DisplayName("회원가입 테스트 정상 (프로필이미지 없이)")
    void signUpTest_without_profileImage() throws Exception {

        // given
        String email = "member@email.com";
        String nickname = "member";
        String password = "aaaaaa1!";

        // when & then
        mockMvc.perform(
                multipart("/api/members/sign-up")
                    .param("email", email)
                    .param("nickname", nickname)
                    .param("password", password)
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                    .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("email").value(email))
            .andExpect(jsonPath("nickname").value(nickname));

        Member signedUpMember = memberRepository.findByEmail(email).get();
        assertThat(signedUpMember.getImageUrl()).isEqualTo(
            backendUrl + "/images/default-profile.png");
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

        // when & then
        mockMvc.perform(
                multipart("/api/members/sign-up")
                    .param("email", duplicatedEmail)
                    .param("nickname", "nickname")
                    .param("password", "aaaaaa1!")
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                    .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("status").value(HttpStatus.CONFLICT.value()))
            .andExpect(jsonPath("message").value("이미 등록된 이메일입니다."))
        ;
    }

    @DisplayName("회원가입 테스트 ERROR(잘못된 이메일)")
    @ParameterizedTest(name = "{index}: 잘못된 이메일 : {0}")
    @NullAndEmptySource
    @ValueSource(strings = {"aaa", "aaa@", "@bbb"})
    void signUpErrorTest_wrongEmail(String wrongEmail) throws Exception {

        mockMvc.perform(
                multipart("/api/members/sign-up")
                    .param("email", wrongEmail)
                    .param("nickname", "nickname")
                    .param("password", "aaaaaa1!")
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                    .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("status").value(400))
            .andExpect(jsonPath("message").value("입력 값이 잘못되었습니다."))
            .andExpect(jsonPath("fieldErrors[0].field").value("email"))
            .andExpect(jsonPath("fieldErrors[0].defaultMessage").exists())
            .andExpect(jsonPath("fieldErrors[0].rejectedValue").value(wrongEmail))
//            TODO : docs
//            .andDo(
//                document("member-signUp-error")
//            )
        ;
    }

    @DisplayName("회원가입 테스트 ERROR(잘못된 비밀번호)")
    @ParameterizedTest(name = "{index}: 잘못된 비밀번호 : {0}")
    @NullAndEmptySource
    @ValueSource(strings = {"aaaa!@2", "ccc@3cccccccccccc", "bbbbbbbb1", "AAAAAAAAA!", "@11111111"})
    void signUpErrorTest_wrongPassword(String wrongPassword) throws Exception {

        mockMvc.perform(
                multipart("/api/members/sign-up")
                    .param("email", "member@email.com")
                    .param("nickname", "nickname")
                    .param("password", wrongPassword)
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                    .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("status").value(400))
            .andExpect(jsonPath("message").value("입력 값이 잘못되었습니다."))
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

        mockMvc.perform(
                multipart("/api/members/sign-up")
                    .param("email", "member@email.com")
                    .param("nickname", wrongNickname)
                    .param("password", "aaaaaaaa1!")
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                    .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("status").value(400))
            .andExpect(jsonPath("message").value("입력 값이 잘못되었습니다."))
            .andExpect(jsonPath("fieldErrors[0].field").value("nickname"))
            .andExpect(jsonPath("fieldErrors[0].defaultMessage").exists())
            .andExpect(jsonPath("fieldErrors[0].rejectedValue").value(wrongNickname))
        ;
    }

    @Test
    @DisplayName("내 정보 조회 정상")
    void getMyInfoTest() throws Exception {

        // given
        String email = "kycho@naver.com";
        String nickname = "kycho";
        String imageUrl = "kycho_image_url";
        long id = createMember(email, nickname, imageUrl);
        createMembers(5);

        String token = generateToken(email);

        // when & then
        mockMvc.perform(
                get("/api/members/me")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("id").value(id))
            .andExpect(jsonPath("email").value(email))
            .andExpect(jsonPath("nickname").value(nickname))
            .andExpect(jsonPath("imageUrl").value(imageUrl))
            .andExpect(jsonPath("password").doesNotExist())
            .andDo(
                document("member-me",
                    preprocessRequest(new AuthHeaderModifyingPreprocessor()),
                    preprocessResponse(prettyPrint()),
                    requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION)
                            .description("인증 정보 헤더 +" + "\n" + "Bearer <jwt토큰값>")
                    ),
                    responseFields(
                        fieldWithPath("id").description("내 ID번호"),
                        fieldWithPath("email").description("내 이메일 정보"),
                        fieldWithPath("nickname").description("내 닉네임 정보"),
                        fieldWithPath("imageUrl").description("내 프로필 이미지 URL")
                    )
                )
            );
    }

    @Test
    @DisplayName("id로 회원 조회 정상")
    void getMemberTest() throws Exception {
        // given
        List<Long> ids = createMembers(10);
        Long targetId = ids.get(3);

        // when & then
        mockMvc.perform(
                get("/api/members/{memberId}", targetId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + generateToken())
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("id").value(targetId))
            .andExpect(jsonPath("email").value("member" + targetId + "@email.com"))
            .andExpect(jsonPath("nickname").value("member" + targetId))
            .andExpect(jsonPath("imageUrl").exists())
            .andExpect(jsonPath("password").doesNotExist())
            .andDo(
                document("member-get",
                    preprocessRequest(new AuthHeaderModifyingPreprocessor()),
                    preprocessResponse(prettyPrint()),
                    pathParameters(
                        parameterWithName("memberId").description("조회하려는 회원의 ID번호")
                    ),
                    requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION)
                            .description("인증 정보 헤더 +" + "\n" + "Bearer <jwt토큰값>")
                    ),
                    responseFields(
                        fieldWithPath("id").description("조회된 회원의 ID번호"),
                        fieldWithPath("email").description("조회된 회원의 이메일 정보"),
                        fieldWithPath("nickname").description("조회된 회원의 닉네임 정보"),
                        fieldWithPath("imageUrl").description("조회된 회원의 프로필 이미지 URL")
                    )
                )
            );
    }

    @Test
    @DisplayName("id로 회원 조회 ERROR (인증된 토큰없이 요청)")
    void getMemberErrorTest_withoutAuth() throws Exception {
        // given
        List<Long> ids = createMembers(10);
        Long targetId = ids.get(3);

        // when & then
        mockMvc.perform(get("/api/members/{memberId}", targetId))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("id로 회원 조회 ERROR (존재하지 않는 회원)")
    void getMemberErrorTest_notFound() throws Exception {
        // given
        List<Long> ids = createMembers(10);
        Collections.sort(ids);
        Long targetId = ids.get(ids.size() - 1) + 3;

        // when & then
        mockMvc.perform(
                get("/api/members/{memberId}", targetId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + generateToken())
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("message").value("해당 회원을 찾을 수 없습니다."))
        ;
    }

    @Test
    @DisplayName("id로 회원 조회 ERROR (잘못된 인풋값)")
    void getMemberErrorTest_wrongInput() throws Exception {
        // given
        String targetId = "wrongInput";

        // when & then
        mockMvc.perform(
                get("/api/members/{memberId}", targetId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + generateToken())
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("message").value("잘못된 요청입니다."));
        ;
    }

    @Test
    @DisplayName("회원 전체 조회 정상")
    void getMemberListTest() throws Exception {
        // given
        int memberNum = 3;
        createMembers(memberNum);

        // when & then
        mockMvc.perform(
                get("/api/members/list")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + generateToken())
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(memberNum))
            .andExpect(jsonPath("$[0].id").exists())
            .andExpect(jsonPath("$[0].email").exists())
            .andExpect(jsonPath("$[0].nickname").exists())
            .andExpect(jsonPath("$[0].imageUrl").exists())
            .andExpect(jsonPath("$[0].password").doesNotExist())
            .andDo(
                document("member-getList",
                    preprocessRequest(new AuthHeaderModifyingPreprocessor()),
                    preprocessResponse(prettyPrint()),
                    requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION)
                            .description("인증 정보 헤더 +" + "\n" + "Bearer <jwt토큰값>")
                    ),
                    responseFields(
                        fieldWithPath("[].id").description("조회된 회원의 ID번호"),
                        fieldWithPath("[].email").description("조회된 회원의 이메일 정보"),
                        fieldWithPath("[].nickname").description("조회된 회원의 닉네임 정보"),
                        fieldWithPath("[].imageUrl").description("조회된 회원의 프로필 이미지 URL")
                    )
                )
            )
        ;
    }

    @Test
    @DisplayName("회원 전체 조회 ERROR (인증 토큰 없이 요청)")
    void getMemberListErrorTest_withoutAuth() throws Exception {
        // given
        createMembers(10);

        // when & then
        mockMvc.perform(get("/api/members/list"))
            .andExpect(status().isUnauthorized());
    }

    @DisplayName("회원 프로필 업데이트 ERROR (잘못된 닉네임)")
    @ParameterizedTest(name = "{index}: 잘못된 닉네임 : {0}")
    @ValueSource(strings = {"", "aaaaaaaaaabbbbbbbbbbccccccccccddddddddddeeeeeeeeeef"})
    void updateProfileTest_wrongNickname(String wrongNickname) throws Exception {

        // given
        MockMultipartFile profileImage = new MockMultipartFile(
            "profileImage", "imageForTest.png", MediaType.IMAGE_PNG_VALUE,
            new FileInputStream("./src/test/resources/static/imageForTest.png")
        );
        given(fileStore.storeFile(profileImage)).willReturn("storeFileName");

        // when & then
        mockMvc.perform(
                multipart("/api/members/1/update")
                    .file(profileImage)
                    .param("nickname", wrongNickname)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + generateToken())
                    .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("status").value(400))
            .andExpect(jsonPath("message").value("입력 값이 잘못되었습니다."))
            .andExpect(jsonPath("fieldErrors[0].field").value("nickname"))
            .andExpect(jsonPath("fieldErrors[0].defaultMessage").value("닉네임은 최대 50자까지 가능합니다."))
            .andExpect(jsonPath("fieldErrors[0].rejectedValue").value(wrongNickname))
            .andDo(
                document("member-updateProfile-wrongNicnkane")
            );
    }

    @Test
    @DisplayName("회원 프로필 업데이트 ERROR (업데이트 정보 없음)")
    void updateProfileTest_noUpdateData() throws Exception {

        // when & then
        mockMvc.perform(
                multipart("/api/members/1/update")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + generateToken())
                    .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("status").value(400))
            .andExpect(jsonPath("message").value("수정할 회원정보가 없습니다."))
            .andDo(
                document("member-updateProfile-noUpdateData")
            );
    }

    @Test
    @DisplayName("프로필 이미지 조회 정상")
    void downloadImageTest() throws Exception {

        // given
        String filename = "profileImage.png";
        File file = new File("./src/test/resources/static/imageForTest.png");
        File uploadedFile = new File(uploadFileDir + filename);
        Files.copy(file.toPath(), uploadedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        given(fileStore.getFullPath(filename)).willReturn(uploadFileDir + filename);

        // when & then
        mockMvc.perform(
                get("/api/members/profile-image/" + filename)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + generateToken())
            )
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE))
            .andExpect(content().bytes(Files.readAllBytes(file.toPath())))
            .andDo(
                document("member-profileImage",
                    preprocessRequest(
                        new AuthHeaderModifyingPreprocessor()
                    ),
                    preprocessResponse(
                        new ContentModifyingOperationPreprocessor((originalContent, contentType) ->
                            "<< Image binary data >>".getBytes(StandardCharsets.UTF_8))
                    ),
                    requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION)
                            .description("인증 정보 헤더 +" + "\n" + "Bearer <jwt토큰값>")
                    )
                )
            );

        uploadedFile.delete();
    }

    @Test
    @DisplayName("프로필 이미지 조회 ERROR (인증 토근 없이 요청)")
    void downloadImageErrorTest_withoutAuth() throws Exception {

        // given
        String filename = "profileImage.png";
        File file = new File("./src/test/resources/static/imageForTest.png");
        File uploadedFile = new File(uploadFileDir + filename);
        Files.copy(file.toPath(), uploadedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        given(fileStore.getFullPath(filename)).willReturn(uploadFileDir + filename);

        // when & then
        mockMvc.perform(get("/api/members/profile-image/" + filename))
            .andExpect(status().isUnauthorized());

        uploadedFile.delete();
    }

    @Test
    @DisplayName("프로필 이미지 조회 ERROR(존재하지 않는 이미지)")
    void downloadImageErrorTest_notFound() throws Exception {

        // when & then
        mockMvc.perform(
                get("/api/members/profile-image/noFile.png")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + generateToken())
            )
            .andExpect(status().isNotFound());
    }

    private long createMember(String email, String nickname, String imageUrl) {
        Member member = Member.builder()
            .email(email)
            .password("aaaaaaa1!")
            .nickname(nickname)
            .imageUrl(imageUrl)
            .build();
        Member signedUpMember = memberService.signUp(member);
        return signedUpMember.getId();
    }

    private List<Long> createMembers(int memberNum) {
        List<Long> ids = new ArrayList<>();
        for (int i = 1; i <= memberNum; i++) {
            Member member = Member.builder()
                .email("member" + i + "@email.com")
                .password("aaaaaaa1!")
                .nickname("member" + i)
                .imageUrl("image_url")
                .build();
            Member signedUpMember = memberService.signUp(member);
            ids.add(signedUpMember.getId());
        }
        return ids;
    }

    private String generateToken() {
        return generateToken("test@naver.com");
    }

    private String generateToken(String email) {
        List<GrantedAuthority> authorities = Collections
            .singletonList(new SimpleGrantedAuthority("ROLE_MEMBER"));

        Authentication authentication =
            new UsernamePasswordAuthenticationToken(email, "", authorities);

        return tokenProvider.createToken(authentication);
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

    static final class AuthHeaderModifyingPreprocessor extends OperationPreprocessorAdapter {

        private final OperationRequestFactory requestFactory = new OperationRequestFactory();

        @Override
        public OperationRequest preprocess(OperationRequest request) {
            HttpHeaders headers = new HttpHeaders();
            for (String key : request.getHeaders().keySet()) {
                if (key.equals(HttpHeaders.AUTHORIZATION)) {
                    headers.put(key, Collections.singletonList("Bearer XXXXXXX.YYYYYYYYYY.ZZZZZZ"));
                } else {
                    headers.put(key, Objects.requireNonNull(request.getHeaders().get(key)));
                }
            }

            return requestFactory.create(request.getUri(), request.getMethod(),
                request.getContent(), headers, request.getParameters(), request.getParts());
        }
    }
}

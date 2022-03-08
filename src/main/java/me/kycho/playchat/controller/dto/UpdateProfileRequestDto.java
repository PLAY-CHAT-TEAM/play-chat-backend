package me.kycho.playchat.controller.dto;

import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileRequestDto {

    @Size(min = 1, max = 50, message = "닉네임은 최대 50자까지 가능합니다.")
    private String nickname;

    private MultipartFile profileImage;

}

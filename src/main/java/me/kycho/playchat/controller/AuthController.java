package me.kycho.playchat.controller;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.kycho.playchat.controller.dto.SignInRequestDto;
import me.kycho.playchat.controller.dto.TokenDto;
import me.kycho.playchat.jwt.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    @PostMapping("/api/authenticate")
    public ResponseEntity<TokenDto> authenticate(@Valid @RequestBody SignInRequestDto signInDto) {

        Authentication authenticationToken =
            new UsernamePasswordAuthenticationToken(signInDto.getEmail(), signInDto.getPassword());

        Authentication authentication =
            authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        String jwt = tokenProvider.createToken(authentication);

        return ResponseEntity.ok(new TokenDto(jwt));
    }
}

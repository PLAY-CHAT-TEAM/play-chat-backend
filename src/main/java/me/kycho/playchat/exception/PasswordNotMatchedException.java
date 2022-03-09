package me.kycho.playchat.exception;

public class PasswordNotMatchedException extends IllegalArgumentException {

    @Override
    public String getMessage() {
        return "현재 비밀번호가 일치하지 않습니다.";
    }
}

package me.kycho.playchat.exception;

public class DuplicatedEmailException extends IllegalArgumentException {

    @Override
    public String getMessage() {
        return "이미 등록된 이메일입니다.";
    }
}

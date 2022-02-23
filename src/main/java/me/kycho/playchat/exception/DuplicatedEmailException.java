package me.kycho.playchat.exception;

public class DuplicatedEmailException extends IllegalArgumentException {

    @Override
    public String getMessage() {
        return "Duplicated Email.";
    }
}

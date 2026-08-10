package dev.orderflow.auth;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("The email or password is incorrect.");
    }
}

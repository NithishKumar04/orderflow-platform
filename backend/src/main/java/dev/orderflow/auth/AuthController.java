package dev.orderflow.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final String demoEmail;
    private final String encodedPassword;
    private final String displayName;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(
            @Value("${orderflow.auth.demo-email}") String demoEmail,
            @Value("${orderflow.auth.demo-password}") String demoPassword,
            @Value("${orderflow.auth.display-name}") String displayName,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.demoEmail = demoEmail;
        this.passwordEncoder = passwordEncoder;
        this.encodedPassword = passwordEncoder.encode(demoPassword);
        this.displayName = displayName;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        if (!demoEmail.equalsIgnoreCase(request.email())
                || !passwordEncoder.matches(request.password(), encodedPassword)) {
            throw new InvalidCredentialsException();
        }
        return new LoginResponse(jwtService.issue(demoEmail, displayName), demoEmail, displayName);
    }

    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {
    }

    public record LoginResponse(String token, String email, String displayName) {
    }
}

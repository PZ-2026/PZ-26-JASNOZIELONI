package pl.edu.ur.coopspace_backend.controller;

import pl.edu.ur.coopspace_backend.dto.AuthResponse;
import pl.edu.ur.coopspace_backend.dto.LoginRequest;
import pl.edu.ur.coopspace_backend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Handles authentication-related endpoints such as login and registration.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final AuthService authService;
    
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    /**
     * Authenticates a user with provided credentials and returns a JWT.
     *
     * @param request the login credentials (email and password)
     * @return an authentication response containing a token and user info
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}

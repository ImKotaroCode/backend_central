package backend_central.backend_central.service;

import backend_central.backend_central.dto.LoginRequest;
import backend_central.backend_central.dto.LoginResponse;
import backend_central.backend_central.repository.AdminRepository;
import backend_central.backend_central.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        var admin = adminRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.getPassword(), admin.getPasswordHash())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        return new LoginResponse(jwtUtil.generateToken(admin.getEmail()));
    }
}

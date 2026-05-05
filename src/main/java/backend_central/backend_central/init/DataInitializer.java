package backend_central.backend_central.init;

import backend_central.backend_central.entity.Admin;
import backend_central.backend_central.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.default.nombre:Admin}")
    private String nombre;

    @Value("${admin.default.email:admin@backend.com}")
    private String email;

    @Value("${admin.default.password:admin123}")
    private String password;

    @Override
    public void run(String... args) {
        if (adminRepository.count() == 0) {
            Admin admin = new Admin();
            admin.setNombre(nombre);
            admin.setEmail(email);
            admin.setPasswordHash(passwordEncoder.encode(password));
            adminRepository.save(admin);
            log.info("Admin por defecto creado: {}", email);
        }
    }
}

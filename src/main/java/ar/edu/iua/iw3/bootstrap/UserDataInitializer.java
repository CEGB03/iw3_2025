package ar.edu.iua.iw3.bootstrap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

import ar.edu.iua.iw3.model.auth.UserAccount;
import ar.edu.iua.iw3.model.persistence.UserAccountRepository;
import ar.edu.iua.iw3.security.Role;
import lombok.extern.slf4j.Slf4j;

@Component
@Order(10)
@Slf4j
public class UserDataInitializer implements ApplicationRunner {

    @Autowired
    private UserAccountRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        createIfNotExists("admin", "admin123", Role.ADMIN);
        createIfNotExists("operator", "operator123", Role.OPERADOR);
        createIfNotExists("viewer", "viewer123", Role.VISOR);
    }

    private void createIfNotExists(String username, String rawPassword, Role role) {
        if (userRepo.existsByUsername(username)) {
            return;
        }
        UserAccount u = new UserAccount();
        u.setUsername(username);
        u.setPasswordHash(passwordEncoder.encode(rawPassword));
        u.setRole(role);
        u.setEnabled(true);
        userRepo.save(u);
        log.info("Usuario {} creado (rol: {})", username, role);
    }
}

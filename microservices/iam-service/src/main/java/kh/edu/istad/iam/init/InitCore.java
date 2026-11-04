package kh.edu.istad.iam.init;

import jakarta.annotation.PostConstruct;
import kh.edu.istad.iam.config.PasswordConfig;
import kh.edu.istad.iam.domain.Permission;
import kh.edu.istad.iam.domain.Role;
import kh.edu.istad.iam.domain.User;
import kh.edu.istad.iam.feature.permission.PermissionJpaRepository;
import kh.edu.istad.iam.feature.role.RoleJpaRepository;
import kh.edu.istad.iam.feature.user.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class InitCore {
    private final PermissionJpaRepository jpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final RoleJpaRepository roleJpaRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        Permission permission = Permission.builder()
                .permission("user:read:own")
                .description("Read All Users")
                .build();
        Role role = Role.builder().role("ADMIN").description("Admin Tas Bro").build();

        User user = User.builder()
                .familyName("Taing")
                .givenName("Sengkim")
                .email("sengkim@gmail.com")
                .gender("Male")
                .emailVerified(true)
                .username("kim")
                .password(passwordEncoder.encode("qwer"))
        .build();

        if(!userJpaRepository.existsByUsername("kim")) {

        }

    }

}

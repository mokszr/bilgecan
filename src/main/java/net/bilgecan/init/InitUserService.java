package net.bilgecan.init;

import net.bilgecan.config.RoleConstant;
import net.bilgecan.entity.security.Role;
import net.bilgecan.entity.security.User;
import net.bilgecan.repository.RoleRepository;
import net.bilgecan.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class InitUserService implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {

        if (userRepository.count() > 0) {
            return;
        }
        User root = new User();
        root.setUsername("root");
        root.setEmail("root@bilgecan.net");
        root.setPassword(passwordEncoder.encode("root"));
        root.setEnabled(true);

        userRepository.save(root);

        Role roleUser = new Role();
        roleUser.setName(RoleConstant.ROLE_USER);

        Role roleAdmin = new Role();
        roleAdmin.setName(RoleConstant.ROLE_ADMIN);

        roleRepository.save(roleUser);
        roleRepository.save(roleAdmin);

        root.getRoles().add(roleUser);
        root.getRoles().add(roleAdmin);

        userRepository.save(root);
    }
}

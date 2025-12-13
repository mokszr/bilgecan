package net.bilgecan.service;

import net.bilgecan.config.ConstantConfigs;
import net.bilgecan.dto.UserDto;
import net.bilgecan.entity.security.Role;
import net.bilgecan.entity.security.User;
import net.bilgecan.exception.AppLevelValidationException;
import net.bilgecan.repository.RoleRepository;
import net.bilgecan.repository.UserRepository;
import com.vaadin.flow.component.notification.Notification;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Transactional(rollbackFor = Exception.class)
@Service
public class CustomUserDetailsService implements UserDetailsService, UserService {

    private final UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private TranslationService translations;
    private RoleRepository roleRepository;
    private SecurityService securityService;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @Override
    public UserDetails loadUserByUsername(String input) throws UsernameNotFoundException {
        return userRepository.findByUsernameOrEmail(input, input)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with input: " + input));
    }


    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public boolean saveUser(UserDto userDto) {

        validateUser(userDto, false);
        User user = toEntity(userDto);

        Optional<User> existingUser = userRepository.findByUsernameIgnoreCase(user.getUsername());
        if (existingUser.isPresent()) {
            // Username already exists
            throw new AppLevelValidationException(translations.t("signup.error.userAlreadyExist"), Notification.Position.MIDDLE);
        }
        existingUser = userRepository.findByEmailIgnoreCase(user.getEmail());
        if (existingUser.isPresent()) {
            // Email already exists
            throw new AppLevelValidationException(translations.t("signup.error.userAlreadyExist"), Notification.Position.MIDDLE);
        }

        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setEnabled(true);

        Set<Role> roles = new HashSet<>();
        Set<String> rolesSelected = userDto.getRoles();
        for (String roleSelected : rolesSelected) {
            Role role = roleRepository.findByName(roleSelected);
            roles.add(role);
        }

        user.setRoles(roles);

        userRepository.save(user);
        return true;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public boolean updateUser(UserDto userDto) {

        validateUser(userDto, true);
        User persistedUser = userRepository.findById(userDto.getId()).get();

        // if username is changing, check if already exists
        if (!persistedUser.getUsername().equals(userDto.getUsername())) {
            Optional<User> existingUser = userRepository.findByUsernameIgnoreCase(userDto.getUsername());
            if (existingUser.isPresent()) {
                // Username already exists
                throw new AppLevelValidationException(translations.t("signup.error.userAlreadyExist"), Notification.Position.MIDDLE);
            }
        }

        // if email is changing, check if already exists
        if (!persistedUser.getEmail().equals(userDto.getEmail())) {
            Optional<User> existingUser = userRepository.findByEmailIgnoreCase(userDto.getEmail());
            if (existingUser.isPresent()) {
                // Email already exists
                throw new AppLevelValidationException(translations.t("signup.error.userAlreadyExist"), Notification.Position.MIDDLE);
            }
        }

        persistedUser.setUsername(userDto.getUsername());
        persistedUser.setEmail(userDto.getEmail());

        Set<Role> roles = new HashSet<>();
        Set<String> rolesSelected = userDto.getRoles();
        for (String roleSelected : rolesSelected) {
            Role role = roleRepository.findByName(roleSelected);
            roles.add(role);
        }

        persistedUser.setRoles(roles);
        userRepository.save(persistedUser);
        return true;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public void resetPassword(UserDto userDto, String newPassword) {
        validatePassword(newPassword);
        User user = userRepository.findById(userDto.getId()).get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public void resetCurrentUserPassword(String oldPassword, String newPassword) {

        validatePassword(newPassword);

        User currentUser = securityService.getCurrentUser();
        // Check old password
        if (!passwordEncoder.matches(oldPassword, currentUser.getPassword())) {
            throw new AppLevelValidationException(translations.t("passwordReset.oldPasswordIsIncorrect"));
        }

        currentUser.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(currentUser);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public void delete(UserDto userDto) {
        long countEnabled = userRepository.countByEnabled(true);
        if (countEnabled <= 1 && userDto.isEnabled()) {
            throw new AppLevelValidationException(translations.t("users.notEnoughEnabledUserRemains"));
        }
        Long currentUserId = securityService.getCurrentUser().getId();
        if (currentUserId.equals(userDto.getId())) {
            throw new AppLevelValidationException(translations.t("users.cannotDeleteYourself"));
        }
        userRepository.deleteById(userDto.getId());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public void enableDisable(UserDto userDto, boolean enableOrDisable) {
        long countEnabled = userRepository.countByEnabled(true);
        if (!enableOrDisable && countEnabled <= 1 && userDto.isEnabled()) {
            throw new AppLevelValidationException(translations.t("users.notEnoughEnabledUserRemains"));
        }
        Long currentUserId = securityService.getCurrentUser().getId();
        if (currentUserId.equals(userDto.getId())) {
            throw new AppLevelValidationException(translations.t("users.cannotDisableYourself"));
        }
        User user = userRepository.findById(userDto.getId()).get();
        user.setEnabled(enableOrDisable);
        userRepository.save(user);
    }

    private User toEntity(UserDto dto) {
        User entity = new User();
        entity.setEmail(dto.getEmail());
        entity.setPassword(dto.getPassword());
        entity.setUsername(dto.getUsername());

        return entity;
    }

    private void validateUser(UserDto user, boolean skipPassword) {
        String username = user.getUsername();
        String email = user.getEmail();
        String password = user.getPassword();

        if (StringUtils.isBlank(username) || StringUtils.isBlank(email)) {
            throw new AppLevelValidationException("empty values are given");
        }

        if (username.length() < ConstantConfigs.MIN_USERNAME_LENGTH
                || username.length() > ConstantConfigs.MAX_USERNAME_LENGTH
                || !ConstantConfigs.USERNAME_POLICY.matcher(username).matches()) {
            throw new AppLevelValidationException("invalid username");
        }

        if (!skipPassword) {
            validatePassword(password);
        }

        if (!ConstantConfigs.EMAIL_POLICY.matcher(email).matches()) {
            throw new AppLevelValidationException("invalid email");
        }

    }

    private static void validatePassword(String password) {
        if (StringUtils.isBlank(password)) {
            throw new AppLevelValidationException("empty values are given");
        }
        if (password.length() < ConstantConfigs.MIN_PASSWORD_LENGTH
                || password.length() > ConstantConfigs.MAX_PASSWORD_LENGTH
                || !ConstantConfigs.PASSWORD_POLICY.matcher(password).matches()) {
            throw new AppLevelValidationException("invalid password");
        }
    }

    @Override
    public void updatePreferredLanguage(User user, Locale preferredLocale) {
        user.setLocale(preferredLocale);
        userRepository.save(user);
    }

    @Override
    public Page<UserDto> findPaginated(Pageable pageable, String searchTerm) {

        if (StringUtils.isBlank(searchTerm)) {
            return mapToDto(userRepository.findAll(pageable));
        }

        return mapToDto(userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(searchTerm, searchTerm, pageable));
    }

    private Page<UserDto> mapToDto(Page<User> page) {
        return page.map(this::toDto);
    }

    private UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setDateCreated(user.getDateCreated());
        dto.setLastUpdated(user.getLastUpdated());
        dto.setUsername(user.getUsername());
        dto.setEnabled(user.isEnabled());
        dto.setRoles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));

        return dto;
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    public void setTranslations(TranslationService translations) {
        this.translations = translations;
    }

    @Autowired
    public void setRoleRepository(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Autowired
    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }
}
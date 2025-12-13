package net.bilgecan.service;


import net.bilgecan.dto.UserDto;
import net.bilgecan.entity.security.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Locale;

public interface UserService {
    boolean saveUser(UserDto user);

    boolean updateUser(UserDto userDto);

    void updatePreferredLanguage(User user, Locale preferredLocale);

    Page<UserDto> findPaginated(Pageable pageable, String searchTerm);

    void resetPassword(UserDto userDto, String newPassword);

    void resetCurrentUserPassword(String oldPassword, String newPassword);

    void delete(UserDto userDto);

    void enableDisable(UserDto userDto, boolean enableOrDisable);
}

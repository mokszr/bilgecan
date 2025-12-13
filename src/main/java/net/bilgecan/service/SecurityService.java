package net.bilgecan.service;

import net.bilgecan.entity.security.Role;
import net.bilgecan.entity.security.User;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinServletRequest;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SecurityService {

    private static final String LOGOUT_SUCCESS_URL = "/";

    public UserDetails getAuthenticatedUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        Object principal = context.getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return (UserDetails) context.getAuthentication().getPrincipal();
        }
        // Anonymous or no authentication.
        return null;
    }

    public User getCurrentUser() {
        UserDetails authenticatedUser = getAuthenticatedUser();
        if (authenticatedUser instanceof User) {
            return (User) authenticatedUser;
        }
        return null;
    }

    public Set<String> getCurrentUserRoles() {
        User currentUser = getCurrentUser();
        if(currentUser == null) {
            return Collections.emptySet();
        }

        Set<String> roles = currentUser.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        return roles;
    }

    public void logout() {
        UI.getCurrent().getPage().setLocation(LOGOUT_SUCCESS_URL);
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(
                VaadinServletRequest.getCurrent().getHttpServletRequest(), null,
                null);
    }
}
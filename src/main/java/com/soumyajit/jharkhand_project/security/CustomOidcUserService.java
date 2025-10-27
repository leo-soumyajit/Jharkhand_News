package com.soumyajit.jharkhand_project.security;

import com.soumyajit.jharkhand_project.entity.User;
import com.soumyajit.jharkhand_project.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class CustomOidcUserService extends OidcUserService {

    private final UserRepository userRepository;

    public CustomOidcUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        log.error("!!!! CustomOidcUserService CONSTRUCTOR CALLED !!!!");
    }

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        log.error("!!!! CustomOidcUserService.loadUser CALLED !!!!");

        // Delegate to the default OIDC user service to get the OidcUser
        OidcUser oidcUser = super.loadUser(userRequest);

        String email = oidcUser.getEmail();
        String firstName = oidcUser.getGivenName();
        String lastName = oidcUser.getFamilyName();
        String picture = oidcUser.getPicture();

        log.error("!!!! OIDC login attempt for email: {} !!!!", email);

        // Find or create the user in the database
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            log.error("!!!! CREATING NEW USER for email: {} !!!!", email);

            User newUser = User.builder()
                    .email(email)
                    .firstName(firstName != null ? firstName : "")
                    .lastName(lastName != null ? lastName : "")
                    .profileImageUrl(picture)
                    .password(null) // No password for OAuth users
                    .authProvider(User.AuthProvider.GOOGLE)
                    .emailVerified(true)
                    .role(User.Role.USER)
                    .build();

            userRepository.saveAndFlush(newUser);
            log.error("!!!! New user SAVED: {} !!!!", email);
            return newUser;
        });

        log.error("!!!! User processing complete for: {} !!!!", email);

        // The success handler will use this oidcUser principal
        return oidcUser;
    }
}

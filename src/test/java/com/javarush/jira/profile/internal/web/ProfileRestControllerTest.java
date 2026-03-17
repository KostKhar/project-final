package com.javarush.jira.profile.internal.web;

import com.javarush.jira.AbstractControllerTest;
import com.javarush.jira.login.AuthUser;
import com.javarush.jira.login.Role;
import com.javarush.jira.login.User;
import com.javarush.jira.profile.internal.ProfileRepository;
import com.javarush.jira.profile.internal.model.Profile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

class ProfileRestControllerTest extends AbstractControllerTest {


    @MockBean
    private ProfileRepository profileRepository;

    @Test
    void getProfileByAuthUser() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setDisplayName("John Doe");
        user.setEmail("john@example.com");
        user.setPassword("password");
        user.setRoles(List.of(Role.ADMIN));

        AuthUser authUser = new AuthUser(user);

        Profile profile = new Profile(user.id());


        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(authUser, null, authUser.getAuthorities());

        when(profileRepository.getOrCreate(user.id())).thenReturn(profile);

        perform(get("/api/profile")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getProfileByNotAuthUser_returnError() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setDisplayName("John Doe");
        user.setEmail("john@example.com");
        user.setPassword("password");
        user.setRoles(List.of(Role.ADMIN));

        Profile profile = new Profile(user.id());

        when(profileRepository.getOrCreate(user.id())).thenReturn(profile);

        perform(get("/api/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.detail").value("Full authentication is required to access this resource"))
                .andExpect(jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()));
    }


}
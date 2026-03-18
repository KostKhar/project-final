package com.javarush.jira.profile.internal.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javarush.jira.AbstractControllerTest;
import com.javarush.jira.login.AuthUser;
import com.javarush.jira.login.Role;
import com.javarush.jira.login.User;
import com.javarush.jira.profile.ContactTo;
import com.javarush.jira.profile.ProfileTo;
import com.javarush.jira.profile.internal.ProfileMapper;
import com.javarush.jira.profile.internal.ProfileRepository;
import com.javarush.jira.profile.internal.model.Profile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

class ProfileRestControllerTest extends AbstractControllerTest {

    @MockBean
    private ProfileRepository profileRepository;

    @MockBean
    private ProfileMapper profileMapper;

    @Autowired
    private ObjectMapper objectMapper;

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
        verify(profileRepository).getOrCreate(authUser.id());
        verify(profileMapper).updateFromTo(any(Profile.class), any(ProfileTo.class));
        verify(profileRepository).save(any(Profile.class));
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
                .andExpect(jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    void checkUserUpdateProfileWithoutAuth() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setDisplayName("John Doe");
        user.setEmail("john@example.com");
        user.setPassword("password");
        user.setRoles(List.of(Role.DEV));


        User admin = new User();
        admin.setId(5L);
        user.setFirstName("Tom");
        user.setLastName("Khan");
        user.setDisplayName("Tom Khan");
        user.setEmail("admin@example.com");
        user.setPassword("password");
        user.setRoles(List.of(Role.ADMIN));


        ProfileTo profileTo = new ProfileTo(2L, Set.of("google"), Set.of(new ContactTo("1111", "neworg@gmail.com")));

        String jsonBody = new ObjectMapper().writeValueAsString(profileTo);

        perform(put("/api/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Unauthorized"));
    }

    @Test
    void checkAdminUpdateProfile() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setDisplayName("John Doe");
        user.setEmail("john@example.com");
        user.setPassword("password");
        user.setRoles(List.of(Role.DEV));


        User admin = new User();
        admin.setId(5L);
        user.setFirstName("Tom");
        user.setLastName("Khan");
        user.setDisplayName("Tom Khan");
        user.setEmail("admin@example.com");
        user.setPassword("password");
        user.setRoles(List.of(Role.ADMIN));

        AuthUser authUser = new AuthUser(admin);


        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(authUser, null, authUser.getAuthorities());

        Profile profile = new Profile(user.id());

        ProfileTo profileTo = new ProfileTo(2L, null, null);

        String jsonBody = objectMapper.writeValueAsString(profileTo);

        when(profileRepository.getOrCreate(user.id())).thenReturn(profile);
        when(profileMapper.updateFromTo(profileRepository.getOrCreate(profileTo.id()), profileTo)).thenReturn(profile);

        perform(put("/api/profile")
                .with(SecurityMockMvcRequestPostProcessors.authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isNoContent());

        verify(profileRepository).getOrCreate(user.id());
        verify(profileMapper).updateFromTo(profileRepository.getOrCreate(profileTo.id()), profileTo);
        verify(profileRepository).save(profile);
    }





}
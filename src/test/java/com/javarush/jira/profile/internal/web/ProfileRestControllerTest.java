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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        when(profileMapper.toTo(profile)).thenReturn(new ProfileTo(user.id(), null, null));

        perform(get("/api/profile")
                .with(SecurityMockMvcRequestPostProcessors.authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(profileRepository).getOrCreate(authUser.id());
        verify(profileMapper).toTo(profile);
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

        perform(get("/api/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    void checkUserUpdateProfileWithoutAuth() throws Exception {
        User admin = new User();
        admin.setId(5L);
        admin.setFirstName("Tom");
        admin.setLastName("Khan");
        admin.setDisplayName("Tom Khan");
        admin.setEmail("admin@example.com");
        admin.setPassword("password");
        admin.setRoles(List.of(Role.ADMIN));

        ProfileTo profileTo = new ProfileTo(5L, Set.of("google"), Set.of(new ContactTo("1111", "neworg@gmail.com")));

        String jsonBody = objectMapper.writeValueAsString(profileTo);

        perform(put("/api/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Unauthorized"));
    }

    @Test
    void checkManagerUpdateProfile() throws Exception {

        User manager = new User();
        manager.setId(5L);
        manager.setFirstName("Tom");
        manager.setLastName("Khan");
        manager.setDisplayName("Tom Khan");
        manager.setEmail("admin@example.com");
        manager.setPassword("password");
        manager.setRoles(List.of(Role.MANAGER));

        AuthUser authUser = new AuthUser(manager);


        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(authUser, null, authUser.getAuthorities());

        Profile profile = new Profile(manager.id());

        ProfileTo profileTo = new ProfileTo(manager.id(), null, null);

        String jsonBody = objectMapper.writeValueAsString(profileTo);

        when(profileRepository.getOrCreate(profileTo.id())).thenReturn(profile);
        when(profileMapper.updateFromTo(profile, profileTo)).thenReturn(profile);

        perform(put("/api/profile")
                .with(SecurityMockMvcRequestPostProcessors.authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isNoContent());

        verify(profileRepository).getOrCreate(profileTo.id());
        verify(profileMapper).updateFromTo(profile, profileTo);
        verify(profileRepository).save(profile);
    }

    @Test
    void updateProfileToAndAuthUserAssureIdNotConsistent_returnError() throws Exception {
        User manager = new User();
        manager.setId(5L);
        manager.setFirstName("Tom");
        manager.setLastName("Khan");
        manager.setDisplayName("Tom Khan");
        manager.setEmail("admin@example.com");
        manager.setPassword("password");
        manager.setRoles(List.of(Role.MANAGER));

        User user = new User();
        user.setId(1L);

        AuthUser authUser = new AuthUser(user);


        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(authUser, null, authUser.getAuthorities());

        ProfileTo profileTo = new ProfileTo(manager.id(), null, null);

        String jsonBody = objectMapper.writeValueAsString(profileTo);


        perform(put("/api/profile")
                .with(SecurityMockMvcRequestPostProcessors.authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Unprocessable Entity"));
    }


    @Test
    void update_ProfileTo_GetContacts_AuthUser_AssureIdNotConsistent_returnError() throws Exception {
        User manager = new User();
        manager.setId(5L);
        manager.setFirstName("Tom");
        manager.setLastName("Khan");
        manager.setDisplayName("Tom Khan");
        manager.setEmail("admin@example.com");
        manager.setPassword("password");
        manager.setRoles(List.of(Role.MANAGER));

        User user = new User();
        user.setId(1L);

        AuthUser authUser = new AuthUser(user);


        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(authUser, null, authUser.getAuthorities());

        String jsonBody = "{"
                + "\"id\":1,"
                + "\"mailNotifications\":[],"
                + "\"contacts\":[{"
                + "\"id\":10,"
                + "\"code\":\"tg\","
                + "\"value\":\"contactValue\""
                + "}]"
                + "}";


        perform(put("/api/profile")
                .with(SecurityMockMvcRequestPostProcessors.authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Unprocessable Entity"));
    }


}
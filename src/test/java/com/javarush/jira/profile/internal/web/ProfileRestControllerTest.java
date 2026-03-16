package com.javarush.jira.profile.internal.web;

import com.javarush.jira.AbstractControllerTest;
import com.javarush.jira.login.AuthUser;

import com.javarush.jira.profile.internal.ProfileRepository;
import com.javarush.jira.profile.internal.model.Profile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;

@WebMvcTest(ProfileRepository.class)
class ProfileRestControllerTest extends AbstractControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    ProfileRepository profileRepository;

    @Test
    void getProfileByAuthUser(){
        Long profileId = 1L;
        Profile profile = new Profile(profileId);
        when(profileRepository.findById(profileId)).thenReturn(Optional.of(profile));

    }




}
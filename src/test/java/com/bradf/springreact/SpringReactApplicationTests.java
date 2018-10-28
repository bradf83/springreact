package com.bradf.springreact;

import com.bradf.springreact.config.JwtConfig;
import com.bradf.springreact.payload.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SpringReactApplicationTests {

    //    TODO: Additional tests to test malicious activity, leeway, tokens

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtConfig jwtConfig;

    @Test
    public void whenSigningUpFailsBecauseTheEmailIsMissing() throws Exception {
        // No Email
        SignUpRequest signUpRequest = new SignUpRequest("test", "test", null, "tester");
        this.mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void whenSigningUpFailsBecauseTheUserNameIsMissing() throws Exception {
        // No Username
        SignUpRequest signUpRequest = new SignUpRequest("test", null, "test@example.com", "tester");
        this.mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void whenSigningUpFailsBecauseThePasswordIsMissing() throws Exception{
        // No password
        SignUpRequest signUpRequest = new SignUpRequest("test", "test", "test@example.com", null);
        this.mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void whenSigningUpFailsBecauseThePasswordIsToShort() throws Exception{
        // Password with invalid length
        SignUpRequest signUpRequest = new SignUpRequest("test", "test", "test@example.com", "test");
        this.mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void whenSigningUpFailsBecauseTheEmailIsInvalid() throws Exception {
        // Invalid email
        SignUpRequest signUpRequest = new SignUpRequest("test", "test", "test", null);
        this.mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isBadRequest());
    }

    @Transactional
    @Test
    public void whenSigningUpWithANewValidUserShouldReturnCreated() throws Exception {
        SignUpRequest signUpRequest = new SignUpRequest("newuser", "newuser", "newuser@example.com", "tester");
        this.mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value("true"))
                .andExpect(jsonPath("$.message").value("User registered successfully"));

    }

    @Test
    public void whenNotLoggedInAskingForAProtectedResourceReturnsUnauthorized() throws Exception {
        this.mockMvc.perform(post("/api/protected"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void whenLoggedInAndRequestingProtectedResource() throws Exception {
        // Login
        JwtAuthenticationResponse response = this.login();
        ProtectedRequest protectedRequest = new ProtectedRequest("Some message");
        // Request protected resource with acquired token
        this.mockMvc.perform(post("/api/protected")
                .header("Authorization", "Bearer " + response.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(protectedRequest)))
                .andDo(print())
                .andExpect(status().isOk());

    }

    @Test
    public void whenLoggedInAndRefreshingAccessToken() throws Exception {
        JwtAuthenticationResponse response = this.login();
        this.mockMvc.perform(post("/api/auth/refresh")
                .header("Authorization", "Bearer " + response.getAccessToken()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void whenAccessingProtectedResourceAfterRefreshingToken() throws Exception {
        JwtAuthenticationResponse response = this.login();
        JwtAuthenticationResponse refresh = this.refreshAccessToken(response.getAccessToken());

        ProtectedRequest protectedRequest = new ProtectedRequest("Some message");
        // Request protected resource with acquired token
        this.mockMvc.perform(post("/api/protected")
                .header("Authorization", "Bearer " + refresh.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(protectedRequest)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void whenRefreshingAccessTokenAfterAccessTokenHasExpired() throws Exception {
        JwtAuthenticationResponse response = this.login();
        this.waitForTokenExpiry();

        this.mockMvc.perform(post("/api/auth/refresh")
                .header("Authorization", "Bearer " + response.getAccessToken()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void whenAccessingProtectedResourceWithExpiredAccessToken() throws Exception {
        JwtAuthenticationResponse response = this.login();
        this.waitForTokenExpiry();

        ProtectedRequest protectedRequest = new ProtectedRequest("Some message");
        // Request protected resource with acquired token
        this.mockMvc.perform(post("/api/protected")
                .header("Authorization", "Bearer " + response.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(protectedRequest)))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Login with a valid user and get a JWT Token
     * @return
     * @throws Exception
     */
    private JwtAuthenticationResponse login() throws Exception{
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsernameOrEmail("test");
        loginRequest.setPassword("secret");
        MvcResult result = this.mockMvc.perform(
                post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(loginRequest))
        ).andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        return this.objectMapper.readValue(result.getResponse().getContentAsString(), JwtAuthenticationResponse.class);
    }

    private JwtAuthenticationResponse refreshAccessToken(String accessToken) throws Exception {
        MvcResult result = this.mockMvc.perform(post("/api/auth/refresh")
                .header("Authorization", "Bearer " + accessToken))
                .andReturn();
        return this.objectMapper.readValue(result.getResponse().getContentAsString(), JwtAuthenticationResponse.class);
    }

    /**
     * A poor man's way of pushing time forward.  Will do for now.
     * TODO: Implement injected system clock, fixed clock for system testing to allow for time travel.
     */
    private void waitForTokenExpiry(){
        try {
            Thread.sleep(this.jwtConfig.getAccessExpiration().toMillis() + 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}

package com.example.SpendWise.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(properties = {
        // define a simple test user for the default Spring login
        "spring.security.user.name=testuser",
        "spring.security.user.password=testpass",
        "spring.security.user.roles=KIDS"
})
class LoginControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        // build MockMvc and enable Spring Security
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void loginPage_shouldReturn200() throws Exception {
        // the default Spring Security login page should be accessible
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    void login_withValidCredentials_shouldRedirect() throws Exception {
        // posting valid credentials should redirect (302) to the app
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "testuser")
                        .param("password", "testpass"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void login_withInvalidCredentials_shouldRedirectToError() throws Exception {
        // posting invalid credentials should also redirect (302) back to login with error
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "wrong")
                        .param("password", "bad"))
                .andExpect(status().is3xxRedirection());
    }
}

package com.example.SpendWise.controller;

import com.example.SpendWise.service.ExpenseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class ExpenseControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ExpenseService expenseService;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(username = "indiv")
    void listExpenses_authenticatedUser_returnsClientErrorUntilUserEntityExists() throws Exception {
        mockMvc.perform(get("/api/expenses"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void listExpenses_unauthenticatedUser_isRedirectToLogin() throws Exception {
        mockMvc.perform(get("/api/expenses"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "indiv")
    void createExpense_authenticatedUser_currentlyReturnsClientErrorBecauseNoUserEntity() throws Exception {
        String json = "{" +
                "\"name\":\"Groceries\"," +
                "\"category\":\"Food & Dining\"," +
                "\"amount\":10.50," +
                "\"date\":\"" + LocalDate.now() + "\"" +
                "}";

        mockMvc.perform(post("/api/expenses")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().is4xxClientError());
    }
}

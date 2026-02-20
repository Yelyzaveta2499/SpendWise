package com.example.SpendWise.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class BudgetControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(username = "indiv")
    void listBudgets_authenticatedUser_returnsOk() throws Exception {
        mockMvc.perform(get("/api/budgets"))
                .andExpect(status().isOk());
    }

    @Test
    void listBudgets_unauthenticatedUser_isRedirectToLogin() throws Exception {
        mockMvc.perform(get("/api/budgets"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "indiv")
    void createBudget_authenticatedUser_returnsCreated() throws Exception {
        String uniqueCategory = "Housing_" + System.currentTimeMillis();

        String json = "{" +
                "\"category\":\"" + uniqueCategory + "\"," +
                "\"amount\":1200," +
                "\"month\":2," +
                "\"year\":2026" +
                "}";

        mockMvc.perform(post("/api/budgets")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "indiv")
    void updateBudget_authenticatedUser_returnsOk() throws Exception {
        String uniqueCategory = "Utilities_" + System.currentTimeMillis();

        // Create first
        String createJson = "{" +
                "\"category\":\"" + uniqueCategory + "\"," +
                "\"amount\":250," +
                "\"month\":2," +
                "\"year\":2026" +
                "}";

        mockMvc.perform(post("/api/budgets")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated());

        // Find created id by listing and searching for the uniqueCategory
        String list = mockMvc.perform(get("/api/budgets").with(csrf()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        int catIdx = list.indexOf("\"category\":\"" + uniqueCategory + "\"");
        if (catIdx < 0) {
            throw new IllegalStateException("Could not find created budget in response: " + list);
        }

        int idIdx = list.lastIndexOf("\"id\":", catIdx);
        if (idIdx < 0) {
            throw new IllegalStateException("Could not find id near created budget in response: " + list);
        }

        int start = idIdx + 5;
        int end = start;
        while (end < list.length() && Character.isDigit(list.charAt(end))) {
            end++;
        }
        String idStr = list.substring(start, end);

        String updateJson = "{" +
                "\"amount\":300" +
                "}";

        mockMvc.perform(put("/api/budgets/" + idStr)
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk());
    }
}

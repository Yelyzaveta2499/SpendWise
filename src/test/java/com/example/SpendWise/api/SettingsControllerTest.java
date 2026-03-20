package com.example.SpendWise.api;

import com.example.SpendWise.controller.SettingsController;
import com.example.SpendWise.dto.UserSettingsDto;
import com.example.SpendWise.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class SettingsControllerTest {

    private MockMvc mockMvc;
    private UserService userService;
    private TestingAuthenticationToken auth;

    @BeforeEach
    void setup() {
        userService = Mockito.mock(UserService.class);
        SettingsController controller = new SettingsController(userService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();


        auth = new TestingAuthenticationToken("liza", "password");
        auth.setAuthenticated(true);
    }

    @Test
    void getSettings_whenUserExists_returnsOk() throws Exception {
        UserSettingsDto dto = new UserSettingsDto();
        dto.setFirstName("Liza");
        dto.setLastName("Hlushych");
        dto.setAccountType("BUSINESS");

        when(userService.getSettingsForUser("liza"))
                .thenReturn(Optional.of(dto));

        mockMvc.perform(get("/api/settings")
                        .principal(auth))
                .andExpect(status().isOk());
    }

    @Test
    void updateSettings_whenServiceReturnsDto_returnsOk() throws Exception {
        UserSettingsDto dto = new UserSettingsDto();
        dto.setFirstName("Liza");
        dto.setLastName("Hlushych");
        dto.setAccountType("BUSINESS");

        when(userService.updateSettingsForUser(any(), any())).thenReturn(Optional.of(dto));

        String body = "{" +
                "\"firstName\":\"Liza\"," +
                "\"lastName\":\"Hlushych\"," +
                "\"accountType\":\"BUSINESS\"" +
                "}";

        mockMvc.perform(put("/api/settings")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    void deleteAccount_whenServiceReturnsTrue_returnsNoContent() throws Exception {
        when(userService.deleteAccountForUser("liza")).thenReturn(true);

        mockMvc.perform(delete("/api/settings/account")
                        .principal(auth))
                .andExpect(status().isNoContent());
    }
}

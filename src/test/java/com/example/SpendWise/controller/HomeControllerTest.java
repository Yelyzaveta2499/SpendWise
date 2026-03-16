package com.example.SpendWise.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HomeControllerTest {

    private HomeController homeController;

    @BeforeEach
    void setUp() {
        homeController = new HomeController();
    }

    @Test
    void index_returnsIndexView() {
        String view = homeController.index();
        assertEquals("index", view);
    }

    @Test
    void postLogin_withBusinessUser_setsBusinessAccountTypeAndMessage() {
        Authentication authentication = mock(Authentication.class);
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_BUSINESS");
        // Use explicit cast so Mockito matches the generic signature
        Collection<? extends GrantedAuthority> authorities = Collections.singleton(authority);
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);

        Model model = new ConcurrentModel();

        String viewName = homeController.postLogin(authentication, model);

        assertEquals("loading", viewName);
        assertEquals("BUSINESS", model.getAttribute("accountType"));
        assertEquals("Loading business insights and cash flow overview...", model.getAttribute("accountMessage"));
    }

    @Test
    void postLogin_withoutAuthentication_defaultsToIndividual() {
        Model model = new ConcurrentModel();

        String viewName = homeController.postLogin(null, model);

        assertEquals("loading", viewName);
        assertEquals("INDIVIDUAL", model.getAttribute("accountType"));
        assertEquals("Loading your personal SpendWise experience...", model.getAttribute("accountMessage"));
    }
}

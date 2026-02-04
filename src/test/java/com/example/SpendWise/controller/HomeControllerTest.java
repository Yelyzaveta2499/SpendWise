package com.example.SpendWise.controller;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HomeControllerTest {

    @Test
    void index_returnsIndexView() {
        HomeController controller = new HomeController();
        String view = controller.index();
        assertEquals("index", view);
    }

    @Test
    void welcome_returnsWelcomeView() {
        HomeController controller = new HomeController();
        String view = controller.welcome();
        assertEquals("welcome", view);
    }
}


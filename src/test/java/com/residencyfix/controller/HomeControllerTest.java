package com.residencyfix.controller;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HomeControllerTest {

    @Test
    void loginPageShouldReturnLoginView() {
        HomeController controller = new HomeController(null);
        ExtendedModelMap model = new ExtendedModelMap();

        String viewName = controller.login(model);

        assertEquals("login", viewName);
        assertNotNull(model.get("roleOptions"));
    }

    @Test
    void residentDashboardShouldReturnDashboardView() {
        HomeController controller = new HomeController(null);
        ExtendedModelMap model = new ExtendedModelMap();

        String viewName = controller.dashboard("resident", model);

        assertEquals("dashboard", viewName);
        assertEquals("Resident", model.get("roleTitle"));
    }
}

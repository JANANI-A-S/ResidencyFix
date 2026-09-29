package com.residencyfix.controller;

import com.residencyfix.service.ComplaintService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    void homeUsesLiveComplaintCountsInsteadOfSampleMetrics() {
        ComplaintService complaintService = mock(ComplaintService.class);
        when(complaintService.getAllComplaints()).thenReturn(List.of());
        HomeController controller = new HomeController(complaintService);
        ExtendedModelMap model = new ExtendedModelMap();

        String viewName = controller.home(model);

        assertEquals("index", viewName);
        assertEquals(0, model.get("totalRequests"));
        assertEquals(List.of(), model.get("recentComplaints"));
        verify(complaintService).getAllComplaints();
    }

    @Test
    void residentDashboardShouldReturnDashboardView() {
        ComplaintService complaintService = mock(ComplaintService.class);
        when(complaintService.getAllComplaints()).thenReturn(List.of());
        when(complaintService.searchComplaintByResidentName("aisha")).thenReturn(List.of());
        HomeController controller = new HomeController(complaintService);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("residentName", "aisha");
        ExtendedModelMap model = new ExtendedModelMap();

        String viewName = controller.dashboard("resident", session, model);

        assertEquals("dashboard", viewName);
        assertEquals("Resident", model.get("roleTitle"));
    }
}

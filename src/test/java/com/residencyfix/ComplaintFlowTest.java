package com.residencyfix;

import com.residencyfix.model.Complaint;
import com.residencyfix.model.ComplaintStatus;
import com.residencyfix.service.ComplaintService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = {com.residencyfix.controller.ComplaintController.class, com.residencyfix.controller.HomeController.class})
class ComplaintFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ComplaintService complaintService;

    @Test
    void residentComplaintFormOnlyContainsTheRequiredFields() throws Exception {
        mockMvc.perform(get("/complaints/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("complaint-form"))
                .andExpect(content().string(containsString("Category")))
                .andExpect(content().string(containsString("Room Number")))
                .andExpect(content().string(containsString("Description")))
                .andExpect(content().string(not(containsString("Resident Name"))))
                .andExpect(content().string(not(containsString("Status"))));
    }

    @Test
    void staffStatusUpdatePageOnlyShowsStatusField() throws Exception {
        Complaint complaint = new Complaint();
        complaint.setId(1L);
        complaint.setResidentName("Aisha");
        complaint.setRoomNumber("A-101");
        complaint.setCategory("Plumbing");
        complaint.setDescription("Water leakage in the bathroom.");
        complaint.setStatus(ComplaintStatus.NEW);

        when(complaintService.getComplaintById(1L)).thenReturn(complaint);

        mockMvc.perform(get("/complaints/1/status"))
                .andExpect(status().isOk())
                .andExpect(view().name("complaint-form"))
                .andExpect(content().string(containsString("Status")))
                .andExpect(content().string(not(containsString("Category"))))
                .andExpect(content().string(not(containsString("Description"))));
    }

    @Test
    void staffDashboardShowsUpdateStatusAction() throws Exception {
        Complaint complaint = new Complaint();
        complaint.setId(2L);
        complaint.setResidentName("Aisha");
        complaint.setRoomNumber("B-204");
        complaint.setCategory("Electrical");
        complaint.setDescription("Fan is not working in the room.");
        complaint.setStatus(ComplaintStatus.NEW);

        when(complaintService.getAllComplaints()).thenReturn(java.util.List.of(complaint));

        mockMvc.perform(get("/dashboard/staff"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Update Status")))
                .andExpect(content().string(containsString("/complaints/2/status")));
    }

    @Test
    void staffStatusUpdatePageShowsStatusNoteField() throws Exception {
        Complaint complaint = new Complaint();
        complaint.setId(3L);
        complaint.setResidentName("Rahul");
        complaint.setRoomNumber("C-404");
        complaint.setCategory("Cleaning");
        complaint.setDescription("Washroom requires cleaning.");
        complaint.setStatus(ComplaintStatus.NEW);

        when(complaintService.getComplaintById(3L)).thenReturn(complaint);

        mockMvc.perform(get("/complaints/3/status"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Status Note")))
                .andExpect(content().string(containsString("Update Status")));
    }
}

package com.residencyfix.controller;

import com.residencyfix.model.Complaint;
import com.residencyfix.model.ComplaintStatus;
import com.residencyfix.service.ComplaintService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    private final ComplaintService complaintService;

    public HomeController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<Complaint> complaints = complaintService.getAllComplaints();
        List<Complaint> recentComplaints = complaints.stream()
                .sorted(Comparator.comparing(Complaint::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(3)
                .toList();

        model.addAttribute("appName", "ResidencyFix");
        model.addAttribute("recentComplaints", recentComplaints);
        model.addAttribute("totalRequests", complaints.size());
        model.addAttribute("newRequests", complaints.stream().filter(c -> c.getStatus() == ComplaintStatus.NEW).count());
        model.addAttribute("inProgressRequests", complaints.stream().filter(c -> c.getStatus() == ComplaintStatus.IN_PROGRESS).count());
        model.addAttribute("resolvedRequests", complaints.stream().filter(c -> c.getStatus() == ComplaintStatus.RESOLVED).count());
        return "index";
    }

    @GetMapping("/login")
    public String login(Model model, @RequestParam(value = "logout", required = false) String logout) {
        model.addAttribute("roleOptions", List.of("Resident", "Staff", "Warden"));
        if (logout != null) {
            model.addAttribute("logoutMessage", "You have been logged out successfully.");
        }
        return "login";
    }

    public String login(Model model) {
        return login(model, null);
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String role,
                              @RequestParam(value = "name", required = false) String name,
                              @RequestParam(value = "email", required = false) String email,
                              @RequestParam(value = "room", required = false) String room,
                              HttpSession session) {
        String normalizedRole = switch (role == null ? "" : role.trim().toLowerCase()) {
            case "staff" -> "staff";
            case "warden" -> "warden";
            default -> "resident";
        };
        String displayName = name == null ? "" : name.trim();
        if (displayName.isBlank() && email != null && !email.isBlank()) {
            displayName = capitalizeWords(email.split("@")[0].replace('.', ' '));
        }
        session.setAttribute("loggedInRole", normalizedRole);
        session.setAttribute("userRoleTitle", switch (normalizedRole) {
            case "staff" -> "Maintenance Staff";
            case "warden" -> "Hostel Warden";
            default -> "Resident";
        });

        if ("resident".equals(normalizedRole)) {
            if (!displayName.isBlank()) {
                session.setAttribute("residentName", displayName);
            } else {
                session.removeAttribute("residentName");
            }
            if (room != null && !room.isBlank()) {
                session.setAttribute("userRoom", room.trim());
            } else {
                session.removeAttribute("userRoom");
            }
        } else {
            session.removeAttribute("residentName");
            session.removeAttribute("userRoom");
        }

        if (!displayName.isBlank()) {
            session.setAttribute("userName", displayName);
        } else {
            session.removeAttribute("userName");
        }
        if (email != null && !email.isBlank()) {
            session.setAttribute("userEmail", email.trim());
        } else {
            session.removeAttribute("userEmail");
        }

        return "redirect:/dashboard/" + normalizedRole;
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout=true";
    }

    @GetMapping("/dashboard/{role}")
    public String dashboard(@PathVariable String role,
                            @RequestParam(value = "category", required = false) String category,
                            @RequestParam(value = "status", required = false) ComplaintStatus status,
                            @RequestParam(value = "view", required = false) String view,
                            HttpSession session,
                            Model model) {
        return dashboardInternal(role, category, status, view, session, model);
    }

    public String dashboard(String role, HttpSession session, Model model) {
        return dashboardInternal(role, null, null, null, session, model);
    }

    private String dashboardInternal(String role, String category, ComplaintStatus status, String view, HttpSession session, Model model) {
        String normalizedRole = role == null ? "resident" : role.toLowerCase();
        String roleTitle = switch (normalizedRole) {
            case "staff" -> "Staff";
            case "warden" -> "Warden";
            default -> "Resident";
        };

        List<Complaint> complaints;
        if ("resident".equals(normalizedRole)) {
            String residentName = (String) session.getAttribute("residentName");
            if (residentName != null && !residentName.isBlank()) {
                complaints = complaintService.searchComplaintByResidentName(residentName);
            } else {
                complaints = List.of();
            }
        } else if ("open-age".equalsIgnoreCase(view)) {
            // Core Feature 4: Warden views all open complaints sorted by age
            complaints = complaintService.getOpenComplaintsSortedByAge();
        } else if ("overdue".equalsIgnoreCase(view)) {
            // Core Feature 5: Auto-flag complaints open for more than 5 days as overdue
            complaints = complaintService.getOverdueComplaints();
        } else {
            // Staff and Warden can filter by category or status
            if ((category != null && !category.isBlank() && !category.equalsIgnoreCase("all")) || status != null) {
                complaints = complaintService.filterComplaints(null, status, category);
            } else {
                complaints = complaintService.getAllComplaints();
            }
        }

        // Summary calculations
        List<Complaint> allList = complaintService.getAllComplaints();
        List<Complaint> scopeList = "resident".equals(normalizedRole) ? complaints : allList;

        long open = scopeList.stream().filter(c -> c.getStatus() == ComplaintStatus.NEW).count();
        long inProgress = scopeList.stream().filter(c -> c.getStatus() == ComplaintStatus.IN_PROGRESS).count();
        long resolved = scopeList.stream().filter(c -> c.getStatus() == ComplaintStatus.RESOLVED).count();
        long overdue = scopeList.stream().filter(Complaint::isOverdue).count();

        long plumbingCount = allList.stream().filter(c -> "Plumbing".equalsIgnoreCase(c.getCategory())).count();
        long electricalCount = allList.stream().filter(c -> "Electrical".equalsIgnoreCase(c.getCategory())).count();
        long cleaningCount = allList.stream().filter(c -> "Cleaning".equalsIgnoreCase(c.getCategory())).count();

        model.addAttribute("role", normalizedRole);
        model.addAttribute("roleTitle", roleTitle);
        model.addAttribute("complaints", complaints);
        model.addAttribute("statuses", Arrays.asList(ComplaintStatus.values()));
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedView", view);
        model.addAttribute("categories", List.of("Plumbing", "Electrical", "Cleaning"));
        model.addAttribute("summary", Map.of(
                "total", scopeList.size(),
                "open", open,
                "inProgress", inProgress,
                "resolved", resolved,
                "overdue", overdue,
                "plumbing", plumbingCount,
                "electrical", electricalCount,
                "cleaning", cleaningCount
        ));
        return "dashboard";
    }

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        String role = (String) session.getAttribute("loggedInRole");
        if (role == null) role = "resident";

        String name = (String) session.getAttribute("userName");
        if (name == null) {
            name = switch (role) {
                case "staff" -> "Maintenance Staff";
                case "warden" -> "Hostel Warden";
                default -> "Resident";
            };
        }

        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            email = "Not provided";
        }

        String room = (String) session.getAttribute("userRoom");
        String unit = "staff".equals(role) ? "Facilities team"
                : "warden".equals(role) ? "Hostel administration"
                : room == null || room.isBlank() ? "Not provided" : room;

        String roleLabel = switch (role) {
            case "staff" -> "Staff";
            case "warden" -> "Warden";
            default -> "Resident";
        };

        model.addAttribute("roleOptions", List.of("Resident", "Staff", "Warden"));
        model.addAttribute("profileName", name);
        model.addAttribute("profileRole", roleLabel);
        model.addAttribute("profileUnit", unit);
        model.addAttribute("profileEmail", email);
        return "profile";
    }

    private String capitalizeWords(String input) {
        if (input == null || input.isBlank()) return "Resident";
        String[] words = input.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0)))
                  .append(w.substring(1).toLowerCase())
                  .append(" ");
            }
        }
        return sb.toString().trim();
    }
}

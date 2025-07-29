package com.agrifinance.backend.controller;

import com.agrifinance.backend.dto.ProjectDTO;
import com.agrifinance.backend.dto.ProjectGoalDTO;
import com.agrifinance.backend.security.JwtUtil;
import com.agrifinance.backend.service.ProjectService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Projects")
public class ProjectController {
    private final ProjectService projectService;
    private final JwtUtil jwtUtil;

    private UUID getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return UUID.fromString(jwtUtil.getClaims(token).get("userId", String.class));
    }

    @GetMapping("/overview")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ProjectDTO> getOverview(HttpServletRequest request) {
        return ResponseEntity.ok(projectService.getProjectOverview(getUserId(request)));
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<ProjectDTO>> getProjects(HttpServletRequest request,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String search) {
        return ResponseEntity.ok(projectService.getProjects(getUserId(request), status, search));
    }

    @GetMapping("/goals")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<ProjectGoalDTO>> getGoals(HttpServletRequest request,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String priority) {
        return ResponseEntity.ok(projectService.getProjectGoals(getUserId(request), status, priority));
    }

    @GetMapping("/analytics")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getAnalytics(HttpServletRequest request) {
        return ResponseEntity.ok(projectService.getProjectAnalytics(getUserId(request)));
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ProjectDTO> create(HttpServletRequest request, @RequestBody ProjectDTO dto) {
        return ResponseEntity.ok(projectService.createProject(getUserId(request), dto));
    }
}

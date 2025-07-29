package com.agrifinance.backend.controller;

import com.agrifinance.backend.dto.LoanDTO;
import com.agrifinance.backend.dto.LoanPaymentDTO;
import com.agrifinance.backend.security.JwtUtil;
import com.agrifinance.backend.service.LoanService;
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
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@Tag(name = "Loans")
public class LoanController {
    private final LoanService loanService;
    private final JwtUtil jwtUtil;

    private UUID getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return UUID.fromString(jwtUtil.getClaims(token).get("userId", String.class));
    }

    @GetMapping("/overview")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<LoanDTO> getOverview(HttpServletRequest request) {
        return ResponseEntity.ok(loanService.getLoanOverview(getUserId(request)));
    }

    @GetMapping("/payments")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<LoanPaymentDTO>> getPayments(HttpServletRequest request) {
        return ResponseEntity.ok(loanService.getLoanPayments(getUserId(request)));
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<LoanDTO>> getHistory(HttpServletRequest request) {
        return ResponseEntity.ok(loanService.getLoanHistory(getUserId(request)));
    }

    @GetMapping("/analytics")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getAnalytics(HttpServletRequest request) {
        return ResponseEntity.ok(loanService.getLoanAnalytics(getUserId(request)));
    }

    @PostMapping("/apply")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<LoanDTO> apply(HttpServletRequest request, @RequestBody LoanDTO dto) {
        return ResponseEntity.ok(loanService.applyForLoan(getUserId(request), dto));
    }
}

package com.agrifinance.backend.service;

import com.agrifinance.backend.dto.LoanDTO;
import com.agrifinance.backend.dto.LoanPaymentDTO;
import com.agrifinance.backend.model.Loan;
import com.agrifinance.backend.model.LoanPayment;
import com.agrifinance.backend.model.User;
import com.agrifinance.backend.repository.LoanPaymentRepository;
import com.agrifinance.backend.repository.LoanRepository;
import com.agrifinance.backend.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LoanService {
    private final LoanRepository loanRepository;
    private final LoanPaymentRepository loanPaymentRepository;
    private final UserRepository userRepository;

    public LoanService(LoanRepository loanRepository, LoanPaymentRepository loanPaymentRepository, UserRepository userRepository) {
        this.loanRepository = loanRepository;
        this.loanPaymentRepository = loanPaymentRepository;
        this.userRepository = userRepository;
    }

    // Manual mapping methods
    private LoanDTO toDto(Loan loan) {
        if (loan == null) return null;
        LoanDTO dto = new LoanDTO();
        dto.setId(loan.getId() != null ? loan.getId().toString() : null);
        dto.setUserId(loan.getUser() != null ? loan.getUser().getId().toString() : null);
        dto.setAmount(loan.getAmount());
        dto.setStatus(loan.getStatus());
        dto.setType(loan.getType());
        dto.setCreatedAt(loan.getCreatedAt());
        dto.setUpdatedAt(loan.getUpdatedAt());
        if (loan.getPayments() != null) {
            dto.setPayments(loan.getPayments().stream().map(this::toDto).toList());
        }
        return dto;
    }
    private List<LoanDTO> toDtoList(List<Loan> loans) {
        return loans.stream().map(this::toDto).toList();
    }
    private Loan toEntity(LoanDTO dto) {
        if (dto == null) return null;
        Loan loan = new Loan();
        if (dto.getId() != null) loan.setId(UUID.fromString(dto.getId()));
        loan.setAmount(dto.getAmount());
        loan.setStatus(dto.getStatus());
        loan.setType(dto.getType());
        loan.setCreatedAt(dto.getCreatedAt());
        loan.setUpdatedAt(dto.getUpdatedAt());
        // user and payments set elsewhere
        return loan;
    }
    private LoanPaymentDTO toDto(LoanPayment payment) {
        if (payment == null) return null;
        LoanPaymentDTO dto = new LoanPaymentDTO();
        dto.setId(payment.getId() != null ? payment.getId().toString() : null);
        dto.setAmount(payment.getAmount());
        dto.setDueDate(payment.getDueDate());
        dto.setPaidDate(payment.getPaidDate());
        dto.setStatus(payment.getStatus());
        return dto;
    }
    private List<LoanPaymentDTO> toDtoListPayments(List<LoanPayment> payments) {
        return payments.stream().map(this::toDto).toList();
    }

    // User Endpoints
    public LoanDTO getLoanOverview(UUID userId) {
        List<Loan> loans = loanRepository.findByUserId(userId);
        return loans.isEmpty() ? null : toDto(loans.get(loans.size() - 1));
    }
    public List<LoanPaymentDTO> getLoanPayments(UUID userId) {
        List<Loan> loans = loanRepository.findByUserId(userId);
        List<LoanPayment> payments = loans.stream()
                .flatMap(loan -> loan.getPayments().stream())
                .toList();
        return toDtoListPayments(payments);
    }
    public List<LoanDTO> getLoanHistory(UUID userId) {
        return toDtoList(loanRepository.findByUserId(userId));
    }
    public Map<String, Object> getLoanAnalytics(UUID userId) {
        List<Loan> loans = loanRepository.findByUserId(userId);
        double total = loans.stream().mapToDouble(Loan::getAmount).sum();
        long approved = loans.stream().filter(l -> "APPROVED".equals(l.getStatus())).count();
        long pending = loans.stream().filter(l -> "PENDING".equals(l.getStatus())).count();
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalAmount", total);
        analytics.put("approvedCount", approved);
        analytics.put("pendingCount", pending);
        return analytics;
    }
    public LoanDTO applyForLoan(UUID userId, LoanDTO dto) {
        User user = userRepository.findById(userId).orElseThrow();
        Loan loan = toEntity(dto);
        loan.setId(null);
        loan.setUser(user);
        loan.setStatus("PENDING");
        loan.setCreatedAt(LocalDateTime.now());
        loan.setUpdatedAt(LocalDateTime.now());
        loan = loanRepository.save(loan);
        return toDto(loan);
    }
    // Admin Endpoints
    public Page<LoanDTO> getAllLoans(int page, int limit, String status, String type, Double minAmount, Double maxAmount) {
        Pageable pageable = PageRequest.of(page, limit);
        Specification<Loan> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            if (type != null) predicates.add(cb.equal(root.get("type"), type));
            if (minAmount != null) predicates.add(cb.ge(root.get("amount"), minAmount));
            if (maxAmount != null) predicates.add(cb.le(root.get("amount"), maxAmount));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return loanRepository.findAll(spec, pageable).map(this::toDto);
    }
    public LoanDTO getLoanById(UUID loanId) {
        return toDto(loanRepository.findById(loanId).orElseThrow());
    }
    public LoanDTO updateLoanStatus(UUID loanId, String status) {
        Loan loan = loanRepository.findById(loanId).orElseThrow();
        loan.setStatus(status);
        loan.setUpdatedAt(LocalDateTime.now());
        return toDto(loanRepository.save(loan));
    }
    public Map<String, Object> getLoanSummary() {
        List<Loan> loans = loanRepository.findAll();
        double total = loans.stream().mapToDouble(Loan::getAmount).sum();
        long approved = loans.stream().filter(l -> "APPROVED".equals(l.getStatus())).count();
        long pending = loans.stream().filter(l -> "PENDING".equals(l.getStatus())).count();
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalAmount", total);
        summary.put("approvedCount", approved);
        summary.put("pendingCount", pending);
        summary.put("totalLoans", loans.size());
        return summary;
    }
}

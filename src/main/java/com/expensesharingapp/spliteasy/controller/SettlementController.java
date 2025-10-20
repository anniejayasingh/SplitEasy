package com.expensesharingapp.spliteasy.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.expensesharingapp.spliteasy.dto.GroupExpenseBalancesResponse;
import com.expensesharingapp.spliteasy.dto.SettlementRequest;
import com.expensesharingapp.spliteasy.dto.SettlementResponse;
import com.expensesharingapp.spliteasy.service.SettlementService;
import com.expensesharingapp.spliteasy.util.ApiResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/settlements")
@RequiredArgsConstructor
@Slf4j
public class SettlementController {

    private final SettlementService settlementService;

    @PostMapping
    public ResponseEntity<ApiResponse<SettlementResponse>> createSettlement(
            @RequestBody SettlementRequest request) {
        log.info("Entering createSettlement: {}", request);
        SettlementResponse response = settlementService.createSettlement(request);
        log.info("Exiting createSettlement: {}", response);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<SettlementResponse>builder()
                        .success(true)
                        .message("Settlement created successfully")
                        .data(response)
                        .build());
    }

    @GetMapping("/expense/{expenseId}")
    public ResponseEntity<ApiResponse<List<SettlementResponse>>> getSettlementsByExpense(
            @PathVariable Long expenseId) {
        List<SettlementResponse> settlements = settlementService.getByExpense(expenseId);
        return ResponseEntity.ok(ApiResponse.<List<SettlementResponse>>builder()
                .success(true)
                .data(settlements)
                .build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<SettlementResponse>>> getSettlementsByUser(
            @PathVariable Long userId) {
        List<SettlementResponse> settlements = settlementService.getByUser(userId);
        return ResponseEntity.ok(ApiResponse.<List<SettlementResponse>>builder()
                .success(true)
                .data(settlements)
                .build());
    }
    
    
    
    /**
     * Get balances per user for all expenses in a group
     */
    @GetMapping("/group/{groupId}/balances")
    public ResponseEntity<ApiResponse<GroupExpenseBalancesResponse>> getGroupBalances(
            @PathVariable Long groupId) {
        log.info("Fetching balances for group id: {}", groupId);

        GroupExpenseBalancesResponse response = settlementService.getBalancesByGroup(groupId);

        log.info("Balances fetched for group {}: {} entries", groupId, response.balances().size());
        return ResponseEntity.ok(ApiResponse.<GroupExpenseBalancesResponse>builder()
                .success(true)
                .data(response)
                .build());
    }
}


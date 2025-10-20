package com.expensesharingapp.spliteasy.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.expensesharingapp.spliteasy.dto.*;
import com.expensesharingapp.spliteasy.service.ExpenseService;
import com.expensesharingapp.spliteasy.util.ApiResponse;

import java.util.List;

/**
 * Controller for managing Expense-related endpoints.
 * Supports create, update, delete, fetch by ID and group.
 */
@RestController
@RequestMapping("/expenses")
@RequiredArgsConstructor
@Slf4j
public class ExpenseController {

    private final ExpenseService expenseService;

    /**
     * Create a new expense (equal or custom split)
     */
    @PostMapping()
    public ResponseEntity<ApiResponse<ExpenseResponse>> createNewExpense(@RequestBody ExpenseRequest request) {
        log.info("Entering createNewExpense: {}", request);

        ExpenseResponse response = expenseService.createExpense(request);

        log.info("Exiting createNewExpense: {}", response);
        return new ResponseEntity<>(ApiResponse.<ExpenseResponse>builder()
                .success(true)
                .message("Expense created successfully")
                .data(response)
                .build(), HttpStatus.CREATED);
    }

    /**
     * Get expense by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> getExpenseById(@PathVariable Long id) {
        log.info("Entering getExpense with id: {}", id);

        ExpenseResponse response = expenseService.getExpenseById(id);

        log.info("Exiting getExpense: {}", response);
        return ResponseEntity.ok(ApiResponse.<ExpenseResponse>builder()
                .success(true)
                .data(response)
                .build());
    }

    /**
     * Get all expenses
     */
    @GetMapping("/fetchAll")
    public ResponseEntity<ApiResponse<List<ExpenseResponse>>> getAllExpensesList() {
        log.info("Entering getAllExpensesList");

        List<ExpenseResponse> responseList = expenseService.getAllExpenses();

        log.info("Exiting getAllExpensesList, count: {}", responseList.size());
        return ResponseEntity.ok(ApiResponse.<List<ExpenseResponse>>builder()
                .success(true)
                .data(responseList)
                .build());
    }

    /**
     * Get all expenses for a specific group
     */
    @GetMapping("/group/{groupId}")
    public ResponseEntity<ApiResponse<List<ExpenseResponse>>> getExpensesByGroup(@PathVariable Long groupId) {
        log.info("Entering getExpensesByGroup with groupId: {}", groupId);

        List<ExpenseResponse> responseList = expenseService.getExpensesByGroup(groupId);

        log.info("Exiting getExpensesByGroup, count: {}", responseList.size());
        return ResponseEntity.ok(ApiResponse.<List<ExpenseResponse>>builder()
                .success(true)
                .data(responseList)
                .build());
    }

    /**
     * Update an existing expense
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> updateExpense(
            @PathVariable Long id,
            @RequestBody ExpenseRequest request) {
        log.info("Entering updateExpense with id: {} and request: {}", id, request);

        ExpenseResponse updatedExpense = expenseService.updateExpense(id, request);

        log.info("Exiting updateExpense with response: {}", updatedExpense);
        return ResponseEntity.ok(ApiResponse.<ExpenseResponse>builder()
                .success(true)
                .message("Expense updated successfully")
                .data(updatedExpense)
                .build());
    }

    /**
     * Delete an expense by ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(@PathVariable Long id) {
        log.info("Entering deleteExpense with id: {}", id);

        expenseService.deleteExpense(id);

        log.info("Exiting deleteExpense for id: {}", id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Expense deleted successfully")
                .build());
    }
}

package com.expensesharingapp.spliteasy.service;

import com.expensesharingapp.spliteasy.dto.*;
import com.expensesharingapp.spliteasy.entity.*;
import com.expensesharingapp.spliteasy.exception.ResourceNotFoundException;
import com.expensesharingapp.spliteasy.exception.UnAuthorizedActionException;
import com.expensesharingapp.spliteasy.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing expenses, including create, update, delete, and fetch operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;

    /**
     * Create a new expense (equal or custom split)
     */
    @Transactional
    public ExpenseResponse createExpense(ExpenseRequest request) {
        log.info("Creating expense: {}", request);

        // Fetch group
        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Group", request.getGroupId().toString(),request));

        // Fetch creator
        User creator = userRepository.findById(request.getCreatedById())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Creator", request.getCreatedById().toString(),request));

        // Create expense entity
        Expense expense = Expense.builder()
                .description(request.getDescription())
                .group(group)
                .paidBy(creator)
                .totalAmount(request.getTotalAmount())
                .isEqualSplit(request.getIsEqualSplit())
                .createdOn(LocalDateTime.now())
                .build();

        Map<Long, BigDecimal> splits = calculateSplits(request);
        expense.setCustomSplits(splits);

        expense.setCustomSplits(splits); // assuming a Map<Long, BigDecimal> in Expense entity

        Expense savedExpense = expenseRepository.save(expense);

        log.info("Expense created successfully: {}", savedExpense);

        return mapToResponse(savedExpense);
    }

    /**
     * Update an existing expense
     */
    @Transactional
    public ExpenseResponse updateExpense(Long expenseId, ExpenseRequest request) {
        log.info("Updating expense id {}: {}", expenseId, request);

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expenses", expenseId.toString(),request));

        // Check for unsettled balances
        if (!isExpenseSettled(expense)) {
            throw new UnAuthorizedActionException("Cannot update expense with unsettled balances",expenseId.toString(),request);
        }

        // Update fields
        expense.setDescription(request.getDescription());
        expense.setTotalAmount(request.getTotalAmount());
        expense.setIsEqualSplit(request.getIsEqualSplit());

        Map<Long, BigDecimal> splits = calculateSplits(request);
        expense.setCustomSplits(splits);

        Expense updatedExpense = expenseRepository.save(expense);

        log.info("Expense updated successfully: {}", updatedExpense);
        return mapToResponse(updatedExpense);
    }

    /**
     * Delete an expense
     */
    @Transactional
    public void deleteExpense(Long expenseId) {
        log.info("Deleting expense id: {}", expenseId);

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found", expenseId.toString(),""));

        if (!isExpenseSettled(expense)) {
            throw new UnAuthorizedActionException("Cannot delete expense with unsettled balances with expense ID: ","",expenseId);
        }

        expenseRepository.delete(expense);

        log.info("Expense deleted successfully: {}", expenseId);
    }

    /**
     * Get expense by ID
     */
    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(Long id) {
        log.info("Fetching expense id: {}", id);

        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found", "",id.toString()));

        return mapToResponse(expense);
    }

    /**
     * Get all expenses
     */
    @Transactional(readOnly = true)
    public List<ExpenseResponse> getAllExpenses() {
        log.info("Fetching all expenses");
        List<Expense> expenses = expenseRepository.findAll();
        return expenses.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    /**
     * Get expenses by group
     */
    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpensesByGroup(Long groupId) {
        log.info("Fetching expenses for group id: {}", groupId);

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found", groupId.toString(),""));

        return expenseRepository.findByGroup(group).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Check if all splits in expense are settled
     */
    private boolean isExpenseSettled(Expense expense) {
        // If all amounts in userSplits map are zero → settled
        return expense.getCustomSplits().values().stream()
                .allMatch(amount -> amount.compareTo(BigDecimal.ZERO) == 0);
    }

    /**
     * Map Expense entity to ExpenseResponse DTO
     */
    private ExpenseResponse mapToResponse(Expense expense) {
        List<ExpenseSplitResponse> splits = expense.getCustomSplits().entrySet().stream()
                .map(e -> ExpenseSplitResponse.builder()
                        .userId(e.getKey())
                        .amount(e.getValue())
                        .build())
                .collect(Collectors.toList());

        return ExpenseResponse.builder()
                .id(expense.getId())
                .description(expense.getDescription())
                .totalAmount(expense.getTotalAmount())
                .createdById(expense.getPaidBy().getId())
                .groupId(expense.getGroup().getId())
                .isEqualSplit(expense.getIsEqualSplit())
                .splits(splits)
                .createdOn(expense.getCreatedOn())
                .build();
    }
    
    /**
     * Calculate splits for an expense based on equal or custom split
     *
     * @param request ExpenseRequest containing totalAmount, participantIds, isEqualSplit, customSplits
     * @return Map<Long, BigDecimal> of user splits
     */
    private Map<Long, BigDecimal> calculateSplits(ExpenseRequest request) {
        Map<Long, BigDecimal> splits = new HashMap<>();

        if (Boolean.TRUE.equals(request.getIsEqualSplit())) {
            BigDecimal amountPerUser = request.getTotalAmount()
                    .divide(new BigDecimal(request.getParticipantIds().size()), 2, RoundingMode.HALF_UP);
            request.getParticipantIds().forEach(userId -> splits.put(userId, amountPerUser));
        } else {
            BigDecimal sumCustom = request.getCustomSplits().values().stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (sumCustom.compareTo(request.getTotalAmount()) != 0) {
                throw new IllegalArgumentException("Custom splits do not sum up to total amount");
            }
            splits.putAll(request.getCustomSplits());
        }

        return splits;
    }
}

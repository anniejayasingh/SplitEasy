package com.spliteasy.expense_sharing.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spliteasy.expense_sharing.entity.Expense;
import com.spliteasy.expense_sharing.entity.User;
import com.spliteasy.expense_sharing.service.ExpenseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for handling Expense-related endpoints.
 */
@RestController
@RequestMapping("/expenses")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Expense Controller", description = "Operations related to expenses")

public class ExpenseController {

	private final ExpenseService expenseService;

	/**
	 * Endpoint to create a new expense.
	 */
	@PostMapping
	@Operation(summary = "Add an expense", description = "Adds a new expense to a group")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Expense added successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid input") })
	public ResponseEntity<Expense> createNewExpense(@RequestBody Expense expense,
			@RequestHeader("Authorization") String authHeader) {
		log.info("Received request to add expense: {}", expense.getDescription());
		Expense addedExpense = expenseService.addExpense(expense, authHeader);
		return ResponseEntity.status(201).body(addedExpense);
	}

	/**
	 * Get all expenses for a specific group.
	 */
	@GetMapping("/group/{groupId}")
	@Operation(summary = "Get expenses by group", description = "Fetches all expenses for a specific group")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Expenses fetched successfully"),
			@ApiResponse(responseCode = "404", description = "Group not found") })
	public ResponseEntity<List<Expense>> getExpensesByGroup(@PathVariable Long groupId) {
		log.info("Fetching expenses for group id {}", groupId);
		List<Expense> expenses = expenseService.getExpensesByGroup(groupId);
		if (expenses.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(expenses);
	}

	/**
	 * Delete an expense by ID.
	 */
	@DeleteMapping("/{expenseId}")
	@Operation(summary = "Delete expense", description = "Deletes an expense by ID")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Expense deleted successfully"),
			@ApiResponse(responseCode = "404", description = "Expense not found") })
	public ResponseEntity<Void> deleteExpense(@PathVariable Long expenseId) {
		log.info("Deleting expense with id {}", expenseId);
		expenseService.deleteExpense(expenseId);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Get balances for all users in a group.
	 */
	@GetMapping("/group/{groupId}/balances")
	public ResponseEntity<Map<User, BigDecimal>> getBalances(@PathVariable Long groupId) {
		log.info("Fetching balances for group id {}", groupId);
		Map<User, BigDecimal> balances = expenseService.calculateBalances(groupId);
		if (balances.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(balances);
	}
}

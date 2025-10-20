package com.expensesharingapp.spliteasy.dto;

import java.math.BigDecimal;

//Represents a user's outstanding balance for a specific expense
public record UserExpenseBalance(Long userId, Long expenseId, BigDecimal amountOwed) {
}

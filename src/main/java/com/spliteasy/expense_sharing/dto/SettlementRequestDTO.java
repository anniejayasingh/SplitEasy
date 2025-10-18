package com.spliteasy.expense_sharing.dto;

import java.math.BigDecimal;

import lombok.Data;

/**
 * DTO for creating a new settlement.
 */
@Data
public class SettlementRequestDTO {
	private Long groupId;
	private Long fromUserId;
	private Long toUserId;
	private Long expenseId;
	private BigDecimal amount;
}

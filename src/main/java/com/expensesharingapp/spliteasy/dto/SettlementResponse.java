package com.expensesharingapp.spliteasy.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementResponse {
    private Long id;
    private Long payerId;
    private Long receiverId;
    private Long expenseId;
    private BigDecimal amount;
    private LocalDateTime createdOn;
}


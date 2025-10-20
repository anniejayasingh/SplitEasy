package com.expensesharingapp.spliteasy.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SettlementSummary {
    private Long payerId;
    private Long receiverId;
    private BigDecimal amount;
    private String status; // SETTLED or UNSETTLED
}

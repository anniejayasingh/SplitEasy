package com.expensesharingapp.spliteasy.dto;

import lombok.*;
import java.math.BigDecimal;

/**
 * Response DTO for a user's share in an expense.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseSplitResponse {

    private Long userId;
    private BigDecimal amount;
}

package com.expensesharingapp.spliteasy.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for expense details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponse {

    private Long id;
    private String description;
    private BigDecimal totalAmount;
    private Long createdById;
    private Long groupId;
    private Boolean isEqualSplit;
    private List<ExpenseSplitResponse> splits; // List of user shares
    private LocalDateTime createdOn;
}

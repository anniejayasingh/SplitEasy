package com.expensesharingapp.spliteasy.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Request DTO for creating or updating an expense.
 * Supports equal or custom split among participants.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseRequest {

    private String description;
    private Long groupId;
    private Long createdById;
    private BigDecimal totalAmount;  // Total expense amount
    private Boolean isEqualSplit;    // True → equal split, False → custom
    private List<Long> participantIds;             // For equal split
    private Map<Long, BigDecimal> customSplits;    // For custom split
}

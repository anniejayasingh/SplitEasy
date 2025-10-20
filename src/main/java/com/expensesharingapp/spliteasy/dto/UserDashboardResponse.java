package com.expensesharingapp.spliteasy.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Data;
@Data
@Builder
public class UserDashboardResponse {
    private Long userId;
    private String userName;
    private BigDecimal totalOwedByUser;   // total amount user owes others
    private BigDecimal totalOwedToUser;   // total amount owed to user
    private List<SettlementSummary> settlements;
}

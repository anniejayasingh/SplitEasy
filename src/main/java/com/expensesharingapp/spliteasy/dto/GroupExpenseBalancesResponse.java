package com.expensesharingapp.spliteasy.dto;

import java.util.List;

//Response for group balances
public record GroupExpenseBalancesResponse(
 Long groupId,
 List<UserExpenseBalance> balances
) {}

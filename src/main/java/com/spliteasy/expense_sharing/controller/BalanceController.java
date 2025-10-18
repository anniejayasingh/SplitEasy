package com.spliteasy.expense_sharing.controller;

import com.spliteasy.expense_sharing.dto.BalanceDTO;
import com.spliteasy.expense_sharing.service.BalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/balances")
public class BalanceController {

    @Autowired
    private BalanceService balanceService;

    // Get balances for a group
    @GetMapping("/group/{groupId}")
    public List<BalanceDTO> getGroupBalances(@PathVariable Long groupId) {
        return balanceService.calculateBalances(groupId);
    }
}

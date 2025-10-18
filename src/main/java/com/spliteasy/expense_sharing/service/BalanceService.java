package com.spliteasy.expense_sharing.service;

import com.spliteasy.expense_sharing.dto.BalanceDTO;
import com.spliteasy.expense_sharing.entity.Expense;
import com.spliteasy.expense_sharing.entity.Group;
import com.spliteasy.expense_sharing.entity.Settlement;
import com.spliteasy.expense_sharing.entity.User;
import com.spliteasy.expense_sharing.repository.GroupRepository;
import com.spliteasy.expense_sharing.repository.SettlementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class BalanceService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private SettlementRepository settlementRepository;

    /**
     * Compute balances for all users in a group
     */
   	public List<BalanceDTO> calculateBalances(Long groupId) {
    	    Group group = groupRepository.findById(groupId)
    	            .orElseThrow(() -> new RuntimeException("Group not found"));

    	    // Initialize balances map
    	    Map<Long, BigDecimal> balances = new HashMap<>();
    	    for (User user : group.getMembers()) {
    	        balances.put(user.getId(), BigDecimal.ZERO);
    	    }

    	    // 1️⃣ Add expenses
    	    for (Expense expense : group.getExpenses()) {
    	        BigDecimal totalAmount = expense.getAmount();
    	        int membersCount = group.getMembers().size();
    	        BigDecimal share = totalAmount.divide(BigDecimal.valueOf(membersCount), 2, BigDecimal.ROUND_HALF_UP);

    	        for (User member : group.getMembers()) {
    	            if (member.getId().equals(expense.getPaidBy().getId())) {
    	                balances.put(member.getId(), balances.get(member.getId()).add(totalAmount.subtract(share)));
    	            } else {
    	                balances.put(member.getId(), balances.get(member.getId()).subtract(share));
    	            }
    	        }
    	    }

    	    // 2️⃣ Subtract settled amounts for this group
    	    List<Settlement> settlements = settlementRepository.findByGroupAndSettledFalse(group);
    	    for (Settlement settlement : settlements) {
    	        Long payerId = settlement.getPayer().getId();
    	        Long receiverId = settlement.getReceiver().getId();
    	        BigDecimal amount = settlement.getAmount();

    	        balances.put(payerId, balances.getOrDefault(payerId, BigDecimal.ZERO).add(amount));
    	        balances.put(receiverId, balances.getOrDefault(receiverId, BigDecimal.ZERO).subtract(amount));
    	    }

    	    // 3️⃣ Convert to DTO
    	    List<BalanceDTO> result = new ArrayList<>();
    	    for (User user : group.getMembers()) {
    	        result.add(new BalanceDTO(user.getId(), user.getName(), balances.get(user.getId())));
    	    }

    	    return result;
    	}
}

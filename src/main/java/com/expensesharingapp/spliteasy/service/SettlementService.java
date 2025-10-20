package com.expensesharingapp.spliteasy.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.expensesharingapp.spliteasy.dto.GroupExpenseBalancesResponse;
import com.expensesharingapp.spliteasy.dto.SettlementRequest;
import com.expensesharingapp.spliteasy.dto.SettlementResponse;
import com.expensesharingapp.spliteasy.dto.UserExpenseBalance;
import com.expensesharingapp.spliteasy.entity.Expense;
import com.expensesharingapp.spliteasy.entity.Group;
import com.expensesharingapp.spliteasy.entity.Settlement;
import com.expensesharingapp.spliteasy.entity.User;
import com.expensesharingapp.spliteasy.exception.ResourceNotFoundException;
import com.expensesharingapp.spliteasy.exception.UnAuthorizedActionException;
import com.expensesharingapp.spliteasy.repository.ExpenseRepository;
import com.expensesharingapp.spliteasy.repository.GroupRepository;
import com.expensesharingapp.spliteasy.repository.SettlementRepository;
import com.expensesharingapp.spliteasy.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing settlements between users.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementService {

	private final SettlementRepository settlementRepository;
	private final ExpenseRepository expenseRepository;
	private final UserRepository userRepository;
	private final GroupRepository groupRepository;

	/**
	 * Create a settlement between payer and receiver for an expense.
	 */
	@Transactional
	public SettlementResponse createSettlement(SettlementRequest request) {
		log.info("Creating settlement: {}", request);

		Expense expense = expenseRepository.findById(request.getExpenseId()).orElseThrow(
				() -> new ResourceNotFoundException("Expense not found", request.getExpenseId().toString(), request));

		User payer = userRepository.findById(request.getPayerId()).orElseThrow(
				() -> new ResourceNotFoundException("Payer not found", request.getPayerId().toString(), request));

		User receiver = userRepository.findById(request.getReceiverId()).orElseThrow(
				() -> new ResourceNotFoundException("Receiver not found", request.getReceiverId().toString(), request));

		// Validate payer owes the receiver for this expense
		BigDecimal owedAmount = expense.getCustomSplits().getOrDefault(payer.getId(), BigDecimal.ZERO);
		if (owedAmount.compareTo(request.getAmount()) < 0) {
			throw new UnAuthorizedActionException("Payer cannot settle more than owed", request.getPayerId().toString(),
					request);
		}

		// Update balances
		expense.getCustomSplits().put(payer.getId(), owedAmount.subtract(request.getAmount()));

		// If receiver is in splits map, increment their received balance
		BigDecimal receiverBalance = expense.getCustomSplits().getOrDefault(receiver.getId(), BigDecimal.ZERO);
		expense.getCustomSplits().put(receiver.getId(), receiverBalance.add(request.getAmount()));
		
		expenseRepository.save(expense);

		// Save settlement record
		Settlement settlement = Settlement.builder().payer(payer).receiver(receiver).expense(expense)
				.amount(request.getAmount()).status(request.getAmount().compareTo(BigDecimal.ZERO) == 0 ? "SETTLED" : "UNSETTLED"). createdOn(LocalDateTime.now()).build();
		 
		Settlement savedSettlement = settlementRepository.save(settlement);
		log.info("Settlement created successfully: {}", savedSettlement);

		return mapToResponse(savedSettlement);
	}

	/**
	 * Fetch all settlements for a given expense.
	 */
	@Transactional(readOnly = true)
	public List<SettlementResponse> getByExpense(Long expenseId) {
		log.info("Fetching settlements for expense: {}", expenseId);
		Expense expense = expenseRepository.findById(expenseId)
				.orElseThrow(() -> new ResourceNotFoundException("Expense not found", expenseId.toString(), null));

		return settlementRepository.findByExpense(expense).stream().map(this::mapToResponse)
				.collect(Collectors.toList());
	}

	/**
	 * Fetch all settlements involving a specific user (as payer or receiver)
	 */
	@Transactional(readOnly = true)
	public List<SettlementResponse> getByUser(Long userId) {
		log.info("Fetching settlements for user: {}", userId);
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found", userId.toString(), null));

		return settlementRepository.findByPayerOrReceiver(user, user).stream().map(this::mapToResponse)
				.collect(Collectors.toList());
	}

	/**
	 * Maps Settlement entity to SettlementResponse DTO
	 */
	private SettlementResponse mapToResponse(Settlement settlement) {
		return SettlementResponse.builder().id(settlement.getId()).payerId(settlement.getPayer().getId())
				.receiverId(settlement.getReceiver().getId()).expenseId(settlement.getExpense().getId())
				.amount(settlement.getAmount()).createdOn(settlement.getCreatedOn()).build();
	}
	
	 /**
     * Calculate per-user balances for a group across all expenses
     */
    @Transactional(readOnly = true)
    public GroupExpenseBalancesResponse getBalancesByGroup(Long groupId) {
        log.info("Calculating balances for group id: {}", groupId);

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found", groupId.toString(), ""));

        List<Expense> expenses = expenseRepository.findByGroup(group);

        List<UserExpenseBalance> balances = new ArrayList<>();

        for (Expense expense : expenses) {
            Map<Long, BigDecimal> splits = new HashMap<>(expense.getCustomSplits());

            // Apply settlements
            List<Settlement> settlements = settlementRepository.findByExpense(expense);
            for (Settlement s : settlements) {
                splits.put(s.getPayer().getId(), splits.get(s.getPayer().getId()).subtract(s.getAmount()));
                splits.put(s.getReceiver().getId(), splits.get(s.getReceiver().getId()).add(s.getAmount()));
            }

            // Collect non-zero balances
            splits.forEach((userId, amount) -> {
                if (amount.compareTo(BigDecimal.ZERO) != 0) {
                    balances.add(new UserExpenseBalance(userId, expense.getId(), amount));
                }
            });
        }

        log.info("Calculated {} balance entries for group {}", balances.size(), groupId);
        return new GroupExpenseBalancesResponse(groupId, balances);
    }
    
    

}

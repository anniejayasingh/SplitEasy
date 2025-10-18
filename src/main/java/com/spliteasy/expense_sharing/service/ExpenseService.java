package com.spliteasy.expense_sharing.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spliteasy.expense_sharing.config.JwtConfig;
import com.spliteasy.expense_sharing.entity.Expense;
import com.spliteasy.expense_sharing.entity.Group;
import com.spliteasy.expense_sharing.entity.Settlement;
import com.spliteasy.expense_sharing.entity.User;
import com.spliteasy.expense_sharing.exception.ApiException;
import com.spliteasy.expense_sharing.repository.ExpenseRepository;
import com.spliteasy.expense_sharing.repository.GroupRepository;
import com.spliteasy.expense_sharing.repository.SettlementRepository;
import com.spliteasy.expense_sharing.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseService {

	private final ExpenseRepository expenseRepository;
	private final UserRepository userRepository;
	private final GroupRepository groupRepository;
	private final SettlementRepository settlementRepository;
	private final JwtConfig jwtConfig;

	/**
	 * Add a new expense and create settlements for participants
	 */
	@Transactional
	public Expense addExpense(Expense expenseRequest, String authHeader) {
		log.info("Entering addExpense");

		User paidBy = userRepository.findByEmail(jwtConfig.extractEmail(authHeader))
				.orElseThrow(() -> new ApiException("Authenticated user not found", HttpStatus.NOT_FOUND)); // Validate
																											// group
		Group group = Optional.ofNullable(expenseRequest.getGroup())
				.orElseThrow(() -> new ApiException("Group not found", HttpStatus.BAD_REQUEST)); // Validate group

		// Validate participants and ensure payer is included
		Set<User> participants = Optional.ofNullable(expenseRequest.getParticipants()).orElse(new HashSet<>()).stream()
				.map(u -> userRepository.findById(u.getId())
						.orElseThrow(() -> new RuntimeException("User not found: " + u.getId())))
				.collect(Collectors.toSet());

		participants.forEach(user -> {
			if (!group.getMembers().contains(user)) {
				throw new ApiException("User not a member of the group: " + user.getEmail(), HttpStatus.FORBIDDEN);
			}
		});
		if (!participants.contains(paidBy)) {
			participants.add(paidBy);
		}
		expenseRequest.setParticipants(participants);
		expenseRequest.setPaidBy(paidBy);
		expenseRequest.setDate(LocalDateTime.now());

		// Save expense
		Expense savedExpense = expenseRepository.save(expenseRequest);
		log.info("Expense saved with ID: {}", savedExpense.getId());

		// Create settlements for all participants except the payer
		BigDecimal share = savedExpense.getAmount().divide(BigDecimal.valueOf(participants.size()), 2,
				RoundingMode.HALF_EVEN);

		// custom split
		if (savedExpense.getSplits() != null && !savedExpense.getSplits().isEmpty()) {
			savedExpense.getSplits().forEach(split -> {
				Settlement settlement = new Settlement();
				settlement.setPayer(split.getUser());
				settlement.setReceiver(paidBy);
				settlement.setAmount(split.getAmount());
				settlement.setSettled(false);
				settlement.setExpense(savedExpense);
				settlement.setGroup(group);
				settlementRepository.save(settlement);
			});
		} else {
			// Equal split logic
			participants.stream().filter(participant -> !participant.getId().equals(paidBy.getId()))
					.forEach(participant -> {
						Settlement settlement = new Settlement();
						settlement.setPayer(participant);
						settlement.setReceiver(paidBy);
						settlement.setAmount(share);
						settlement.setSettled(false);
						settlement.setExpense(savedExpense);
						settlement.setGroup(group);
						settlementRepository.save(settlement);
						log.debug("Settlement created for participant ID: {} amount: {}", participant.getId(), share);
					});
		}

		log.info("Exiting addExpense" + savedExpense);
		return savedExpense;
	}

	/**
	 * Get all expenses for a specific group
	 */
	public List<Expense> getExpensesByGroup(Long groupId) {
		log.info("Fetching expenses for group ID: {}", groupId);
		Group group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));

		List<Expense> expenses = expenseRepository.findByGroup(group);
		log.info("Found {} expenses for group ID: {}", expenses.size(), groupId);
		return expenses;
	}

	/**
	 * Delete an expense by ID
	 */
	@Transactional
	public void deleteExpense(Long expenseId) {
		log.info("Deleting expense with ID: {}", expenseId);
		expenseRepository.deleteById(expenseId);
		log.info("Expense deleted with ID: {}", expenseId);
	}

	/**
	 * Calculate balances for all members in a group
	 */
	@Transactional(readOnly = true)
	public Map<User, BigDecimal> calculateBalances(Long groupId) {
		Group group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));

		// Initialize zero balances
		Map<User, BigDecimal> balances = group.getMembers().stream()
				.collect(Collectors.toMap(member -> member, member -> BigDecimal.ZERO));

		// Process expenses
		expenseRepository.findByGroup(group).forEach(expense -> {
			BigDecimal totalAmount = expense.getAmount();
			User paidBy = expense.getPaidBy();

			if (expense.getSplits() != null && !expense.getSplits().isEmpty()) {
				expense.getSplits().forEach(split -> {
					balances.put(split.getUser(), balances.get(split.getUser()).subtract(split.getAmount()));
				});
			} else {
				BigDecimal share = totalAmount.divide(BigDecimal.valueOf(expense.getParticipants().size()), 2,
						RoundingMode.HALF_EVEN);
				expense.getParticipants().forEach(p -> balances.put(p, balances.get(p).subtract(share)));
			}

			balances.put(paidBy, balances.get(paidBy).add(totalAmount));
		});

		// Apply settlements
		settlementRepository.findByGroupAndSettledFalse(group).forEach(settlement -> {
			balances.put(settlement.getPayer(), balances.get(settlement.getPayer()).subtract(settlement.getAmount()));
			balances.put(settlement.getReceiver(), balances.get(settlement.getReceiver()).add(settlement.getAmount()));
		});

		return balances;
	}
}

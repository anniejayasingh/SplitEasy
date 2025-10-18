package com.spliteasy.expense_sharing.service;

import com.spliteasy.expense_sharing.config.JwtConfig;
import com.spliteasy.expense_sharing.entity.Expense;
import com.spliteasy.expense_sharing.entity.Settlement;
import com.spliteasy.expense_sharing.entity.User;
import com.spliteasy.expense_sharing.repository.ExpenseRepository;
import com.spliteasy.expense_sharing.repository.GroupRepository;
import com.spliteasy.expense_sharing.repository.SettlementRepository;
import com.spliteasy.expense_sharing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final GroupRepository groupRepository;
	private final JwtConfig jwtConfig;

    

    /**
     * Create a new settlement for a specific expense
     *
     * @param fromUserId ID of the payer
     * @param toUserId   ID of the receiver
     * @param expenseId  ID of the expense
     * @param amount     Amount to settle
     * @return Saved Settlement entity
     */
    @Transactional
    public Settlement createSettlement(Long fromUserId, Long toUserId, Long expenseId, BigDecimal amount) {
        log.info("Entering createSettlement with fromUserId={}, toUserId={}, expenseId={}, amount={}", 
                 fromUserId, toUserId, expenseId, amount);

        User payer = userRepository.findById(fromUserId)
                .orElseThrow(() -> {
                    log.error("Payer not found with ID: {}", fromUserId);
                    return new RuntimeException("Payer not found with ID: " + fromUserId);
                });

        User receiver = userRepository.findById(toUserId)
                .orElseThrow(() -> {
                    log.error("Receiver not found with ID: {}", toUserId);
                    return new RuntimeException("Receiver not found with ID: " + toUserId);
                });

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> {
                    log.error("Expense not found with ID: {}", expenseId);
                    return new RuntimeException("Expense not found with ID: " + expenseId);
                });

        Settlement settlement = new Settlement();
        settlement.setPayer(payer);
        settlement.setReceiver(receiver);
        settlement.setExpense(expense);
        settlement.setAmount(amount);
        settlement.setSettled(false); // default: not settled

        Settlement saved = settlementRepository.save(settlement);
        log.info("Exiting createSettlement with settlementId={}", saved.getId());
        return saved;
    }

    /**
     * Mark a settlement as settled
     *
     * @param settlementId ID of the settlement to mark as settled
     * @return Updated Settlement entity
     */
    @Transactional
    public Settlement settlePayment(Long settlementId, String authHeader) {
        log.info("Entering settlePayment with settlementId={}", settlementId);

        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> {
                    log.error("Settlement not found with ID: {}", settlementId);
                    return new RuntimeException("Settlement not found with ID: " + settlementId);
                });
        if (!settlement.getPayer().getId().equals(getCurrentUserId(authHeader))) {
            throw new RuntimeException("You are not authorized to settle this transaction");
        }

        if (settlement.isSettled()) {
            throw new RuntimeException("Settlement already completed");
        }

        settlement.setSettled(true);
        Settlement updated = settlementRepository.save(settlement);

        log.info("Exiting settlePayment with settlementId={}", updated.getId());
        return updated;
    }

    /**
     * Retrieve all unsettled settlements for a specific user
     *
     * @param userId ID of the user
     * @return List of unsettled Settlement entities
     */
    public List<Settlement> getUserUnsettled(Long userId) {
        log.info("Entering getUserUnsettled for userId={}", userId);

        List<Settlement> unsettled = settlementRepository.findByPayerIdOrReceiverIdAndSettledFalse(userId, userId);

        log.info("Exiting getUserUnsettled with {} settlements found", unsettled.size());
        return unsettled;
    }

    /**
     * Create a settlement by specifying group, payer, and receiver (alternate use-case)
     *
     * @param groupId   ID of the group
     * @param fromUserId Payer user ID
     * @param toUserId   Receiver user ID
     * @param amount     Amount to settle
     * @return Saved Settlement entity
     */
    @Transactional
    public Settlement settleAmount(Long groupId, Long fromUserId, Long toUserId, BigDecimal amount) {
        log.info("Entering settleAmount with groupId={}, fromUserId={}, toUserId={}, amount={}", 
                 groupId, fromUserId, toUserId, amount);

        // Using Java 8 streams for clarity (optional but demonstrates stream usage)
        User payer = userRepository.findById(fromUserId)
                .orElseThrow(() -> new RuntimeException("Payer not found"));
        User receiver = userRepository.findById(toUserId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        // Create a new settlement without an expense reference
        Settlement settlement = new Settlement();
        settlement.setPayer(payer);
        settlement.setReceiver(receiver);
        settlement.setAmount(amount);
        settlement.setSettled(false);
        settlement.setGroup(groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found")));

        Settlement saved = settlementRepository.save(settlement);
        log.info("Exiting settleAmount with settlementId={}", saved.getId());
        return saved;
    }
    public Long getCurrentUserId(String authHeader) {
        String email = jwtConfig.extractEmail(authHeader); 
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
        return user.getId();
    }
    
}

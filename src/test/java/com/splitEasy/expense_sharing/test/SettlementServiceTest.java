package com.splitEasy.expense_sharing.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.spliteasy.expense_sharing.config.JwtConfig;
import com.spliteasy.expense_sharing.entity.Expense;
import com.spliteasy.expense_sharing.entity.Settlement;
import com.spliteasy.expense_sharing.entity.User;
import com.spliteasy.expense_sharing.repository.ExpenseRepository;
import com.spliteasy.expense_sharing.repository.SettlementRepository;
import com.spliteasy.expense_sharing.repository.UserRepository;
import com.spliteasy.expense_sharing.service.SettlementService;

class SettlementServiceTest {

    @Mock
    private SettlementRepository settlementRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private SettlementService settlementService;
    @InjectMocks
    private JwtConfig jwtConfig;

    private User payer;
    private User receiver;
    private Expense expense;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        payer = new User();
        payer.setId(1L);
        payer.setName("Payer User");

        receiver = new User();
        receiver.setId(2L);
        receiver.setName("Receiver User");

        expense = new Expense();
        expense.setId(100L);
    }

    @Test
    void testCreateSettlement_success() {
        when(userRepository.findById(payer.getId())).thenReturn(Optional.of(payer));
        when(userRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));
        when(expenseRepository.findById(expense.getId())).thenReturn(Optional.of(expense));
        when(settlementRepository.save(any(Settlement.class))).thenAnswer(i -> i.getArgument(0));

        BigDecimal amount = BigDecimal.valueOf(150);
        Settlement result = settlementService.createSettlement(payer.getId(), receiver.getId(), expense.getId(), amount);

        assertThat(result.getPayer()).isEqualTo(payer);
        assertThat(result.getReceiver()).isEqualTo(receiver);
        assertThat(result.getAmount()).isEqualTo(amount);
        assertThat(result.isSettled()).isFalse();

        verify(settlementRepository, times(1)).save(any(Settlement.class));
    }

    @Test
    void testCreateSettlement_userNotFound() {
        when(userRepository.findById(payer.getId())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
            settlementService.createSettlement(payer.getId(), receiver.getId(), expense.getId(), BigDecimal.valueOf(100))
        );
    }

    @Test
    void testSettlePayment_success() {
        Settlement settlement = new Settlement();
        settlement.setId(10L);
        settlement.setSettled(false);

        when(settlementRepository.findById(10L)).thenReturn(Optional.of(settlement));
        when(settlementRepository.save(settlement)).thenReturn(settlement);

        Settlement result = settlementService.settlePayment(10L,"email");
        assertThat(result.isSettled()).isTrue();
        verify(settlementRepository).save(settlement);
    }

    @Test
    void testSettlePayment_notFound() {
        when(settlementRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> settlementService.settlePayment(999L,"email"));
    }

    @Test
    void testGetUserUnsettled_success() {
        when(settlementRepository.findByPayerIdOrReceiverIdAndSettledFalse(1L, 1L))
                .thenReturn(List.of(new Settlement(), new Settlement()));

        List<Settlement> results = settlementService.getUserUnsettled(1L);
        assertThat(results).hasSize(2);
    }
}

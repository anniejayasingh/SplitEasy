package com.splitEasy.expense_sharing.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import com.spliteasy.expense_sharing.entity.Expense;
import com.spliteasy.expense_sharing.entity.Group;
import com.spliteasy.expense_sharing.entity.Settlement;
import com.spliteasy.expense_sharing.entity.User;
import com.spliteasy.expense_sharing.repository.ExpenseRepository;
import com.spliteasy.expense_sharing.repository.GroupRepository;
import com.spliteasy.expense_sharing.repository.SettlementRepository;
import com.spliteasy.expense_sharing.repository.UserRepository;
import com.spliteasy.expense_sharing.service.ExpenseService;

import model.Role;

/**
 * Unit tests for ExpenseService
 */
@SpringBootTest
class ExpenseServiceTest {

    @InjectMocks
    private ExpenseService expenseService;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private SettlementRepository settlementRepository;

    private User user1;
    private User user2;
    private Group group;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        user1 = new User(1L, "Alice", "alice@example.com","", Role.ADMIN);
        user2 = new User(2L, "Bob", "bob@example.com","",  Role.USER);

        group = new Group();
        group.setId(1L);
        group.setName("Test Group");
        group.setMembers(new HashSet<>(Arrays.asList(user1, user2)));
    }

    @Test
    void testAddExpense_createsExpenseAndSettlements() {
        Expense expenseRequest = new Expense();
        expenseRequest.setDescription("Lunch");
        expenseRequest.setAmount(BigDecimal.valueOf(100));
        expenseRequest.setGroup(group);
        expenseRequest.setParticipants(new HashSet<>(Collections.singletonList(user2)));

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(settlementRepository.save(any(Settlement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Expense savedExpense = expenseService.addExpense(expenseRequest,"email");

        assertNotNull(savedExpense);
        assertEquals("Lunch", savedExpense.getDescription());
        verify(settlementRepository, times(1)).save(any(Settlement.class));
    }

    @Test
    void testGetExpensesByGroup_returnsExpenses() {
        Expense expense = new Expense();
        expense.setId(1L);
        expense.setDescription("Dinner");
        expense.setGroup(group);

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(expenseRepository.findByGroup(group)).thenReturn(Collections.singletonList(expense));

        List<Expense> expenses = expenseService.getExpensesByGroup(1L);

        assertEquals(1, expenses.size());
        assertEquals("Dinner", expenses.get(0).getDescription());
    }

    @Test
    void testDeleteExpense_callsRepository() {
        when(expenseRepository.existsById(1L)).thenReturn(true);

        expenseService.deleteExpense(1L);

        verify(expenseRepository, times(1)).deleteById(1L);
    }

    @Test
    void testCalculateBalances_returnsCorrectBalances() {
        Expense expense = new Expense();
        expense.setAmount(BigDecimal.valueOf(100));
        expense.setPaidBy(user1);
        expense.setParticipants(new HashSet<>(Arrays.asList(user1, user2)));
        expense.setGroup(group);

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(expenseRepository.findByGroup(group)).thenReturn(Collections.singletonList(expense));
        when(settlementRepository.findByGroupAndSettledFalse(group)).thenReturn(Collections.emptyList());

        Map<User, BigDecimal> balances = expenseService.calculateBalances(1L);

        assertEquals(BigDecimal.valueOf(50.00).setScale(2), balances.get(user1));
        assertEquals(BigDecimal.valueOf(-50.00).setScale(2), balances.get(user2));
    }
}

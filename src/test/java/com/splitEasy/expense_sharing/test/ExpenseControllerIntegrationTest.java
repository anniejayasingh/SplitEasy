package com.splitEasy.expense_sharing.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spliteasy.expense_sharing.entity.Expense;
import com.spliteasy.expense_sharing.entity.Group;
import com.spliteasy.expense_sharing.entity.User;
import com.spliteasy.expense_sharing.repository.ExpenseRepository;
import com.spliteasy.expense_sharing.repository.GroupRepository;
import com.spliteasy.expense_sharing.repository.UserRepository;
import com.spliteasy.expense_sharing.service.ExpenseService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
class ExpenseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private ExpenseRepository expenseRepository;
    @Autowired
    private ExpenseService expenseService;

    private User user1, user2, user3;
    private Group group;

    @BeforeEach
    void setup() {
        expenseRepository.deleteAll();
        groupRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users using setters
        user1 = new User();
        user1.setName("User1");
        user1.setEmail("user1@test.com");
        user1 = userRepository.save(user1);

        user2 = new User();
        user2.setName("User2");
        user2.setEmail("user2@test.com");
        user2 = userRepository.save(user2);

        user3 = new User();
        user3.setName("User3");
        user3.setEmail("user3@test.com");
        user3 = userRepository.save(user3);

        // Create a test group using setters
        group = new Group();
        group.setName("Trip Group");
        group.setMembers(new HashSet<>(Arrays.asList(user1, user2, user3)));
        group = groupRepository.save(group);
    }

    @Test
    @DisplayName("Add Expense - Equal Split")
    void testAddExpenseEqualSplit() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("groupId", group.getId());
        request.put("payerId", user1.getId());
        request.put("amount", 300.0);
        request.put("description", "Dinner");
        request.put("splitType", "EQUAL");

        String jsonRequest = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().string("Expense added successfully"));

        // Verify balances using repository method
        Map<User, BigDecimal> balances = expenseService.calculateBalances(group.getId());
        assertThat(balances.get(user1)).isEqualByComparingTo(BigDecimal.valueOf(200));
        assertThat(balances.get(user2)).isEqualByComparingTo(BigDecimal.valueOf(-100));
        assertThat(balances.get(user3)).isEqualByComparingTo(BigDecimal.valueOf(-100));
    }

    @Test
    @DisplayName("✅ Add Expense - Custom Split")
    void testAddExpenseCustomSplit() throws Exception {
        Map<String, Double> customSplit = new HashMap<>();
        customSplit.put(user1.getId().toString(), 100.0);
        customSplit.put(user2.getId().toString(), 150.0);
        customSplit.put(user3.getId().toString(), 50.0);

        Map<String, Object> request = new HashMap<>();
        request.put("groupId", group.getId());
        request.put("payerId", user1.getId());
        request.put("amount", 300.0);
        request.put("description", "Hotel");
        request.put("splitType", "CUSTOM");
        request.put("customSplit", customSplit);

        String jsonRequest = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().string("Expense added successfully"));

        Map<User, BigDecimal> balances = expenseService.calculateBalances(group.getId());
        assertThat(balances.get(user1)).isEqualByComparingTo(BigDecimal.valueOf(200));
        assertThat(balances.get(user2)).isEqualByComparingTo(BigDecimal.valueOf(-150));
        assertThat(balances.get(user3)).isEqualByComparingTo(BigDecimal.valueOf(-50));
    }

    @Test
    @DisplayName("✅ Get Balances for Group")
    void testGetBalances() throws Exception {
        // Add an equal expense
        Expense expense = new Expense();
        expense.setGroup(group);
        expense.setPaidBy(user1);
        expense.setAmount(BigDecimal.valueOf(300));
        expense.setDescription("Dinner");
        expenseRepository.save(expense);

        mockMvc.perform(get("/expenses/group/+ group.getId()/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['" + user1.getId() + "']").value(200))
                .andExpect(jsonPath("$['" + user2.getId() + "']").value(-100))
                .andExpect(jsonPath("$['" + user3.getId() + "']").value(-100));
    }
}

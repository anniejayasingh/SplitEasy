package com.expensesharingapp.spliteasy.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseSplit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User who owes this amount
     */
    @ManyToOne
    private User user;

    /**
     * Amount owed by this user
     */
    private BigDecimal amount;

    /**
     * Reference to the parent expense
     */
    @ManyToOne
    private Expense expense;
}

package com.expensesharingapp.spliteasy.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.expensesharingapp.spliteasy.util.MapToJsonConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "expenses")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Description of the expense
     */
    private String description;

    /**
     * Total amount of the expense
     */
    private BigDecimal totalAmount;

    /**
     * User who paid the expense
     */
    @ManyToOne
    private User paidBy;

    /**
     * Group associated with this expense
     */
    @ManyToOne
    private Group group;

    /**
     * Flag indicating whether the split is equal or custom
     */
    private Boolean isEqualSplit;

    /**
     * Users who share the expense (if equal split, all members included)
     */
    @ManyToMany
    private List<User> participants;

    /**
     * User splits for the expense.
     * Key → User ID, Value → Amount owed by that user.
     * Stored as a JSON column in DB for simplicity.
     */
    @Convert(converter = MapToJsonConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<Long, BigDecimal> customSplits;

    /**
     * Timestamp when the expense was created
     */
    private LocalDateTime createdOn;
}

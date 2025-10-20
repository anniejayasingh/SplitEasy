package com.expensesharingapp.spliteasy.repository;


import com.expensesharingapp.spliteasy.entity.Expense;
import com.expensesharingapp.spliteasy.entity.Settlement;
import com.expensesharingapp.spliteasy.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Settlement entity.
 */
@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    /**
     * Find all settlements for a given expense.
     */
    List<Settlement> findByExpense(Expense expense);

    /**
     * Find all settlements where a user is either payer or receiver.
     */
    List<Settlement> findByPayerOrReceiver(User payer, User receiver);

    @Query("SELECT s FROM Settlement s WHERE s.payer.id = :userId OR s.receiver.id = :userId")
    List<Settlement> findByUserId(@Param("userId") Long userId);
}


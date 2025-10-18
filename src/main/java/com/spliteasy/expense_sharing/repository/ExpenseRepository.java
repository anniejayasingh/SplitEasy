package com.spliteasy.expense_sharing.repository;

import com.spliteasy.expense_sharing.entity.Expense;
import com.spliteasy.expense_sharing.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // Fetch all expenses for a specific group
    List<Expense> findByGroup(Group group);
}

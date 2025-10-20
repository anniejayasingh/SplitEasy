package com.expensesharingapp.spliteasy.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.expensesharingapp.spliteasy.entity.Expense;
import com.expensesharingapp.spliteasy.entity.Group;

public interface ExpenseRepository extends JpaRepository<Expense, Long>{
	 List<Expense> findByGroup(Group group);

}

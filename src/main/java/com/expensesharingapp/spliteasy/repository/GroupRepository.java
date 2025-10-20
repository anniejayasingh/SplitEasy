package com.expensesharingapp.spliteasy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.expensesharingapp.spliteasy.entity.Group;

public interface GroupRepository extends JpaRepository<Group, Long> {
}

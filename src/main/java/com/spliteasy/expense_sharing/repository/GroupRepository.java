package com.spliteasy.expense_sharing.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.spliteasy.expense_sharing.entity.Group;

public interface GroupRepository extends JpaRepository<Group, Long> {


}

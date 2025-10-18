package com.spliteasy.expense_sharing.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.spliteasy.expense_sharing.entity.Group;
import com.spliteasy.expense_sharing.entity.Settlement;

import jakarta.persistence.LockModeType;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    //List<Settlement> findByFromUserIdOrToUserId(Long payerId, Long receiverId);
    List<Settlement> findByPayerIdOrReceiverIdAndSettledFalse(Long payerId, Long receiverId);
    List<Settlement> findByGroupAndSettledFalse(Group group);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Settlement s WHERE s.id = :id")
    Optional<Settlement> findByIdForUpdate(@Param("id") Long id);
}

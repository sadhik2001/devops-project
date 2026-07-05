package com.payg.gateway.service;

import com.payg.gateway.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    List<Transaction> findByMerchantId(String merchantId);

    Optional<Transaction> findByReferenceId(String referenceId);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.status = 'SUCCESS'")
    Long countSuccessfulTransactions();
}

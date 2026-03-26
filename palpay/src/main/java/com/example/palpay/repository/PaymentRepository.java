package com.example.palpay.repository;

import com.example.palpay.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    public Payment findByauthId(String authId);
}

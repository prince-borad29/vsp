package com.vsp.authservice.repository;

import com.vsp.authservice.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserAccount,String> {
    Optional<UserAccount> findByEmail(String email);
}

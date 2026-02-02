package com.kb.healthcare.common.business.user.repository;

import com.kb.healthcare.common.business.user.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);

}

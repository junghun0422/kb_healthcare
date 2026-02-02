package com.kb.healthcare.common.business.user.service;


import com.kb.healthcare.common.business.user.domain.entity.User;
import com.kb.healthcare.common.business.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository repository;

    public User findByEmail(String email) {
        return repository.findByEmail(email);
    }

    public void saveUser(User user) { repository.save(user); }

    public Optional<User> findById(Long id) { return repository.findById(id); }

}
package com.Codexa.Codexa.repository;

import com.Codexa.Codexa.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import com.Codexa.Codexa.entity.User;

import java.util.List;

public interface SubmissionRepository
        extends JpaRepository<Submission, Long> {
    List<Submission> findByUser(User user);
}
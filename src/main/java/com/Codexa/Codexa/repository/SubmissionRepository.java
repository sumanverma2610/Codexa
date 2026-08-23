package com.Codexa.Codexa.repository;

import com.Codexa.Codexa.entity.Submission;
import com.Codexa.Codexa.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository
        extends JpaRepository<Submission, Long> {

    Page<Submission> findByUser(
            User user,
            Pageable pageable
    );
}
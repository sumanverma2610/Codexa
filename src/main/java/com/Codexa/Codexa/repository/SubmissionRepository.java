package com.Codexa.Codexa.repository;

import com.Codexa.Codexa.entity.Submission;
import com.Codexa.Codexa.entity.SubmissionStatus;
import com.Codexa.Codexa.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository
        extends JpaRepository<Submission, Long> {

    // All submissions of a user
    Page<Submission> findByUser(
            User user,
            Pageable pageable
    );

    // Submissions of a user with specific status
    Page<Submission> findByUserAndStatus(
            User user,
            SubmissionStatus status,
            Pageable pageable
    );
    long countByUser(User user);

    long countByUserAndStatus(
            User user,
            SubmissionStatus status
    );
    Page<Submission> findAllByOrderByIdDesc(
            Pageable pageable
    );

    Page<Submission> findByStatus(
            SubmissionStatus status,
            Pageable pageable
    );}
package com.Codexa.Codexa.repository;

import com.Codexa.Codexa.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository
        extends JpaRepository<Submission, Long> {
}
package com.Codexa.Codexa.repository;

import com.Codexa.Codexa.entity.Problem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemRepository extends JpaRepository<Problem, Long> {

    Page<Problem> findByTitleContainingIgnoreCase(
            String title,
            Pageable pageable
    );

    Page<Problem> findByDifficultyIgnoreCase(
            String difficulty,
            Pageable pageable
    );

    Page<Problem> findByTitleContainingIgnoreCaseAndDifficultyIgnoreCase(
            String title,
            String difficulty,
            Pageable pageable
    );
}
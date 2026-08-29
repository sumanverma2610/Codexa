package com.Codexa.Codexa.service;

import com.Codexa.Codexa.dto.DashboardResponse;
import com.Codexa.Codexa.entity.SubmissionStatus;
import com.Codexa.Codexa.entity.User;
import com.Codexa.Codexa.exception.ResourceNotFoundException;
import com.Codexa.Codexa.repository.SubmissionRepository;
import com.Codexa.Codexa.repository.UserRepository;

import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;

    public DashboardService(
            SubmissionRepository submissionRepository,
            UserRepository userRepository) {

        this.submissionRepository = submissionRepository;
        this.userRepository = userRepository;
    }

    public DashboardResponse getDashboard(
            String email) {

        // Find user

        User user =
                userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"
                                ));

        // Create response

        DashboardResponse response =
                new DashboardResponse();

        // Total submissions

        long total =
                submissionRepository.countByUser(
                        user
                );

        // Accepted

        long accepted =
                submissionRepository.countByUserAndStatus(
                        user,
                        SubmissionStatus.ACCEPTED
                );

        // Unique problems solved

        long problemsSolved =
                submissionRepository .countDistinctProblemByUserAndStatus(
                        user, SubmissionStatus.ACCEPTED
                );

        // Wrong Answer

        long wrongAnswer =
                submissionRepository.countByUserAndStatus(
                        user,
                        SubmissionStatus.WRONG_ANSWER
                );

        // Compilation Error

        long compilationError =
                submissionRepository.countByUserAndStatus(
                        user,
                        SubmissionStatus.COMPILATION_ERROR
                );

        // Runtime Error

        long runtimeError =
                submissionRepository.countByUserAndStatus(
                        user,
                        SubmissionStatus.RUNTIME_ERROR
                );

        // Time Limit

        long timeLimit =
                submissionRepository.countByUserAndStatus(
                        user,
                        SubmissionStatus.TIME_LIMIT_EXCEEDED
                );

        // Set values

        response.setTotalSubmissions(total);
        response.setProblemsSolved(problemsSolved);
        response.setAcceptedSubmissions(accepted);

        response.setWrongAnswers(wrongAnswer);

        response.setCompilationErrors(
                compilationError
        );

        response.setRuntimeErrors(
                runtimeError
        );

        response.setTimeLimitExceeded(
                timeLimit
        );

        // Acceptance rate

        if (total > 0) {

            double rate =
                    ((double) accepted / total) * 100;

            response.setAcceptanceRate(
                    Math.round(rate * 100.0) / 100.0
            );

        } else {

            response.setAcceptanceRate(0.0);
        }

        return response;
    }
}
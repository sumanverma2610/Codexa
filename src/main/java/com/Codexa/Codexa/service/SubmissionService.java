package com.Codexa.Codexa.service;

import com.Codexa.Codexa.dto.CreateSubmissionRequest;
import com.Codexa.Codexa.dto.SubmissionResponse;
import com.Codexa.Codexa.dto.SubmissionStatsResponse;
import com.Codexa.Codexa.entity.Problem;
import com.Codexa.Codexa.entity.Submission;
import com.Codexa.Codexa.entity.SubmissionStatus;
import com.Codexa.Codexa.entity.TestCase;
import com.Codexa.Codexa.entity.User;
import com.Codexa.Codexa.exception.ForbiddenException;
import com.Codexa.Codexa.exception.ResourceNotFoundException;
import com.Codexa.Codexa.executor.CodeExecutorService;
import com.Codexa.Codexa.executor.ExecutionResult;
import com.Codexa.Codexa.repository.ProblemRepository;
import com.Codexa.Codexa.repository.SubmissionRepository;
import com.Codexa.Codexa.repository.TestCaseRepository;
import com.Codexa.Codexa.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubmissionService {

    private final CodeExecutorService codeExecutorService;
    private final SubmissionRepository submissionRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;
    private final TestCaseRepository testCaseRepository;

    public SubmissionService(
            CodeExecutorService codeExecutorService,
            SubmissionRepository submissionRepository,
            ProblemRepository problemRepository,
            UserRepository userRepository,
            TestCaseRepository testCaseRepository) {

        this.codeExecutorService = codeExecutorService;
        this.submissionRepository = submissionRepository;
        this.problemRepository = problemRepository;
        this.userRepository = userRepository;
        this.testCaseRepository = testCaseRepository;
    }

    // CREATE SUBMISSION

    public Submission createSubmission(
            CreateSubmissionRequest request,
            String email) {

        // 1. Find problem

        Problem problem =
                problemRepository.findById(
                        request.getProblemId()
                ).orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Problem not found"
                        ));

        // 2. Find user

        User user =
                userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"
                                ));

        // 3. Create submission

        Submission submission =
                new Submission();

        submission.setCode(
                request.getCode()
        );

        submission.setLanguage(
                request.getLanguage()
        );

        submission.setProblem(
                problem
        );

        submission.setUser(
                user
        );

        // 4. Initially RUNNING

        submission.setStatus(
                SubmissionStatus.RUNNING
        );

        // 5. Save submission

        Submission savedSubmission =
                submissionRepository.save(
                        submission
                );

        // 6. Get test cases

        List<TestCase> testCases =
                testCaseRepository.findByProblemId(
                        problem.getId()
                );

        // 7. No test cases

        if (testCases.isEmpty()) {

            savedSubmission.setStatus(
                    SubmissionStatus.WRONG_ANSWER
            );

            savedSubmission.setResult(
                    "No test cases found for this problem"
            );

            return submissionRepository.save(
                    savedSubmission
            );
        }

        // 8. Run every test case

        for (TestCase testCase : testCases) {

            ExecutionResult result =
                    codeExecutorService.execute(
                            savedSubmission.getCode(),
                            testCase.getInput()
                    );

            // Execution failed

            if (result.getStatus() !=
                    SubmissionStatus.ACCEPTED) {

                savedSubmission.setStatus(
                        result.getStatus()
                );

                savedSubmission.setResult(
                        result.getOutput()
                );

                return submissionRepository.save(
                        savedSubmission
                );
            }

            // Compare output

            String actualOutput =
                    result.getOutput().trim();

            String expectedOutput =
                    testCase
                            .getExpectedOutput()
                            .trim();

            if (!actualOutput.equals(
                    expectedOutput
            )) {

                savedSubmission.setStatus(
                        SubmissionStatus.WRONG_ANSWER
                );

                savedSubmission.setResult(
                        "Expected: "
                                + expectedOutput
                                + "\nActual: "
                                + actualOutput
                );

                return submissionRepository.save(
                        savedSubmission
                );
            }
        }

        // =================================================
        // All test cases passed
        // =================================================

        savedSubmission.setStatus(
                SubmissionStatus.ACCEPTED
        );

        savedSubmission.setResult(
                "All test cases passed"
        );

        return submissionRepository.save(
                savedSubmission
        );
    }

    // GET MY SUBMISSIONS

    public Page<SubmissionResponse> getMySubmissions(
            String email,
            int page,
            int size,
            SubmissionStatus status) {

        User user =
                userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"
                                ));

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by("id").descending()
                );

        Page<Submission> submissions;

        if (status != null) {

            submissions =
                    submissionRepository.findByUserAndStatus(
                            user,
                            status,
                            pageable
                    );

        } else {

            submissions =
                    submissionRepository.findByUser(
                            user,
                            pageable
                    );
        }

        return submissions.map(
                this::convertToResponse
        );
    }

    // GET SUBMISSION BY ID

    public SubmissionResponse getSubmissionById(
            Long id,
            String email) {

        Submission submission =
                submissionRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Submission not found"
                                ));

        User user =
                userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"
                                ));

        // User can only access his own submission

        if (!submission.getUser()
                .getId()
                .equals(user.getId())) {

            throw new ForbiddenException(
                    "You cannot access this submission"
            );
        }

        return convertToResponse(
                submission
        );
    }

    // CONVERT ENTITY TO RESPONSE

    private SubmissionResponse convertToResponse(
            Submission submission) {

        SubmissionResponse response =
                new SubmissionResponse();

        response.setId(
                submission.getId()
        );

        response.setProblemId(
                submission.getProblem().getId()
        );

        response.setLanguage(
                submission.getLanguage()
        );

        response.setStatus(
                submission.getStatus()
        );

        response.setResult(
                submission.getResult()
        );

        response.setCode(
                submission.getCode()
        );

        return response;
    }

    // SUBMISSION STATISTICS

    public SubmissionStatsResponse getSubmissionStats(
            String email) {

        User user =
                userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"
                                ));

        SubmissionStatsResponse response =
                new SubmissionStatsResponse();

        // Total

        response.setTotal(
                submissionRepository.countByUser(
                        user
                )
        );

        // Accepted

        response.setAccepted(
                submissionRepository.countByUserAndStatus(
                        user,
                        SubmissionStatus.ACCEPTED
                )
        );

        // Wrong Answer

        response.setWrongAnswer(
                submissionRepository.countByUserAndStatus(
                        user,
                        SubmissionStatus.WRONG_ANSWER
                )
        );

        // Compilation Error

        response.setCompilationError(
                submissionRepository.countByUserAndStatus(
                        user,
                        SubmissionStatus.COMPILATION_ERROR
                )
        );

        // Runtime Error

        response.setRuntimeError(
                submissionRepository.countByUserAndStatus(
                        user,
                        SubmissionStatus.RUNTIME_ERROR
                )
        );

        // Time Limit Exceeded

        response.setTimeLimitExceeded(
                submissionRepository.countByUserAndStatus(
                        user,
                        SubmissionStatus.TIME_LIMIT_EXCEEDED
                )
        );

        // Output Limit Exceeded

        response.setOutputLimitExceeded(
                submissionRepository.countByUserAndStatus(
                        user,
                        SubmissionStatus.OUTPUT_LIMIT_EXCEEDED
                )
        );

        return response;
    }

    // ADMIN - GET ALL SUBMISSIONS

    public Page<SubmissionResponse> getAllSubmissions(
            int page,
            int size,
            SubmissionStatus status) {

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by("id").descending()
                );

        Page<Submission> submissions;

        if (status != null) {

            submissions =
                    submissionRepository.findByStatus(
                            status,
                            pageable
                    );

        } else {

            submissions =
                    submissionRepository.findAllByOrderByIdDesc(
                            pageable
                    );
        }

        return submissions.map(
                this::convertToResponse
        );
    }
}
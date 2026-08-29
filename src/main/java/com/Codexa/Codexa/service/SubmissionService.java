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

        Problem problem =
                problemRepository.findById(
                        request.getProblemId()
                ).orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Problem not found"
                        ));

        User user =
                userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"
                                ));

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

        submission.setStatus(
                SubmissionStatus.RUNNING
        );

        Submission savedSubmission =
                submissionRepository.save(
                        submission
                );

        List<TestCase> testCases =
                testCaseRepository.findByProblemId(
                        problem.getId()
                );

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

        for (TestCase testCase : testCases) {

            ExecutionResult result =
                    codeExecutorService.execute(
                            savedSubmission.getCode(),
                            testCase.getInput()
                    );

            // Save execution details
            savedSubmission.setExecutionTime(
                    result.getExecutionTime()
            );

            savedSubmission.setMemoryUsed(
                    result.getMemoryUsed()
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

        response.setExecutionTime(
                submission.getExecutionTime()
        );

        response.setMemoryUsed(
                submission.getMemoryUsed()
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

        response.setTotal(
                submissionRepository.countByUser(user)
        );

        response.setAccepted(
                submissionRepository.countByUserAndStatus(
                        user,
                        SubmissionStatus.ACCEPTED
                )
        );

        response.setWrongAnswer(
                submissionRepository.countByUserAndStatus(
                        user,
                        SubmissionStatus.WRONG_ANSWER
                )
        );

        response.setCompilationError(
                submissionRepository.countByUserAndStatus(
                        user,
                        SubmissionStatus.COMPILATION_ERROR
                )
        );

        response.setRuntimeError(
                submissionRepository.countByUserAndStatus(
                        user,
                        SubmissionStatus.RUNTIME_ERROR
                )
        );

        response.setTimeLimitExceeded(
                submissionRepository.countByUserAndStatus(
                        user,
                        SubmissionStatus.TIME_LIMIT_EXCEEDED
                )
        );

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
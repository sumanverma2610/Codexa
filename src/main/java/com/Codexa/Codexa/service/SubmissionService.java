package com.Codexa.Codexa.service;

import com.Codexa.Codexa.dto.CreateSubmissionRequest;
import com.Codexa.Codexa.dto.SubmissionResponse;
import com.Codexa.Codexa.entity.Problem;
import com.Codexa.Codexa.entity.Submission;
import com.Codexa.Codexa.entity.SubmissionStatus;
import com.Codexa.Codexa.entity.TestCase;
import com.Codexa.Codexa.entity.User;
import com.Codexa.Codexa.exception.ForbiddenException;
import com.Codexa.Codexa.exception.ResourceNotFoundException;
import com.Codexa.Codexa.executor.CodeExecutorService;
import com.Codexa.Codexa.repository.ProblemRepository;
import com.Codexa.Codexa.repository.SubmissionRepository;
import com.Codexa.Codexa.repository.TestCaseRepository;
import com.Codexa.Codexa.repository.UserRepository;
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

    public Submission createSubmission(
            CreateSubmissionRequest request,
            String email) {

        // 1. Find problem
        Problem problem = problemRepository
                .findById(request.getProblemId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Problem not found"
                        ));

        // 2. Find user
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));

        // 3. Create submission
        Submission submission = new Submission();

        submission.setCode(request.getCode());
        submission.setLanguage(request.getLanguage());
        submission.setProblem(problem);
        submission.setUser(user);

        // 4. Initially PENDING
        submission.setStatus(
                SubmissionStatus.PENDING
        );

        // Save first
        submissionRepository.save(submission);

        // 5. Change status to RUNNING
        submission.setStatus(
                SubmissionStatus.RUNNING
        );

        submissionRepository.save(submission);

        // 6. Get all test cases
        List<TestCase> testCases =
                testCaseRepository.findByProblemId(
                        problem.getId()
                );

        // If problem has no test cases
        if (testCases.isEmpty()) {

            submission.setStatus(
                    SubmissionStatus.WRONG_ANSWER
            );

            submission.setResult(
                    "No test cases found for this problem"
            );

            return submissionRepository.save(
                    submission
            );
        }

        String finalResult = "";

        // 7. Run code against every test case
        for (TestCase testCase : testCases) {

            String executionResult =
                    codeExecutorService.execute(
                            submission.getCode(),
                            testCase.getInput()
                    );

            // 8. Compilation error
            if (executionResult.startsWith(
                    "Compilation Error:"
            )) {

                submission.setStatus(
                        SubmissionStatus.COMPILATION_ERROR
                );

                finalResult = executionResult;

                break;
            }

            // 9. Runtime error
            if (executionResult.startsWith(
                    "Runtime Error:"
            )) {

                submission.setStatus(
                        SubmissionStatus.RUNTIME_ERROR
                );

                finalResult = executionResult;

                break;
            }

            // 10. Time limit exceeded
            if (executionResult.equals(
                    "Time Limit Exceeded"
            )) {

                submission.setStatus(
                        SubmissionStatus.TIME_LIMIT_EXCEEDED
                );

                finalResult = executionResult;

                break;
            }

            // 11. Actual output
            String actualOutput =
                    executionResult.trim();

            // 12. Expected output
            String expectedOutput =
                    testCase.getExpectedOutput().trim();

            // 13. Compare output
            if (!actualOutput.equals(
                    expectedOutput
            )) {

                submission.setStatus(
                        SubmissionStatus.WRONG_ANSWER
                );

                finalResult =
                        "Wrong Answer\n" +
                                "Expected: " +
                                expectedOutput +
                                "\nActual: " +
                                actualOutput;

                break;
            }

            // This test case passed
            finalResult =
                    "Test case passed";
        }

        // 14. If every test case passed
        if (submission.getStatus() ==
                SubmissionStatus.RUNNING) {

            submission.setStatus(
                    SubmissionStatus.ACCEPTED
            );

            finalResult =
                    "All test cases passed";
        }

        // 15. Save result
        submission.setResult(
                finalResult
        );

        return submissionRepository.save(
                submission
        );
    }

    public List<SubmissionResponse> getMySubmissions(
            String email) {

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));

        List<Submission> submissions =
                submissionRepository.findByUser(user);

        return submissions.stream()
                .map(this::convertToResponse)
                .toList();
    }

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
}
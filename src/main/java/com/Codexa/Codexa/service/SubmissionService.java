package com.Codexa.Codexa.service;

import com.Codexa.Codexa.dto.CreateSubmissionRequest;
import com.Codexa.Codexa.entity.Problem;
import com.Codexa.Codexa.entity.Submission;
import com.Codexa.Codexa.entity.SubmissionStatus;
import com.Codexa.Codexa.entity.User;
import com.Codexa.Codexa.executor.CodeExecutorService;
import com.Codexa.Codexa.repository.ProblemRepository;
import com.Codexa.Codexa.repository.SubmissionRepository;
import com.Codexa.Codexa.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubmissionService {

    private final CodeExecutorService codeExecutorService;
    private final SubmissionRepository submissionRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;

    public SubmissionService(
            CodeExecutorService codeExecutorService,
            SubmissionRepository submissionRepository,
            ProblemRepository problemRepository,
            UserRepository userRepository) {

        this.codeExecutorService = codeExecutorService;
        this.submissionRepository = submissionRepository;
        this.problemRepository = problemRepository;
        this.userRepository = userRepository;
    }

    public Submission createSubmission(
            CreateSubmissionRequest request,
            String email) {

        // Find problem
        Problem problem = problemRepository.findById(request.getProblemId())
                .orElseThrow(() ->
                        new RuntimeException("Problem not found"));

        // Find user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        // Create submission
        Submission submission = new Submission();

        submission.setCode(request.getCode());
        submission.setLanguage(request.getLanguage());
        submission.setProblem(problem);
        submission.setUser(user);

        // Initially pending
        submission.setStatus(SubmissionStatus.PENDING);

        // Execute code
        String executionResult =
                codeExecutorService.execute(submission.getCode());

        // Save execution result
        submission.setResult(executionResult);

        // Determine status
        if (executionResult.startsWith("Compilation Error:")) {

            submission.setStatus(
                    SubmissionStatus.COMPILATION_ERROR
            );

        } else if (executionResult.startsWith("Runtime Error:")) {

            submission.setStatus(
                    SubmissionStatus.RUNTIME_ERROR
            );

        } else if (executionResult.equals("Time Limit Exceeded")) {

            submission.setStatus(
                    SubmissionStatus.TIME_LIMIT_EXCEEDED
            );

        } else {

            submission.setStatus(
                    SubmissionStatus.ACCEPTED
            );
        }

        // Save submission
        return submissionRepository.save(submission);
    }
    public List<Submission> getMySubmissions(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return submissionRepository.findByUser(user);
    }
}
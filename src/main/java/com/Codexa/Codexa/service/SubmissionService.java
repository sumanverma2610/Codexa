package com.Codexa.Codexa.service;

import com.Codexa.Codexa.dto.CreateSubmissionRequest;
import com.Codexa.Codexa.entity.Problem;
import com.Codexa.Codexa.entity.Submission;
import com.Codexa.Codexa.entity.SubmissionStatus;
import com.Codexa.Codexa.entity.User;
import com.Codexa.Codexa.repository.ProblemRepository;
import com.Codexa.Codexa.repository.SubmissionRepository;
import com.Codexa.Codexa.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;

    public SubmissionService(
            SubmissionRepository submissionRepository,
            ProblemRepository problemRepository,
            UserRepository userRepository) {

        this.submissionRepository = submissionRepository;
        this.problemRepository = problemRepository;
        this.userRepository = userRepository;
    }

    public Submission createSubmission(
            CreateSubmissionRequest request,
            String email) {

        Problem problem = problemRepository.findById(request.getProblemId())
                .orElseThrow(() ->
                        new RuntimeException("Problem not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Submission submission = new Submission();

        submission.setCode(request.getCode());
        submission.setLanguage(request.getLanguage());
        submission.setProblem(problem);
        submission.setUser(user);
        submission.setStatus(SubmissionStatus.PENDING);

        return submissionRepository.save(submission);
    }
}
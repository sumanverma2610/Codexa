package com.Codexa.Codexa.service;

import com.Codexa.Codexa.dto.CreateTestCaseRequest;
import com.Codexa.Codexa.entity.Problem;
import com.Codexa.Codexa.entity.TestCase;
import com.Codexa.Codexa.repository.ProblemRepository;
import com.Codexa.Codexa.repository.TestCaseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestCaseService {

    private final TestCaseRepository testCaseRepository;
    private final ProblemRepository problemRepository;

    public TestCaseService(
            TestCaseRepository testCaseRepository,
            ProblemRepository problemRepository) {

        this.testCaseRepository = testCaseRepository;
        this.problemRepository = problemRepository;
    }

    public TestCase createTestCase(
            Long problemId,
            CreateTestCaseRequest request) {

        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() ->
                        new RuntimeException("Problem not found"));

        TestCase testCase = new TestCase();

        testCase.setInput(request.getInput());
        testCase.setExpectedOutput(request.getExpectedOutput());
        testCase.setSample(request.isSample());
        testCase.setHidden(request.isHidden());
        testCase.setProblem(problem);

        return testCaseRepository.save(testCase);
    }

    public List<TestCase> getTestCasesByProblem(Long problemId) {

        if (!problemRepository.existsById(problemId)) {
            throw new RuntimeException("Problem not found");
        }

        return testCaseRepository.findByProblemId(problemId);
    }
    public List<TestCase> getSampleTestCases(Long problemId) {

        if (!problemRepository.existsById(problemId)) {
            throw new RuntimeException("Problem not found");
        }

        return testCaseRepository
                .findByProblemIdAndSampleTrue(problemId);
    }
}
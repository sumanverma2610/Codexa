package com.Codexa.Codexa.controller;

import com.Codexa.Codexa.dto.CreateTestCaseRequest;
import com.Codexa.Codexa.dto.TestCaseResponse;
import com.Codexa.Codexa.entity.TestCase;
import com.Codexa.Codexa.service.TestCaseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/problems")
public class TestCaseController {

    private final TestCaseService testCaseService;

    public TestCaseController(TestCaseService testCaseService) {
        this.testCaseService = testCaseService;
    }

    // ADMIN ONLY
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{problemId}/test-cases")
    public ResponseEntity<TestCaseResponse> createTestCase(
            @PathVariable Long problemId,
            @Valid @RequestBody CreateTestCaseRequest request) {

        TestCase testCase =
                testCaseService.createTestCase(
                        problemId,
                        request
                );

        return ResponseEntity.ok(
                convertToResponse(testCase)
        );
    }

    // USER + ADMIN
    // Returns ONLY sample test cases
    @GetMapping("/{problemId}/test-cases")
    public ResponseEntity<List<TestCaseResponse>> getTestCases(
            @PathVariable Long problemId) {

        List<TestCaseResponse> response =
                testCaseService
                        .getSampleTestCases(problemId)
                        .stream()
                        .map(this::convertToResponse)
                        .toList();

        return ResponseEntity.ok(response);
    }

    private TestCaseResponse convertToResponse(
            TestCase testCase) {

        TestCaseResponse response =
                new TestCaseResponse();

        response.setId(testCase.getId());

        response.setInput(
                testCase.getInput()
        );

        response.setSample(
                testCase.isSample()
        );

        return response;
    }
}
 package com.Codexa.Codexa.controller;

import com.Codexa.Codexa.dto.CreateTestCaseRequest;
import com.Codexa.Codexa.entity.TestCase;
import com.Codexa.Codexa.service.TestCaseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/problems/{problemId}/testcases")
public class TestCaseController {

    private final TestCaseService testCaseService;

    public TestCaseController(TestCaseService testCaseService) {
        this.testCaseService = testCaseService;
    }

    @PostMapping
    public ResponseEntity<TestCase> createTestCase(
            @PathVariable Long problemId,
            @Valid @RequestBody CreateTestCaseRequest request) {

        return ResponseEntity.ok(
                testCaseService.createTestCase(problemId, request)
        );
    }

    @GetMapping
    public ResponseEntity<List<TestCase>> getTestCases(
            @PathVariable Long problemId) {

        return ResponseEntity.ok(
                testCaseService.getTestCasesByProblem(problemId)
        );
    }
}

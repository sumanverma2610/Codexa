package com.Codexa.Codexa.service;

import com.Codexa.Codexa.dto.CreateProblemRequest;
import com.Codexa.Codexa.entity.Problem;
import com.Codexa.Codexa.repository.ProblemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ProblemService {

    private final ProblemRepository problemRepository;

    public ProblemService(ProblemRepository problemRepository) {
        this.problemRepository = problemRepository;
    }

    public Problem createProblem(CreateProblemRequest request) {

        Problem problem = new Problem();

        problem.setTitle(request.getTitle());
        problem.setDescription(request.getDescription());
        problem.setDifficulty(request.getDifficulty());
        problem.setConstraints(request.getConstraints());
        problem.setInputFormat(request.getInputFormat());
        problem.setOutputFormat(request.getOutputFormat());

        return problemRepository.save(problem);
    }

    public Page<Problem> getAllProblems(
            int page,
            int size,
            String sortBy,
            String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable =
                PageRequest.of(page, size, sort);

        return problemRepository.findAll(pageable);
    }

    public Page<Problem> searchProblems(
            String search,
            String difficulty,
            int page,
            int size,
            String sortBy,
            String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable =
                PageRequest.of(page, size, sort);

        if (search != null && !search.isBlank()
                && difficulty != null && !difficulty.isBlank()) {

            return problemRepository
                    .findByTitleContainingIgnoreCaseAndDifficultyIgnoreCase(
                            search,
                            difficulty,
                            pageable
                    );
        }

        if (search != null && !search.isBlank()) {

            return problemRepository
                    .findByTitleContainingIgnoreCase(
                            search,
                            pageable
                    );
        }

        if (difficulty != null && !difficulty.isBlank()) {

            return problemRepository
                    .findByDifficultyIgnoreCase(
                            difficulty,
                            pageable
                    );
        }

        return problemRepository.findAll(pageable);
    }

    public Problem getProblemById(Long id) {

        return problemRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Problem not found"));
    }

    public Problem updateProblem(
            Long id,
            CreateProblemRequest request) {

        Problem problem = problemRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Problem not found"));

        problem.setTitle(request.getTitle());
        problem.setDescription(request.getDescription());
        problem.setDifficulty(request.getDifficulty());
        problem.setConstraints(request.getConstraints());
        problem.setInputFormat(request.getInputFormat());
        problem.setOutputFormat(request.getOutputFormat());

        return problemRepository.save(problem);
    }

    public void deleteProblem(Long id) {

        Problem problem = problemRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Problem not found"));

        problemRepository.delete(problem);
    }
}
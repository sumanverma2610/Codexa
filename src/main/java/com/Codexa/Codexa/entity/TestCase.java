package com.Codexa.Codexa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "test_cases")
@Getter
@Setter
@AllArgsConstructor
public class TestCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String input;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String expectedOutput;

    @Column(nullable = false)
    private boolean sample;

    @Column(nullable = false)
    private boolean hidden;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    public TestCase() {
    }
}
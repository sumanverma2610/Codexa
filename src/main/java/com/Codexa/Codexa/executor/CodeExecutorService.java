package com.Codexa.Codexa.executor;

import com.Codexa.Codexa.entity.SubmissionStatus;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class CodeExecutorService {

    private static final int MAX_OUTPUT_SIZE = 1024 * 1024; // 1 MB

    public ExecutionResult execute(String code, String input) {

        Path executionDirectory =
                Paths.get(
                        "execution",
                        UUID.randomUUID().toString()
                );

        try {

            // 1. Create execution directory

            Files.createDirectories(
                    executionDirectory
            );

            // 2. Create Main.java

            Path javaFile =
                    executionDirectory.resolve("Main.java");

            Files.writeString(
                    javaFile,
                    code
            );

            // 3. Compile Java code

            Process compileProcess =
                    new ProcessBuilder(
                            "javac",
                            "Main.java"
                    )
                            .directory(
                                    executionDirectory.toFile()
                            )
                            .redirectErrorStream(true)
                            .start();

            BufferedReader compileReader =
                    new BufferedReader(
                            new InputStreamReader(
                                    compileProcess.getInputStream()
                            )
                    );

            StringBuilder compileOutput =
                    new StringBuilder();

            String line;

            while ((line = compileReader.readLine()) != null) {

                compileOutput
                        .append(line)
                        .append("\n");

                if (compileOutput.length()
                        > MAX_OUTPUT_SIZE) {

                    compileProcess.destroyForcibly();

                    return new ExecutionResult(
                            SubmissionStatus.OUTPUT_LIMIT_EXCEEDED,
                            "Compilation output exceeded 1 MB"
                    );
                }
            }

            int compileExitCode =
                    compileProcess.waitFor();

            // 4. Compilation error

            if (compileExitCode != 0) {

                return new ExecutionResult(
                        SubmissionStatus.COMPILATION_ERROR,
                        compileOutput.toString()
                );
            }

            // 5. Run Java program

            Process runProcess =
                    new ProcessBuilder(
                            "java",
                            "-Xmx128m",
                            "-Xss1m",
                            "-Djava.security.manager=disallow",
                            "Main"
                    )
                            .directory(
                                    executionDirectory.toFile()
                            )
                            .redirectErrorStream(true)
                            .start();

            // 6. Send input

            if (input != null) {

                OutputStream outputStream =
                        runProcess.getOutputStream();

                outputStream.write(
                        input.getBytes(
                                StandardCharsets.UTF_8
                        )
                );

                outputStream.flush();
                outputStream.close();
            }

            // 7. Read output while program is running

            StringBuilder output =
                    new StringBuilder();

            final boolean[] outputLimitExceeded =
                    {false};

            Thread outputReader =
                    new Thread(() -> {

                        try {

                            BufferedReader reader =
                                    new BufferedReader(
                                            new InputStreamReader(
                                                    runProcess
                                                            .getInputStream()
                                            )
                                    );

                            String outputLine;

                            while ((outputLine =
                                    reader.readLine()) != null) {

                                synchronized (output) {

                                    output.append(
                                            outputLine
                                    ).append("\n");

                                    if (output.length()
                                            > MAX_OUTPUT_SIZE) {

                                        outputLimitExceeded[0] =
                                                true;

                                        runProcess
                                                .destroyForcibly();

                                        break;
                                    }
                                }
                            }

                        } catch (Exception ignored) {
                        }
                    });

            outputReader.start();

            // 8. Maximum execution time = 5 seconds

            boolean finished =
                    runProcess.waitFor(
                            5,
                            TimeUnit.SECONDS
                    );

            // 9. Time limit exceeded

            if (!finished) {

                runProcess.destroyForcibly();

                outputReader.join(1000);

                return new ExecutionResult(
                        SubmissionStatus.TIME_LIMIT_EXCEEDED,
                        "Program exceeded 5 seconds"
                );
            }

            // Wait for output reader

            outputReader.join(1000);

            // 10. Output limit exceeded

            if (outputLimitExceeded[0]) {

                return new ExecutionResult(
                        SubmissionStatus.OUTPUT_LIMIT_EXCEEDED,
                        "Program output exceeded 1 MB"
                );
            }

            // 11. Runtime error

            int runExitCode =
                    runProcess.exitValue();

            if (runExitCode != 0) {

                return new ExecutionResult(
                        SubmissionStatus.RUNTIME_ERROR,
                        output.toString()
                );
            }

            // 12. Successful execution

            return new ExecutionResult(
                    SubmissionStatus.ACCEPTED,
                    output.toString()
            );

        } catch (Exception e) {

            return new ExecutionResult(
                    SubmissionStatus.EXECUTION_ERROR,
                    e.getMessage()
            );

        } finally {

            // 13. Delete temporary files

            deleteDirectory(
                    executionDirectory
            );
        }
    }

    private void deleteDirectory(
            Path directory) {

        try {

            if (!Files.exists(directory)) {
                return;
            }

            Files.walk(directory)
                    .sorted(
                            Comparator.reverseOrder()
                    )
                    .forEach(path -> {

                        try {

                            Files.deleteIfExists(
                                    path
                            );

                        } catch (Exception ignored) {
                        }
                    });

        } catch (Exception ignored) {
        }
    }
}
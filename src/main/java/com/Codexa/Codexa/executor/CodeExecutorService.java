package com.Codexa.Codexa.executor;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

@Service
public class CodeExecutorService {

    public String execute(String code) {

        try {
            // 1. Create Java file
            File file = new File("Main.java");

            FileWriter writer = new FileWriter(file);
            writer.write(code);
            writer.close();

            // 2. Compile Java code
            Process compileProcess =
                    new ProcessBuilder("javac", "Main.java")
                            .redirectErrorStream(true)
                            .start();

            BufferedReader compileReader =
                    new BufferedReader(
                            new InputStreamReader(
                                    compileProcess.getInputStream()
                            )
                    );

            StringBuilder compileOutput = new StringBuilder();

            String line;

            while ((line = compileReader.readLine()) != null) {
                compileOutput.append(line).append("\n");
            }

            int compileExitCode = compileProcess.waitFor();

            // 3. Compilation error
            if (compileExitCode != 0) {
                return "Compilation Error:\n" + compileOutput;
            }

            // 4. Run Java program
            Process runProcess =
                    new ProcessBuilder("java", "Main")
                            .redirectErrorStream(true)
                            .start();

            // 5. Wait maximum 5 seconds
            boolean finished =
                    runProcess.waitFor(5, TimeUnit.SECONDS);

            // 6. Timeout
            if (!finished) {

                runProcess.destroyForcibly();

                return "Time Limit Exceeded";
            }

            // 7. Read program output
            BufferedReader runReader =
                    new BufferedReader(
                            new InputStreamReader(
                                    runProcess.getInputStream()
                            )
                    );

            StringBuilder output = new StringBuilder();

            while ((line = runReader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // 8. Runtime error
            int runExitCode = runProcess.exitValue();

            if (runExitCode != 0) {
                return "Runtime Error:\n" + output;
            }

            // 9. Successful execution
            return output.toString();

        } catch (Exception e) {

            return "Execution Error: " + e.getMessage();
        }
    }
}
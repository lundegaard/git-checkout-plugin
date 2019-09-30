package com.github.gastaldi.git;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * @author Lukas Zaruba, lukas.zaruba@lundegaard.eu, 2019
 */
public class ExecutionHelper {

    private ExecutionHelper() {
        // to prevent instantiation
    }

    /**
     * Executes given command in pwd and waits for result
     * IO is inherited from this process
     *
     * @throws IOException when interrupted
     * @throws RuntimeException when process finishes with status other than 0
     */
    public static void executeCommand(Path pwd, String... command) throws IOException {
        Process process = new ProcessBuilder()
                .directory(pwd.toFile())
                .command(command)
                .inheritIO()
                .start();
        try {
            int result = process.waitFor();
            if (result != 0) {
                throw new RuntimeException("Command " + Arrays.toString(command) + " exited with error code " + result);
            }
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

}

package com.github.gastaldi.git;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CredentialStorageHelperTest {

    @Test
    void credentialsTest() throws IOException {
        new CredentialStorageHelper()
                .storeCredentials(getWorkDir(), "https", "bitbucket.org", "username", "password");
        Path output = getWorkDir().resolve("git.store");
        List<String> lines = Files.readAllLines(output);
        assertThat(lines).containsOnly("https://username:password@bitbucket.org");
        Files.delete(output);
    }

    private Path getWorkDir() {
        return Paths.get(System.getProperty("user.dir"));
    }

}
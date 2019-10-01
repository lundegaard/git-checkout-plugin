package com.github.gastaldi.git.impl;

import com.github.gastaldi.git.GitWrapper;
import org.codehaus.plexus.util.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GitWrapperImplTest {

    @Test
    void initializeNullPwd() {
        assertThrows(IllegalArgumentException.class, () -> new GitWrapperImpl().initialize(null));
    }

    @Test
    void ensureDirsBeforeInit() {
        assertThrows(IllegalStateException.class, () -> new GitWrapperImpl().ensureDirectory());
    }

    @Test
    void ensureDirs() throws IOException {
        initWrapper().ensureDirectory();
        assertThat(pwd()).isDirectory();
    }

    @Test
    void ensureDirsAlreadyExists() throws IOException {
        Files.createDirectories(pwd().resolve(".git"));
        assertThrows(IllegalStateException.class, () -> initWrapper().ensureDirectory());
    }

    private GitWrapper initWrapper() {
        return new GitWrapperImpl().initialize(pwd());
    }

    @Test
    void initNullBeforeInit() {
        assertThrows(IllegalStateException.class, () -> new GitWrapperImpl().initRepository());
    }

    @Test
    void init() throws IOException {
        initWrapper()
            .ensureDirectory()
            .initRepository();
        assertThat(pwd().resolve(".git")).isDirectory();
    }

    @Test
    void addRemoteBeforeInit() {
        assertThrows(IllegalStateException.class, () -> new GitWrapperImpl().addRemote("remote"));
    }

    @Test
    void addRemoteNullRemote() {
        assertThrows(IllegalArgumentException.class, () -> initWrapper().addRemote(null));
    }

    @Test
    void addRemoteEmptyRemote() {
        assertThrows(IllegalArgumentException.class, () -> initWrapper().addRemote(""));
    }

    @Test
    void addRemote() throws IOException {
        initWrapper()
            .ensureDirectory()
            .initRepository()
            .addRemote("somerepo");
        List<String> config = readGitConfig();
        assertThat(config).anySatisfy(line -> line.contains("[remote \"origin\"]"));
        assertThat(config).anySatisfy(line -> line.contains("\"url = somerepo\""));
    }

    private List<String> readGitConfig() throws IOException {
        return Files.readAllLines(pwd().resolve(".git/config"));
    }

    @Test
    void configureSparseCheckoutBeforeInit() {
        assertThrows(IllegalStateException.class, () -> new GitWrapperImpl().configureSparseCheckout(Arrays.asList("some", "other")));
    }

    @Test
    void configureSparseCheckoutNullPaths() {
        assertThrows(IllegalArgumentException.class, () -> initWrapper().configureSparseCheckout(null));
    }

    @Test
    void configureSparseCheckoutEmptyPaths() throws IOException {
        initWrapper()
            .ensureDirectory()
            .initRepository()
            .configureSparseCheckout(Arrays.asList());
        assertThat(readGitConfig()).noneMatch(line -> line.contains("core.sparseCheckout = true"));
        assertThat(Files.exists(pwd().resolve(".git/info/sparse-checkout"))).isFalse();
    }

    @Test
    void configureSparseCheckout() throws IOException {
        initWrapper()
            .ensureDirectory()
            .initRepository()
            .configureSparseCheckout(Arrays.asList("some", "other"));
        assertThat(readGitConfig()).anySatisfy(line -> line.contains("core.sparseCheckout = true"));
        List<String> sparse = Files.readAllLines(pwd().resolve(".git/info/sparse-checkout"));
        assertThat(sparse).anySatisfy(line -> line.contains("some"));
        assertThat(sparse).anySatisfy(line -> line.contains("other"));
    }

    @Test
    void cleanBeforeInit() {
        assertThrows(IllegalStateException.class, () -> new GitWrapperImpl().clean());
    }

    @Test
    void cleanNonExistingOK() {
        initWrapper().clean();
    }

    @Test
    void clean() throws IOException {
        Path git = pwd().resolve(".git");
        Files.createDirectories(git);
        Files.write(git.resolve("some.txt"), Collections.singleton("test data"));
        initWrapper().clean();
        assertThat(git).doesNotExist();
    }

    @Test
    void pullBeforeInit() {
        assertThrows(IllegalStateException.class, () -> new GitWrapperImpl().pull("branch"));
    }

    @Test
    void pullNullBranch() {
        assertThrows(IllegalArgumentException.class, () -> initWrapper().pull(null));
    }

    @Test
    void pullEmptyBranch() {
        assertThrows(IllegalArgumentException.class, () -> initWrapper().pull(""));
    }

    @Test
    void pull() {
        ExecutionException e = assertThrows(ExecutionException.class, () -> initWrapper()
                .ensureDirectory()
                .initRepository()
                .pull("master"));
        assertThat(e.getExitCode()).isEqualTo(1); // git executes something, cannot test pull itself without git connection
    }

    private Path pwd() {
        return Paths.get("tests");
    }

    @AfterEach
    void afterEach() throws IOException {
        FileUtils.deleteDirectory(pwd().toFile());
    }

}
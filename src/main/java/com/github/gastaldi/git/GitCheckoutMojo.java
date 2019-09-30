package com.github.gastaldi.git;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.github.gastaldi.git.ExecutionHelper.executeCommand;

/**
 * Clones only specific paths from a Git repository
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Mojo(name = "git-checkout")
public class GitCheckoutMojo extends AbstractMojo {

    private static final String HTTPS_SCHEMA = "https";

    @Parameter(property = "outputDirectory", defaultValue = "${project.build.outputDirectory}")
    private File outputDirectory;

    @Parameter(property = "repository", required = true)
    private String repository;

    @Parameter(property = "branch", defaultValue = "master")
    private String branch;

    @Parameter(property = "paths", required = true)
    private List<String> paths;

    @Parameter(property = "username")
    private String username;

    @Parameter(property = "password")
    private String password;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Path outputDirectoryPath = outputDirectory.toPath();
        try {
            Files.createDirectories(outputDirectoryPath);
            if (Files.exists(outputDirectoryPath.resolve(".git"))) {
                throw new MojoExecutionException("Cannot execute mojo in a directory that already contains a .git directory");
            }
            executeCommand(outputDirectoryPath, "git", "init");
            executeCommand(outputDirectoryPath, "git", "remote", "add", "origin", repository);
            executeCommand(outputDirectoryPath, "git", "config", "core.sparseCheckout", "true");
            Path sparseCheckoutFile = outputDirectoryPath.resolve(".git/info/sparse-checkout");
            Files.write(sparseCheckoutFile, paths);
            handleCredentials();
            executeCommand(outputDirectoryPath, "git", "pull", "origin", branch);
            executeCommand(outputDirectoryPath, "rm", "-rf", ".git");
            getLog().info("Files were checked out in: " + outputDirectoryPath);
        } catch (Exception e) {
            throw new MojoFailureException("Caught IOException in mojo", e);
        }
    }

    private void handleCredentials() throws IOException {
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) return;
        getLog().info("credentials: both username and password present - configuring credentials helper");
        URI uri = URI.create(repository);
        if (!HTTPS_SCHEMA.equals(uri.getScheme())) {
            getLog().warn("skipping credentials as it cannot be used with non-https schemas: " + uri);
            return;
        }
        getLog().info("credentials: host '" + uri.getHost()  + "'");
        new CredentialStorageHelper().storeCredentials(outputDirectory.toPath(), HTTPS_SCHEMA, uri.getHost(), username, password);
        executeCommand(outputDirectory.toPath(), "git", "config", "credential.helper",
                "store --file " + CredentialStorageHelper.GIT_STORE_PATH);
    }

}

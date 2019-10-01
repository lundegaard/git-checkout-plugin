package com.github.gastaldi.git;

import com.github.gastaldi.git.impl.GitWrapperImpl;
import com.github.gastaldi.git.impl.URLCredentialsDecoratorImpl;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Clones only specific paths from a Git repository
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Mojo(name = "git-checkout")
public class GitCheckoutMojo extends AbstractMojo {

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
        try {
            executeInternal(outputDirectory.toPath());
        } catch (Exception e) {
            throw new MojoFailureException("Caught IOException in mojo", e);
        }
    }

    private void executeInternal(Path pwd) throws IOException {
        new GitWrapperImpl()
                .initialize(pwd)
                .ensureDirectory()
                .initRepository()
                .addRemote(new URLCredentialsDecoratorImpl().decorate(repository, username, password))
                .configureSparseCheckout(paths)
                .pull(branch)
                .clean();
    }

}

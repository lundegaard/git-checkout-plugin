package com.github.gastaldi.git;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.util.Collections.singletonList;

/**
 * @author Lukas Zaruba, lukas.zaruba@gmail.com, 2019
 */
public class CredentialStorageHelper {

    public static final String GIT_STORE_PATH = "git.store";

    /**
     * Stores credentials in the git.store file in the form compatible with
     * https://git-scm.com/book/en/v2/Git-Tools-Credential-Storage
     */
    public void storeCredentials(Path pwd, String schema, String host, String username, String password) throws IOException {
        String credentials = schema + "://" + username + ":" + password + "@" + host;
        Files.write(pwd.resolve(GIT_STORE_PATH), singletonList(credentials));
    }

}

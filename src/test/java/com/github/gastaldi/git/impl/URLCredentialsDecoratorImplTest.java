package com.github.gastaldi.git.impl;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class URLCredentialsDecoratorImplTest {

    private static final String URL = "https://github.com/something";

    @Test
    void urlNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new URLCredentialsDecoratorImpl().decorate(null, null, null));
    }

    @Test
    void urlNonHttps() {
        assertThrows(IllegalArgumentException.class,
                () -> new URLCredentialsDecoratorImpl().decorate("git@github.com/something", null, null));
    }

    @Test
    void passwordWithoutUsername() {
        assertThrows(IllegalArgumentException.class,
                () -> new URLCredentialsDecoratorImpl().decorate(URL, null, "pass"));
    }

    @Test
    void noCredentials() {
        assertThat(new URLCredentialsDecoratorImpl().decorate(URL, null, null)).isEqualTo(URL);
    }

    @Test
    void username() {
        assertThat(new URLCredentialsDecoratorImpl().decorate(URL, "user", null))
                .isEqualTo("https://user:@github.com/something");
    }

    @Test
    void usernameAndPassword() {
        assertThat(new URLCredentialsDecoratorImpl().decorate(URL, "user", "pass"))
                .isEqualTo("https://user:pass@github.com/something");
    }

    @Test
    void usernameAndPasswordEscape() {
        assertThat(new URLCredentialsDecoratorImpl().decorate(URL, "user@domain.com", "pass&"))
                .isEqualTo("https://user%40domain.com:pass%26@github.com/something");
    }

}
package com.example;

import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.server.util.HttpHostResolver;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
class TestHttpHostResolver {
    @Inject
    HttpHostResolver resolver;

    @Test
    void resolveHost_without_host_header() {
        var expected = "https://www.example.com";

        var actual = resolver.resolve(HttpRequest.GET(expected));

        assertEquals(expected, actual);
    }

    @Test
    void resolveHost_with_host_header() {
        var expected = "https://www.example.com";

        var actual = resolver.resolve(HttpRequest.GET(expected)
                .header(HttpHeaders.HOST, "www.example.com"));

        assertEquals(expected, actual);
    }
}

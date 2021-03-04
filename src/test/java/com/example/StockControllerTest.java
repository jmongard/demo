package com.example;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.io.socket.SocketUtils;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import io.reactivex.Flowable;
import io.reactivex.Single;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

@MicronautTest

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StockControllerTest implements TestPropertyProvider {
    private static final Logger log = LoggerFactory.getLogger(StockControllerTest.class);

    public static final String SPEC_NAME = "spec.name";
    public static final String SPEC = "test.server";

    private final int port = SocketUtils.findAvailableTcpPort();
    private EmbeddedServer rsServer;

    @BeforeAll
    void startServer() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(SPEC_NAME, SPEC);
        properties.put("micronaut.server.port", port);
        rsServer = ApplicationContext.run(EmbeddedServer.class, properties);
    }

    @AfterAll
    void stopServer() {
        if (rsServer != null) {
            rsServer.stop();
        }
    }

    @Override
    public Map<String, String> getProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("micronaut.http.services.stockclient.url", "http://localhost:" + port);
        return properties;
    }

    @Inject
    @Client("/rest")
    protected RxHttpClient client;

    @Controller()
    @Requires(property = SPEC_NAME, value = SPEC)
    public static class TestServerController {

        @Post("/stock-values")
        public Single<StockValueResult> getStockValues(List<String> ids) {
            log.info("Received query for {} ids", ids.size());
            return Single.just(0L).delay(10, TimeUnit.MILLISECONDS).flatMap(x -> {
                log.info("Returning error: unauthorized ");
                return Single.error(new HttpStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
            });
        }
    }

    @Test
    void test_stock_values_fail_auth() throws InterruptedException {
        List<String> ids = IntStream.range(1, 1999).mapToObj(String::valueOf).collect(Collectors.toList());

        for (var i = 0; i < 100; i += 1) {
            log.info("<<<<<>>>>><<<<<>>>>><<<<<>>>>> Starting test run ({}) <<<<<>>>>><<<<<>>>>><<<<<>>", i+1);
            MutableHttpRequest<List<String>> post = HttpRequest.POST("stock-value", ids);
            BlockingHttpClient blockingHttpClient = client.toBlocking();
            assertThatThrownBy(() -> blockingHttpClient.exchange(post, String.class))
                    .isInstanceOf(HttpClientResponseException.class)
                    ;//.hasMessageContaining("Unauthorized");
            log.info("¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤ End test run ({}) ¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤", i);
            Thread.sleep(5000);
        }
        for (int i = 100; i > 0; i--) {
            log.info("########################## Finished test. Waiting ({}) ###############################", i);
            Thread.sleep(5000);
        }
    }

}
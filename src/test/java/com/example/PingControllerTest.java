package com.example;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.io.socket.SocketUtils;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.http.server.exceptions.HttpServerException;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.*;

import javax.inject.Inject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@MicronautTest

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PingControllerTest implements TestPropertyProvider {
    private static final Logger log = LoggerFactory.getLogger(com.example.PingControllerTest.class);


    @Inject
    protected PingClient client;

    @Controller()
    @Requires(property = SPEC_NAME, value = SPEC)
    public static class PingController {

        @Post("/ping")
        public Single<PingResponse> ping(String sender) {
            log.info("Ping received from {}", sender);
//            PingResponse pingResponse = new PingResponse();
//            pingResponse.setResponse("pong");
//            return pingResponse;
            return Single.<PingResponse>error(
             new HttpStatusException(HttpStatus.UNAUTHORIZED, "testing")).delay(2, TimeUnit.SECONDS);
        }
    }

    @Test
    void testPing() throws Exception {
        for (var i = 0; i < 100; i += 1) {
            final int ii = i;
            int count = 10;
            List<PingResponse> pingResponse = Flowable.range(1, count)
                    .flatMapSingle(j -> client.ping(ii+"-"+j)
                            .onErrorReturn(err-> getPingResponse(err.getMessage())))
                    .toList().blockingGet();
            assertThat(pingResponse).hasSize(count);


            Thread.sleep(10000);
//            stopServer();
//            startServer();

            log.info("//////////////////////" + i);
        }

        while (true) {
            Thread.sleep(10000);
            log.info("press ctrl+c");
        }
    }

    private PingResponse getPingResponse(String response) {
        PingResponse pingResponse1 = new PingResponse();
        pingResponse1.setResponse(response);
        return pingResponse1;
    }


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
        properties.put("micronaut.http.services.pingservice.url", "http://localhost:" + port);
        return properties;
    }
}
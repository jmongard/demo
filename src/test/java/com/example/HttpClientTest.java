package com.example;

import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.reactivex.Flowable;
import io.reactivex.Single;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

@MicronautTest
class HttpClientTest {
    private static final Logger log = LoggerFactory.getLogger(HttpClientTest.class);

    @Inject
    HttpStatusClient apiClient;

    @Client("https://httpstat.us/")
    interface HttpStatusClient {
        @Post(uri = "/403")
        Flowable<DummyResult> forbiddenCall(@QueryValue String firstId);
    }

    public class DummyResult {
        private String id;
        private Double value;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
        }
    }

    @Test
    void test_forbidden_call() throws InterruptedException {
        List<String> collect = IntStream.range(1, 100).mapToObj(String::valueOf).collect(Collectors.toList());

        for (int i = 0; i < 10; i += 1) {

            Single<Map<String, Double>> collectedResult = Flowable.fromIterable(collect)
                    .buffer(10)
                    .flatMap(ids -> {
                        log.info("Calling http service with {} ids", ids.get(0));
                        return apiClient.forbiddenCall(ids.get(0));
                    })
                    .collect(LinkedHashMap::new, (map, result) -> map.put(result.getId(), result.getValue()));
            assertThatThrownBy(collectedResult::blockingGet)
                    .isInstanceOf(Exception.class);

            Thread.sleep(1000);
            log.info("<<<<<>>>>><<<<<>>>>><<<<<>>>>> End test run ({}) <<<<<>>>>><<<<<>>>>><<<<<>>", i+1);
            Thread.sleep(1000);
        }
        for (int i = 100; i > 0; i--) {
            log.info("########################## Finished test. Waiting ({}) ###############################", i);
            Thread.sleep(5000);
        }
    }

}
package com.example;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.annotation.Client;
import io.reactivex.Single;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Client("pingservice")
public interface PingClient {

    @Post("/ping")
    Single<PingResponse> ping(String sender);
}

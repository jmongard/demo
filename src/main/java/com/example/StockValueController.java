package com.example;

import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.annotation.Client;
import io.reactivex.Flowable;
import io.reactivex.Single;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller("/rest")
public class StockValueController  {

    private static final Logger log = LoggerFactory.getLogger(StockValueController.class);

    private final StockClient apiClient;

    public StockValueController(StockClient apiClient) {
        this.apiClient = apiClient;
    }

    @Post(uri="stock-value")
    public Single<Map<String,Double>>getStockValueWareHouse(
            @Body List<String> articleIds) {
        return Flowable.fromIterable(articleIds)
                .buffer(100)
                .flatMap(ids -> {
                    log.info("Calling http service with {} ids", ids.size());
                    return apiClient.getStockValues(ids);
                })
                .collect(LinkedHashMap::new, (map, stock) -> map.put(stock.getArticleId(), stock.getAvailableStockQty()));
    }

    @Client("stockclient")
    public interface StockClient {
        @Post(uri = "/stock-values")
        Flowable<StockValueResult> getStockValues(List<String> ids);
    }
}
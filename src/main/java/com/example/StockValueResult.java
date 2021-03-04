package com.example;

public class StockValueResult {

    private String articleId;
    private Double availableStockQty;

    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }

    public Double getAvailableStockQty() {
        return availableStockQty;
    }

    public void setAvailableStockQty(Double availableStockQty) {
        this.availableStockQty = availableStockQty;
    }
}

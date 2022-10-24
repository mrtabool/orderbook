package org.example.orderbook.model;

import java.util.Objects;

public class OrderBook {
    private Integer price;
    private Integer size;
    private String type;
    private String comment;

    public OrderBook(int price, int size, String type, String comment) {
        this.price = price;
        this.size = size;
        this.type = type;
        this.comment = comment;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderBook orderBook = (OrderBook) o;
        return price == orderBook.price;
    }

    @Override
    public int hashCode() {
        return Objects.hash(price);
    }

    @Override
    public String toString() {
        return "OrderBook{" +
                "price=" + price +
                ", size=" + size +
                ", type='" + type + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }
}

package io.agora.vlive.protocol.model.model;

public class Product {
    public static final int PRODUCT_LAUNCHED = 1;
    public static final int PRODUCT_UNAVAILABLE = 0;

    public String productId;
    public String productName;
    public String description;
    public int price;
    public String thumbnail;
    public int state;

    public Product(Product product) {
        this.productId = product.productId;
        this.productName = product.productName;
        this.description = product.description;
        this.price = product.price;
        this.thumbnail = product.thumbnail;
        this.state = product.state;
    }
}

package com.ecnu.smartmarketing.entity;

/**
 * @author BWZW_171475
 * @description: TODO
 * @date 2022/6/1521:56
 */
public class Advertisement {
    private Long id;
    private String name;
    private String url;
    private Double price;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}

package com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.model;

public class Service {
    private int id;
    private String serviceName;
    private double price;

    public Service(int id, String serviceName, double price) {
        this.id = id;
        this.serviceName = serviceName;
        this.price = price;
    }

    public int getId() { return id; }
    public String getServiceName() { return serviceName; }
    public double getPrice() { return price; }

    public void setId(int id) { this.id = id; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public void setPrice(double price) { this.price = price; }

    public String getPriceFormatted() { return String.format("%,.0f VNĐ", price); }
}

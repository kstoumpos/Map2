package com.example.user.map2;

public class Poi {

    public Double longitude;
    public Double latitude;
    public String title;
    public String category;
    public String order;

    public Poi(String title, Double longitude, Double latitude, String category, String order) {
        this.title = title;
        this.longitude = longitude;
        this.latitude = latitude;
        this.category = category;
        this.order = order;
    }

    public Poi(){}

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }
}

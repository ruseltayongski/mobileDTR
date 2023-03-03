package com.dohro7.officemobiledtr.model;

public class Geopoint {

    double latitude;
    double longitude;

    public  Geopoint (double latitude, double longitude)
    {
        this.latitude=latitude;
        this.longitude=longitude;
    }

    public double getLatitude()
    {
        return  latitude;
    }

    public double getLongitude()
    {
        return  longitude;
    }

}

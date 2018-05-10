package com.sed.andrearubeis.safeemergencydrive;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class Gps {
    private String info;
    private String time_istant;
    private String validity;
    private String latitude;
    private String longitude;
    private String velocity;
    private String pos_date;
    private String vehicle;

    // getter / setter

    public String getInfo(){
        return  info;
    }
    public void setInfo(String nInfo){
        this.info = nInfo;
    }

    public String getTimeIstant(){
        return  time_istant;
    }
    public void setTimeIstant(String nTimeIstant){
        this.time_istant = nTimeIstant;
    }

    public String getValidity(){
        return  validity;
    }
    public void setValidity(String nValidity){
        this.validity = nValidity;
    }

    public String getLatitude(){
        return  latitude;
    }
    public void setLatitude(String nLatitude){
        this.latitude = nLatitude;
    }

    public String getLongitude(){
        return  longitude;
    }
    public void setLongitude(String nLongitude){
        this.longitude = nLongitude;
    }

    public String getVelocity(){
        return  velocity;
    }
    public void setVelocity(String nVelocity){
        this.velocity = nVelocity;
    }

    public String getPosDate(){
        return  pos_date;
    }
    public void setPosDate(String nPosDate){
        this.pos_date = nPosDate;
    }

    public String getVehicle(){
        return  vehicle;
    }
    public void setVehicle(String nVehicle){
        this.vehicle = nVehicle;
    }

    public String toString() {
        return this.info+" , "+this.validity+" , "+this.time_istant+" , "+this.latitude+" , "+this.longitude+" , "+this.velocity+" , "+this.pos_date + " , " + this.vehicle;
    }

    public LatLng getPosition() {
        String lat = this.latitude.split(",")[0];
        String lon = this.longitude.split(",")[0];
        Double latDeg = Double.parseDouble(lat.substring(0,2));
        Double latMin = Double.parseDouble(lat.substring(2))/60;
        latDeg = latDeg + latMin;

        Double lonDeg = Double.parseDouble(lon.substring(0,3));
        Double lonMin = Double.parseDouble(lon.substring(3))/60;
        lonDeg = lonDeg + lonMin;


        Log.d("Gps" , "latitude  :" + latDeg + "    longitude:  " + lonDeg);
        return new LatLng(latDeg,lonDeg);
    }

}

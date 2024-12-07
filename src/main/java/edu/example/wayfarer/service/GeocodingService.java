package edu.example.wayfarer.service;

public interface GeocodingService {

    String geocoding(String address);
    String reverseGeocoding(double lat, double lng);
}

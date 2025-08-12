
package com.example.fds.util;

public class Geo {
  private static final double R = 6371.0088;

  public static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
    double dLat = Math.toRadians(lat2 - lat1), dLon = Math.toRadians(lon2 - lon1);
    double a = Math.pow(Math.sin(dLat / 2), 2)
        + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.pow(Math.sin(dLon / 2), 2);
    double c = 2 * Math.asin(Math.min(1, Math.sqrt(a)));
    return R * c;
  }
}

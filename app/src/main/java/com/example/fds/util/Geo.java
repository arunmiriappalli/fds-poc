
package com.example.fds.util;

public class Geo {
    // Earth's radius in kilometers
    private static final double R = 6371.0088;

  /**
   * Calculates the great-circle distance between two points on the Earth using the Haversine formula.
   *
   * @param lat1 Latitude of the first point in degrees.
   * @param lon1 Longitude of the first point in degrees.
   * @param lat2 Latitude of the second point in degrees.
   * @param lon2 Longitude of the second point in degrees.
   * @return Distance between the two points in kilometers.
   */
  public static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
    double dLat = Math.toRadians(lat2 - lat1), dLon = Math.toRadians(lon2 - lon1);
    double a = Math.pow(Math.sin(dLat / 2), 2)
        + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.pow(Math.sin(dLon / 2), 2);
    double c = 2 * Math.asin(Math.min(1, Math.sqrt(a)));
    return R * c;
  }
}

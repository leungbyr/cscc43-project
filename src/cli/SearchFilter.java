package cli;

import java.util.ArrayList;

import enums.Amenity;

public class SearchFilter {
  String startDate;
  String endDate;
  int minPrice;
  int maxPrice;
  ArrayList<Amenity> amenities;
  
  public SearchFilter(String startDate, String endDate, int minPrice, int maxPrice, ArrayList<Amenity> amenities) {
    this.startDate = startDate;
    this.endDate = endDate;
    this.minPrice = minPrice;
    this.maxPrice = maxPrice;
    this.amenities = amenities;
  }
}

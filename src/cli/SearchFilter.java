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
    
  }
}

package cli;

import java.util.List;

import enums.Amenity;

public class SearchFilter {
  public String startDate;
  public String endDate;
  public int minPrice;
  public int maxPrice;
  public List<Amenity> amenities;
  
  public SearchFilter(String startDate, String endDate, int minPrice, int maxPrice, List<Amenity> amenities) {
    this.startDate = startDate;
    this.endDate = endDate;
    this.minPrice = minPrice;
    this.maxPrice = maxPrice;
    this.amenities = amenities;
  }
  
}

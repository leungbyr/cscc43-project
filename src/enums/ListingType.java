package enums;

public enum ListingType {
  HOUSE ("Full house"),
  APARTMENT ("Apartment"),
  ROOM ("Room"),
  LOFT ("Loft"),
  TENT ("Tent"),
  CASTLE ("Castle");
  
  private final String name;
  
  private ListingType(String s) {
    name = s;
  }
  
  public String toString() {
    return this.name;
  }
}

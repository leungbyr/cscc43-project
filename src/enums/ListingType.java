package enums;

public enum ListingType {
  HOUSE ("Full house"),
  APARTMENT ("Apartment"),
  ROOM ("Room");
  
  private final String name;
  
  private ListingType(String s) {
    name = s;
  }
  
  public String toString() {
    return this.name;
  }
}

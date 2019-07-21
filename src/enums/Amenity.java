package enums;

public enum Amenity {
  ESSENTIALS ("Essentials"),
  WIFI ("WiFi"),
  AC ("Air conditioning"),
  TOWELS ("Towels"),
  WASHER ("Washing machine"),
  DRYER ("Drying machine"),
  PARKING ("Free parking"),
  FRIDGE ("Refrigerator"),
  STOVE ("Stove"),
  CKESSENTIALS ("Cooking essentials"),
  COFFEE ("Coffee machine"),
  DISHWASHER ("Dishwasher"),
  POOL ("Swimming pool"),
  GYM ("Gym");
  
  private final String name;
  
  private Amenity(String s) {
    name = s;
  }
  
  public String toString() {
    return this.name;
  }
}

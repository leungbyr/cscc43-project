package cli;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import controllers.CommentsController;
import controllers.ListingController;
import controllers.SQLController;
import controllers.SearchController;
import enums.Amenity;
import enums.ListingType;

public class SearchCmd {

  private SQLController sqlMngr = null;
  private Scanner sc = null;
  private String username = null;
  
  protected SearchCmd(SQLController sqlMngr, Scanner sc, String username) {
    this.sqlMngr = sqlMngr;
    this.sc = sc;
    this.username = username;
  }

  public boolean execute() {
    if (sc != null && sqlMngr != null) {
      String input = "";
      int choice = -1;
      do {
        menu(); // Print Menu
        input = sc.nextLine();
        
        try {
          choice = Integer.parseInt(input);
          switch (choice) { // Activate the desired functionality
          case 1:
            this.byVicinity();
            break;
          case 2:
            this.byPostalCode();
            break;
          case 3:
            this.byAddress();
            break;
          default:
            break;
          }
        } catch (NumberFormatException e) {
          input = "-1";
        }
      } while (input.compareTo("0") != 0);

      return true;
    } else {
      System.out.println("");
      System.out.println("Connection could not been established! Bye!");
      System.out.println("");
      return false;
    }
  }
  
  private void byVicinity() {
    String lat, lon;
    int maxDistance = -1;;
    
    do {
      System.out.print("Latitude: ");
      lat = sc.nextLine();
    } while (!lat.matches("(\\-?\\d+(\\.\\d+)?)"));
    
    do {
      System.out.print("Longitude: ");
      lon = sc.nextLine();
    } while (!lon.matches("(\\-?\\d+(\\.\\d+)?)"));
    
    while (maxDistance == -1) {
      System.out.print("Distance in km (default = 50): ");
      String distInput = sc.nextLine();
      if (distInput.equals("")) {
        maxDistance = 50;
        break;
      }
      
      try {
        maxDistance = Integer.parseInt(distInput);
      } catch (NumberFormatException e) {
        // Looping and asking for input again
      }
    }
  
    int sort;
    do {
      System.out.println("Sort by:");
      System.out.println("1. Price (ascending)");
      System.out.println("2. Price (descending)");
      System.out.println("3. Distance");
      System.out.print("Choose one of the previous options [1-3]: ");
      String sortInput = sc.nextLine();
      
      try {
        sort = Integer.parseInt(sortInput);
      } catch (NumberFormatException e) {
        sort = -1;
      }
    } while (sort < 1 || sort > 3);
    
    SearchFilter filters = getFilters();
    SearchController searchMngr = new SearchController();
    ResultSet rs = searchMngr.byVicinity(lat, lon, maxDistance, sort, filters);
    System.out.println("=======SEARCH RESULTS=======");
    this.showListings(rs, true);
  }
  
  private void byPostalCode() {
    String postalCode;
    do {
      System.out.print("Postal code: ");
      postalCode = sc.nextLine();
      
      if (postalCode.contains(" ")) {
        postalCode = postalCode.replace(" ", "");
      }
    } while (!postalCode.matches("^(?!.*[DFIOQU])[A-VXY][0-9][A-Z]?[0-9][A-Z][0-9]$"));
    
    int sort;
    do {
      System.out.println("Sort by:");
      System.out.println("1. Price (ascending)");
      System.out.println("2. Price (descending)");
      System.out.println("3. Date (ascending)");
      System.out.println("4. Date (descending)");
      System.out.print("Choose one of the previous options [1-4]: ");
      String sortInput = sc.nextLine();
      
      try {
        sort = Integer.parseInt(sortInput);
      } catch (NumberFormatException e) {
        sort = -1;
      }
    } while (sort < 1 || sort > 4);
    
    SearchFilter filters = getFilters();
    SearchController searchMngr = new SearchController();
    ResultSet rs = searchMngr.byPostalCode(postalCode, filters, sort);
    this.showListings(rs, false);
  }

  private void byAddress() {
    System.out.print("Address: ");
    String address = sc.nextLine();
    SearchController searchMngr = new SearchController();
    ResultSet rs = searchMngr.byAddress(address);
    this.showListings(rs, false);
  }

  protected void showListings(ResultSet rs, boolean showDistance) {
    int i = 0;
    try {
      for (i = 1; rs.next(); i++) {
          String rsLat = rs.getString("lat");
          String rsLon = rs.getString("lon");
          String type = ListingType.valueOf(rs.getString("type")).toString();
          String address = rs.getString("address");
          String city = rs.getString("city");
          String country = rs.getString("country");
          String postal = rs.getString("postal");
          String date = rs.getString("date");
          BigDecimal price = rs.getBigDecimal("price");
          String distString;
          if (showDistance) {
            BigDecimal distance = rs.getBigDecimal("distance").setScale(2, RoundingMode.HALF_EVEN);
            distString = ", Distance: " + distance.toString() + "km";
          } else {
            distString = "";
          }
          
          System.out.println(i + ". " + type + " at "  + address + ", " + city + ", " + country + ", " + postal + " (" + rsLat + ", " + rsLon + ")");
          System.out.print(String.format("%" + ((int)(Math.log10(i)) + 3) + "s", ""));
          System.out.println("Date: " + date + ", Price: $" + price.setScale(2, RoundingMode.HALF_DOWN) + distString);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    
    int num = -1;
    while (num < 0) {
      System.out.print("Enter 0 to go back or one of the previous options [1-" + (i - 1) + "] to select a listing: ");
      String input = sc.nextLine();
      try {
        num = Integer.parseInt(input);
        if (num == 0) {
          return;
        } else if (num < 0 || num > (i - 1)) {
          num = -1;
        }
      } catch (NumberFormatException e) {
        // Loop again
      }
    }
    
    try {
      rs.absolute(num);
      String lat = rs.getString("lat");
      String lon = rs.getString("lon");
      Date date = rs.getDate("date");
      BigDecimal price = rs.getBigDecimal("price");
      this.listingOptions(lat, lon, date, price);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
  
  private void listingOptions(String lat, String lon, Date date, BigDecimal price) {
    int op = -1;
    do {
      System.out.println("=======LISTING OPTIONS=======");
      System.out.println("0. Back");
      System.out.println("1. Book this listing");
      System.out.println("2. See host comments");
      System.out.println("3. See listing comments");
      System.out.print("Choose one of the previous options [0-3]: ");
      String sortInput = sc.nextLine();
      
      try {
        op = Integer.parseInt(sortInput);
      } catch (NumberFormatException e) {
        op = -1;
      }
    } while (op < 0 || op > 3);
    
    if (op == 0) {
      return;
    }
    
    if (op == 1) {
      ListingController listingMngr = new ListingController();
      boolean booked = listingMngr.bookListing(this.username, lat, lon, date, price);
      if (booked) {
        System.out.println("Booking successful!");
      }
    } else if (op == 2) {
      CommentsController commentsMngr = new CommentsController();
      ResultSet rs = commentsMngr.getHostComments(lat, lon, date);
      try {
        System.out.println("=======COMMENTS ON HOST=======");
        while (rs.next()) {
          String comment = rs.getString("text");
          String rating = rs.getString("rating");
          System.out.println("(" + rating + " stars) " + comment);
        }
        
        this.listingOptions(lat, lon, date, price);
      } catch (SQLException e) {
        e.printStackTrace();
      }
    } else if (op == 3) {
      CommentsController commentsMngr = new CommentsController();
      ResultSet rs = commentsMngr.getListingComments(lat, lon);
      try {
        System.out.println("======COMMENTS ON LISTING======");
        while (rs.next()) {
          String comment = rs.getString("text");
          String rating = rs.getString("rating");
          System.out.println("(" + rating + " stars) " + comment);
        }
        
        this.listingOptions(lat, lon, date, price);
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  private SearchFilter getFilters() {
    String[] dateRange = new String[2];
    do {
      System.out.println("==================");
      System.out.println("Enter a date range (default = all time)");
      System.out.print("Start date (yyyy-MM-dd): ");
      dateRange[0] = sc.nextLine();
      System.out.print("End date (yyyy-MM-dd): ");
      dateRange[1] = sc.nextLine();
      
      if (dateRange[0].equals("") && dateRange[1].equals("")) {
        break;
      }
    } while (!dateRange[0].matches("([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))")
              || !dateRange[1].matches("([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))"));
    
    String input1 = "-1";
    String input2 = "-1";
    int[] priceRange = new int[2];
    do {
      System.out.print("Minimum price (default = 0): $");
      input1 = sc.nextLine();
      
      if (input1.equals("")) {
        priceRange[0] = 0;
      }
      try {
        priceRange[0] = Integer.parseInt(input1);
        break;
      } catch (NumberFormatException e) {
        input1 = "-1";
      }
    } while (input1.compareTo("-1") != 0);
    
    do {
      System.out.print("Maximum price (default = none): $");
      input2 = sc.nextLine();
      
      if (input2.equals("")) {
        priceRange[1] = -1;
      }
      try {
        priceRange[1] = Integer.parseInt(input2);
        break;
      } catch (NumberFormatException e) {
        input2 = "-1";
      }
    } while (input2.compareTo("-1") != 0);
    
    ArrayList<Amenity> amenities = this.getAmenities();
    SearchFilter filter = new SearchFilter(dateRange[0], dateRange[1], priceRange[0], priceRange[1], amenities);
    return filter;
  }

  private ArrayList<Amenity> getAmenities() {
    ArrayList<Amenity> amenities = new ArrayList<Amenity>();
    Amenity values[] = Amenity.values();
    
    // Print options
    System.out.println("=========AMENITIES=========");
    for (int i = 0; i < values.length; i++) {
      System.out.println((i + 1) + ". " + values[i].toString());
    }
    
    // Parse input
    String input = "";
    do {
      try {
        System.out.print("Choose amenities [0-" + values.length + "] (separate by commas): ");
        input = sc.nextLine();
        if (input.equals("")) {
          break;
        }
        
        List<String> chosen = Arrays.asList(input.split("\\s*,\\s*"));
        for (String num : chosen) {
          int choice = Integer.parseInt(num);
          if (choice <= values.length) {
            amenities.add(values[choice - 1]);
          }
        }
        
        break;
      } catch (NumberFormatException e) {
        input = "-1";
      }
    } while (input.compareTo("-1") == 0);
    
    return amenities;
  }

  // Print menu options
  private static void menu() {
    System.out.println("=======SEARCH FILTERS=======");
    System.out.println("0. Back.");
    System.out.println("1. Search by vicinity");
    System.out.println("2. Search by postal code");
    System.out.println("3. Search by address");
    System.out.print("Choose one of the previous options [0-3]: ");
  }
}

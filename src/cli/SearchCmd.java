package cli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import controllers.SQLController;
import controllers.SearchController;
import enums.Amenity;

public class SearchCmd {

  private SQLController sqlMngr = null;
  private Scanner sc = null;
  
  protected SearchCmd(SQLController sqlMngr, Scanner sc) {
    this.sqlMngr = sqlMngr;
    this.sc = sc;
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
      System.out.println("1. Price");
      System.out.println("2. Distance");
      System.out.print("Choose one of the previous options [1 or 2]: ");
      String sortInput = sc.nextLine();
      
      try {
        sort = Integer.parseInt(sortInput);
      } catch (NumberFormatException e) {
        sort = -1;
      }
    } while (sort < 1 || sort > 2);
    
    SearchFilter filters = getFilters();
    SearchController searchMngr = new SearchController();
    searchMngr.byVicinity(lat, lon, maxDistance, sort, filters);
  }

  private void byPostalCode() {
    String postalCode;
    do {
      System.out.print("Postal code (XXX XXX): ");
      postalCode = sc.nextLine();
    } while (!postalCode.matches("^(?!.*[DFIOQU])[A-VXY][0-9][A-Z] ?[0-9][A-Z][0-9]$"));
    
    SearchFilter filters = getFilters();
    SearchController searchMngr = new SearchController();
    searchMngr.byPostalCode(postalCode, filters);
  }

  private void byAddress() {
    System.out.print("Address: ");
    String address = sc.nextLine();
    SearchController searchMngr = new SearchController();
    searchMngr.byAddress(address);
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
        priceRange[1] = 0;
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
    System.out.println("=========SEARCH FILTERS=========");
    System.out.println("0. Back.");
    System.out.println("1. Search by vicinity");
    System.out.println("2. Search by postal code");
    System.out.println("3. Search by address");
    System.out.print("Choose one of the previous options [0-3]: ");
  }
}
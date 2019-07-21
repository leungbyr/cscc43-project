package cli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import controllers.SQLController;
import enums.Amenity;
import enums.ListingType;

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
            this.byPriceAsc();
            break;
          case 3:
            this.byPriceDsc();
            break;
          case 4:
            this.byPostalCode();
            break;
          case 5:
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
    // TODO Auto-generated method stub
    
  }

  private void byPriceAsc() {
    // TODO Auto-generated method stub
    
  }

  private void byPriceDsc() {
    // TODO Auto-generated method stub
    
  }

  private void byPostalCode() {
    // TODO Auto-generated method stub
    
  }

  private void byAddress() {
    // TODO Auto-generated method stub
    
  }

  private SearchFilter getFilters() {
    String[] dateRange = new String[2];
    do {
      System.out.println("Enter a date range");
      System.out.print("Start date (yyyy-MM-dd): ");
      dateRange[0] = sc.nextLine();
      System.out.print("End date (yyyy-MM-dd): ");
      dateRange[1] = sc.nextLine();
    } while (!dateRange[0].matches("([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))")
              || !dateRange[1].matches("([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))"));
    
    String input = "-1";
    int[] priceRange = new int[2];
    do {
      System.out.print("Minimum price: $");
      String input1 = sc.nextLine();
      System.out.print("Maximum price: $");
      String input2 = sc.nextLine();
      try {
        priceRange[0] = Integer.parseInt(input1);
        priceRange[1] = Integer.parseInt(input2);
        break;
      } catch (NumberFormatException e) {
        input = "-1";
      }
    } while (input.compareTo("-1") != 0);
    
    ArrayList<Amenity> amenities = this.getAmenities();
    SearchFilter filter = new SearchFilter(dateRange[0], dateRange[1], priceRange[0], priceRange[1], amenities);
    return filter;
  }

  private ArrayList<Amenity> getAmenities() {
    ArrayList<Amenity> amenities = new ArrayList<Amenity>();
    Amenity values[] = Amenity.values();
    
    // Print options
    System.out.println("========= AMENITIES =========");
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
    System.out.println("2. Search by price (ascending)");
    System.out.println("3. Search by price (descending)");
    System.out.println("4. Search by postal code");
    System.out.println("5. Search by address");
    System.out.print("Choose one of the previous options [0-5]: ");
  }
}

package cli;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Scanner;

import controllers.ListingController;
import controllers.SQLController;
import controllers.UserController;
import enums.Amenity;
import enums.ListingType;

public class UserCmd {

  private SQLController sqlMngr = null;
  private Scanner sc = null;
  private String username = null;
  
  protected UserCmd(SQLController sqlMngr, Scanner sc, String username) {
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
            this.searchListings();
            break;
          case 2:
            this.showCurrentBookings();
            break;
          case 3:
            this.createListing();
            break;
          case 4:
            this.showListings();
            break;
          case 5:
            this.showPastBookings();
            break;
          case 6:
            if (this.deleteProfile()) input = "0";
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

  private boolean deleteProfile() {
    System.out.print("Are you sure you want to delete your account? (y/n): ");
    String input = sc.nextLine().toLowerCase();
    if (input.equals("y")) {
      UserController userMngr = new UserController();
      userMngr.deleteUser(this.username);
      return true;
    }
    
    return false;
  }

  private void showPastBookings() {
    ListingController listingMngr = new ListingController();
    listingMngr.printPastBookings(this.username);
  }

  private void showListings() {
    ListingController listingMngr = new ListingController();
    listingMngr.printHostedListings(this.username);
  }

  private void createListing() {
    // Collect listing info
    ListingType listingType = this.getListingType();
    String[] info = new String[6];
    System.out.print("Address: ");
    info[0] = sc.nextLine();
    
    do {
      System.out.print("Latitude: ");
      info[1] = sc.nextLine();
    } while (!info[1].matches("(\\-?\\d+(\\.\\d+)?)"));
    
    do {
      System.out.print("Longitude: ");
      info[2] = sc.nextLine();
    } while (!info[2].matches("(\\-?\\d+(\\.\\d+)?)"));
    
    System.out.print("City: ");
    info[3] = sc.nextLine();
    
    System.out.print("Country: ");
    info[4] = sc.nextLine();
    
    do {
      System.out.print("Postal code: ");
      info[5] = sc.nextLine();
      
      if (info[5].contains(" ")) {
        info[5] = info[5].replace(" ", "");
      }
    } while (!info[5].matches("^(?!.*[DFIOQU])[A-VXY][0-9][A-Z]?[0-9][A-Z][0-9]$"));
    
    List<Amenity> amenities = this.getAmenities();
    List<AvailableDate> availableDates = this.getAvailableDates();
    ListingController listingMngr = new ListingController();
    boolean inserted = listingMngr.insertListing(this.username, listingType, info[0], info[1], info[2], info[3], info[4], info[5], availableDates, amenities);
    if (inserted) {
      System.out.println("Listing successfully posted!");
    } else {
      System.out.println("Listing at this latitude and longitude already exists!");
    }
  }
  
  private List<AvailableDate> getAvailableDates() {
    ArrayList<AvailableDate> availableDates = new ArrayList<AvailableDate>();
    boolean done = false;
    
    while (!done) {
      System.out.println("=======AVAILABLE DATES=======");
      System.out.println("Enter a date range or 0 to finish posting listing");
      
      Date startDate = null;
      while (startDate == null) {
        System.out.print("Start date (yyyy-MM-dd): ");
        String dateInput = sc.nextLine();
        
        if (dateInput.equals("0"))  {
          done = true;
          break;
        }
        
        try {
          startDate = new SimpleDateFormat("yyyy-MM-dd").parse(dateInput);
        } catch (ParseException e) {
          // Loop and ask for date again
        }
      }
      
      if (done) break;
      
      Date endDate = null;
      while (endDate == null) {
        System.out.print("End date (yyyy-MM-dd): ");
        String dateInput = sc.nextLine();
        
        if (dateInput.equals("0"))  {
          done = true;
          break;
        }
        
        try {
          endDate = new SimpleDateFormat("yyyy-MM-dd").parse(dateInput);
        } catch (ParseException e) {
          // Loop and ask for date again
        }
      }
      
      if (done) break;
      
      BigDecimal price = null;
      while (price == null) {
        System.out.print("Price for this date range: $");
        String priceInput = sc.nextLine();
        if (priceInput.matches("^\\d{0,8}(\\.\\d{1,4})?$")) {
          price = new BigDecimal(priceInput);
        }
      }
      
      List<Date> dates = this.datesBetween(startDate, endDate);
      for (Date date : dates) {
        availableDates.add(new AvailableDate(date, price));
      }
    }
    
    return availableDates;
  }
  
  private List<Amenity> getAmenities() {
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
  
  private List<Date> datesBetween(Date startDate, Date endDate) {
    List<Date> dates = new ArrayList<Date>();
    Calendar calendar = new GregorianCalendar();
    calendar.setTime(startDate);

    while (calendar.getTime().getTime() <= endDate.getTime()) {
      Date result = calendar.getTime();
      dates.add(result);
      calendar.add(Calendar.DATE, 1);
    }
    
    return dates;
  }

  private ListingType getListingType() {
    ListingType type = null;
    ListingType types[] = ListingType.values();
    
    // Print options
    System.out.println("=========SELECT TYPE=========");
    for (int i = 0; i < types.length; i++) {
      System.out.println((i + 1) + ". " + types[i].toString());
    }
    
    // Parse input
    String input = "";
    int choice;
    while (type == null) {
      System.out.print("Choose one of the previous options [1-" + types.length + "]: ");
      input = sc.nextLine();
      try {
        choice = Integer.parseInt(input);
        if (choice > 0 && choice <= types.length) { // Checking that choice is valid
          type = types[choice - 1];
          break;
        }
      } catch (NumberFormatException e) {
        // Looping and asking for input again
      }
    }
    
    return type;
  }

  private void showCurrentBookings() {
    ListingController listingMngr = new ListingController();
    listingMngr.printUpcomingBookings(this.username);
  }

  private void searchListings() {
    SearchCmd searchCmd = new SearchCmd(sqlMngr, sc, this.username);
    searchCmd.execute();
  }

  // Print menu options
  private void menu() {
    System.out.println("===========================");
    System.out.println("Logged in as: " + this.username);
    System.out.println("=========USER MENU=========");
    System.out.println("0. Log out.");
    System.out.println("1. Search listings");
    System.out.println("2. My upcoming bookings");
    System.out.println("3. Create a listing");
    System.out.println("4. My listings");
    System.out.println("5. Past bookings");
    System.out.println("6. Delete profile");
    System.out.print("Choose one of the previous options [0-6]: ");
  }
}

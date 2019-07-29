package cli;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Scanner;

import controllers.CommentsController;
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
              this.showRentedOut();
              break;
            case 6:
              this.showPastListings();
              break;
            case 7:
              this.showPastBookings();
              break;
            case 8:
              this.profileComments();
              break;
            case 9:
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

  private void profileComments() {
    CommentsController commentsMngr = new CommentsController();
    ResultSet rs = commentsMngr.getComments(this.username);
    try {
      System.out.println("=======PROFILE COMMENTS=======");
      while (rs.next()) {
        String comment = rs.getString("text");
        String rating = rs.getString("rating");
        System.out.println("(" + rating + " stars) " + comment);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private void showPastListings() {
    ListingController listingMngr = new ListingController();
    ResultSet rs = listingMngr.printPastRentedOut(this.username);
    int i = this.printResults(rs);
    int num = -1;
    while (num < 0) {
      System.out.print("Enter 0 to go back or one of the previous options [1-" + (i - 1) + "] to comment/rate the renter: ");
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
      this.renterComment(lat, lon, date);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private void renterComment(String lat, String lon, Date date) {
    int rating = -1;
    while (rating < 0) {
      System.out.print("Enter a rating (1-5) or 0 to go back: ");
      String ratingInput = sc.nextLine();
      try {
        rating = Integer.parseInt(ratingInput);
        if (rating == 0) {
          return;
        } else if (rating < 1 || rating > 5) {
          rating = -1;
        }
      } catch (NumberFormatException e) {
        // Loop again
      }
    }

    System.out.println("Comment: ");
    String comment = sc.nextLine();
    CommentsController commentsMngr = new CommentsController();
    commentsMngr.renterComment(this.username, lat, lon, date, rating, comment);
  }

  private void showRentedOut() {
    ListingController listingMngr = new ListingController();
    ResultSet rs = listingMngr.printRentedOut(this.username);
    int i = this.printResults(rs);
    int modifyNum = -1;
    while (modifyNum < 0) {
      System.out.print("Enter 0 to go back or one of the previous options [1-" + (i - 1) + "] to cancel the booking: ");
      String modifyInput = sc.nextLine();
      try {
        modifyNum = Integer.parseInt(modifyInput);
        if (modifyNum == 0) {
          return;
        } else if (modifyNum < 0 || modifyNum > (i - 1)) {
          modifyNum = -1;
        }
      } catch (NumberFormatException e) {
        // Loop again
      }
    }

    try {
      rs.absolute(modifyNum);
      String modifyLat = rs.getString("lat");
      String modifyLon = rs.getString("lon");
      Date modifyDate = rs.getDate("date");
      BigDecimal price = rs.getBigDecimal("price");

      if (listingMngr.cancelBookingHost(modifyLat, modifyLon, modifyDate, price)) {
        System.out.println("Booking canceled.");
      }
    } catch (SQLException e) {
      e.printStackTrace();
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
    ResultSet rs = listingMngr.printPastBookings(this.username);
    int i = 0;

    try {
      System.out.println("=======PAST BOOKINGS=======");
      for (i = 1; rs.next(); i++) {
        String lat = rs.getString("lat");
        String lon = rs.getString("lon");
        String type = ListingType.valueOf(rs.getString("type")).toString();
        String address = rs.getString("address");
        String city = rs.getString("city");
        String country = rs.getString("country");
        String postal = rs.getString("postal");
        String date = rs.getString("date");
        boolean canceled = rs.getBoolean("canceled");

        System.out.println(i + ". " + type + " at "  + address + ", " + city + ", " + country + ", " + postal + " (" + lat + ", " + lon + ")");
        System.out.print(String.format("%" + ((int)(Math.log10(i)) + 3) + "s", ""));
        if (canceled) System.out.print("(CANCELED) ");
        System.out.println("Booked for: " + date);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    int num = -1;
    while (num < 0) {
      System.out.print("Enter 0 to go back or one of the previous options [1-" + (i - 1) + "] to comment/rate the listing or host: ");
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
      this.bookedComment(lat, lon, date);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private void bookedComment(String lat, String lon, Date date) {
    int op = -1;
    do {
      System.out.println("=======COMMENT OPTIONS=======");
      System.out.println("0. Back");
      System.out.println("1. Comment and rate the host");
      System.out.println("2. Comment and rate the listing");
      System.out.print("Choose one of the previous options [0-2]: ");
      String sortInput = sc.nextLine();

      try {
        op = Integer.parseInt(sortInput);
      } catch (NumberFormatException e) {
        op = -1;
      }
    } while (op < 0 || op > 2);

    if (op == 0) {
      return;
    }

    if (op == 1) {
      int rating = -1;
      while (rating < 0) {
        System.out.print("Enter a rating (1-5) or 0 to go back: ");
        String ratingInput = sc.nextLine();
        try {
          rating = Integer.parseInt(ratingInput);
          if (rating == 0) {
            return;
          } else if (rating < 1 || rating > 5) {
            rating = -1;
          }
        } catch (NumberFormatException e) {
          // Loop again
        }
      }

      System.out.println("Comment: ");
      String comment = sc.nextLine();
      CommentsController commentsMngr = new CommentsController();
      commentsMngr.hostComment(this.username, lat, lon, date, rating, comment);
    } else if (op == 2) {
      int rating = -1;
      while (rating < 0) {
        System.out.print("Enter a rating (1-5) or 0 to go back: ");
        String ratingInput = sc.nextLine();
        try {
          rating = Integer.parseInt(ratingInput);
          if (rating == 0) {
            return;
          } else if (rating < 1 || rating > 5) {
            rating = -1;
          }
        } catch (NumberFormatException e) {
          // Loop again
        }
      }

      System.out.println("Comment: ");
      String comment = sc.nextLine();
      CommentsController commentsMngr = new CommentsController();
      commentsMngr.listingComment(this.username, lat, lon, date, rating, comment);
    }
  }

  private void showListings() {
    ListingController listingMngr = new ListingController();
    ResultSet rs = listingMngr.printHostedListings(this.username);
    int i = this.printResults(rs);
    int modifyNum = -1;
    while (modifyNum < 0) {
      System.out.print("Enter 0 to go back or one of the previous options [1-" + (i - 1) + "] to modify the listing: ");
      String modifyInput = sc.nextLine();
      try {
        modifyNum = Integer.parseInt(modifyInput);
        if (modifyNum == 0) {
          return;
        } else if (modifyNum < 0 || modifyNum > (i - 1)) {
          modifyNum = -1;
        }
      } catch (NumberFormatException e) {
        // Loop again
      }
    }

    try {
      rs.absolute(modifyNum);
      String modifyLat = rs.getString("lat");
      String modifyLon = rs.getString("lon");
      Date modifyDate = rs.getDate("date");
      this.modifyListing(modifyLat, modifyLon, modifyDate);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private int printResults(ResultSet rs) {
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

        System.out.println(i + ". " + type + " at "  + address + ", " + city + ", " + country + ", " + postal + " (" + rsLat + ", " + rsLon + ")");
        System.out.print(String.format("%" + ((int)(Math.log10(i)) + 3) + "s", ""));
        System.out.println("Date: " + date + ", Price: $" + price.setScale(2, RoundingMode.HALF_DOWN));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return i;
  }

  private void modifyListing(String lat, String lon, Date date) {
    int op = -1;
    do {
      System.out.println("=======MODIFY LISTING=======");
      System.out.println("0. Back");
      System.out.println("1. Remove the listing");
      System.out.println("2. Change price");
      System.out.print("Choose one of the previous options [0-2]: ");
      String sortInput = sc.nextLine();

      try {
        op = Integer.parseInt(sortInput);
      } catch (NumberFormatException e) {
        op = -1;
      }
    } while (op < 0 || op > 2);

    if (op == 0) {
      return;
    }

    ListingController listingMngr = new ListingController();
    if (op == 1) {
      if (listingMngr.removeListing(lat, lon, date)) {
        System.out.println("Listing removed!");
      }
    } else if (op == 2) {
      BigDecimal price = null;
      while (price == null) {
        System.out.print("New price: $");
        String priceInput = sc.nextLine();
        if (priceInput.matches("^\\d{0,8}(\\.\\d{1,4})?$")) {
          price = new BigDecimal(priceInput);
          if (listingMngr.updatePrice(lat, lon, date, price)) {
            System.out.println("Price updated!");
          } else {
            System.out.println("Price not updated (listing date may be too close)");
          }
        }
      }
    }
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
    List<AvailableDate> availableDates = this.getAvailableDates(info[1], info[2]);
    ListingController listingMngr = new ListingController();
    boolean inserted = listingMngr.insertListing(this.username, listingType, info[0], Double.parseDouble(info[1]), Double.parseDouble(info[2]), info[3], info[4], info[5], availableDates, amenities);
    if (inserted) {
      System.out.println("Listing successfully posted!");
    } else {
      System.out.println("Listing at this latitude and longitude already exists!");
    }
  }

  private List<AvailableDate> getAvailableDates(String lat, String lon) {
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
        ListingController listingMngr = new ListingController();
        double suggestedPrice = listingMngr.getSuggestedPrice(lat, lon, 100);
        String priceString = new BigDecimal(suggestedPrice).setScale(2, RoundingMode.HALF_EVEN).toString();
        System.out.print("Price for this date range (suggested price = $" + priceString + "): $");
        String priceInput = sc.nextLine();
        if (priceInput.matches("^\\d+(,\\d{3})*(\\.\\d{1,2})?$")) {
          price = new BigDecimal(priceInput);
        } else if (priceInput.equals("")) {
          price = new BigDecimal(priceString);
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
    ListingController listingMngr = new ListingController();

    // Print options
    System.out.println("=========AMENITIES=========");
    for (int i = 0; i < values.length; i++) {
      double amenityValue = listingMngr.getAmenityValue(values[i]);
      String valueString = new BigDecimal(amenityValue).setScale(2, RoundingMode.HALF_EVEN).toString();
      System.out.println((i + 1) + ". " + values[i].toString() + " (Estimated value = $" + valueString + ")");
    }

    // Parse input
    String input = "";
    do {
      try {
        System.out.print("Choose amenities [1-" + values.length + "] (separate by commas): ");
        input = sc.nextLine();
        if (input.equals("")) {
          break;
        }

        List<String> chosen = Arrays.asList(input.split("\\s*,\\s*"));
        for (String num : chosen) {
          int choice = Integer.parseInt(num);
          if (choice > 0 && choice <= values.length) {
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
    ResultSet rs = listingMngr.printUpcomingBookings(this.username);
    int i = 0;

    try {
      System.out.println("======UPCOMING BOOKINGS======");
      for (i = 1; rs.next(); i++) {
        String lat = rs.getString("lat");
        String lon = rs.getString("lon");
        String type = ListingType.valueOf(rs.getString("type")).toString();
        String address = rs.getString("address");
        String city = rs.getString("city");
        String country = rs.getString("country");
        String postal = rs.getString("postal");
        String date = rs.getString("date");

        System.out.println(i + ". " + type + " at "  + address + ", " + city + ", " + country + ", " + postal + " (" + lat + ", " + lon + ")");
        System.out.print(String.format("%" + ((int)(Math.log10(i)) + 3) + "s", ""));
        System.out.println("Booked for: " + date);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    int cancelNum = -1;
    while (cancelNum < 0) {
      System.out.print("Enter 0 to go back or one of the previous options [1-" + (i - 1) + "] to cancel the booking: ");
      String cancelInput = sc.nextLine();
      try {
        cancelNum = Integer.parseInt(cancelInput);
        if (cancelNum == 0) {
          return;
        } else if (cancelNum < 0 || cancelNum > (i - 1)) {
          cancelNum = -1;
        }
      } catch (NumberFormatException e) {
        // Loop again
      }
    }

    try {
      rs.absolute(cancelNum);
      String cancelLat = rs.getString("lat");
      String cancelLon = rs.getString("lon");
      Date cancelDate = rs.getDate("date");
      BigDecimal cancelPrice = rs.getBigDecimal("price");
      boolean canceled = listingMngr.cancelBookingRenter(cancelLat, cancelLon, cancelDate, cancelPrice);
      if (canceled) {
        System.out.println("Booking canceled!");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
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
    System.out.println("4. My available listings");
    System.out.println("5. Upcoming rented out listings");
    System.out.println("6. Past rented out listings");
    System.out.println("7. Past bookings");
    System.out.println("8. Profile comments");
    System.out.println("9. Delete profile");
    System.out.print("Choose one of the previous options [0-9]: ");
  }
}

package cli;

import java.util.Scanner;

import controllers.ListingController;
import controllers.SQLController;
import controllers.UserController;
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
    
    ListingController listingMngr = new ListingController();
    listingMngr.insertListing(this.username, listingType, info[0], info[1], info[2], info[3], info[4], info[5]);
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
    SearchCmd searchCmd = new SearchCmd(sqlMngr, sc);
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

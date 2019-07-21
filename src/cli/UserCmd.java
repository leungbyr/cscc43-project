package cli;

import java.util.Scanner;

import controllers.ListingController;
import controllers.SQLController;
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
      System.out.println("");
      System.out.println("**********************");
      System.out.println("******LOGGED IN*******");
      System.out.println("**********************");
      System.out.println("");

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
            case 2:
              this.showCurrentBookings();
            case 3:
              this.createListing();
            case 4:
              this.showListings();
            case 5:
              this.showPastBookings();
            case 6:
              this.deleteProfile();
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

  private void deleteProfile() {
    // TODO Auto-generated method stub
    
  }

  private void showPastBookings() {
    // TODO Auto-generated method stub
    
  }

  private void showListings() {
    // TODO Auto-generated method stub
    
  }

  private void createListing() {
    // Collect new listing info
    ListingType listingType = this.getListingType();
    String[] info = new String[1];
    System.out.print("Address: ");
    info[0] = sc.nextLine();
    
    ListingController listingMngr = new ListingController();
    listingMngr.insertListing(this.username, listingType, info[0]);
  }

  private ListingType getListingType() {
    ListingType type = null;
    ListingType types[] = ListingType.values();
    
    // Print options
    System.out.println("=========SELECT TYPE=========");
    System.out.println("0. Cancel");
    for (int i = 0; i < types.length; i++) {
      System.out.println((i + 1) + ". " + types[i].toString());
    }
    System.out.print("Choose one of the previous options [0-" + types.length + "]: ");
    
    // Parse input
    String input = "";
    int choice = -1;
    do {
      input = sc.nextLine();
      try {
        choice = Integer.parseInt(input);
        if (choice <= types.length) { // Checking that choice is valid
          type = types[choice - 1];
        }
        
        break;
      } catch (NumberFormatException e) {
        input = "-1";
      }
    } while (input.compareTo("0") != 0);
    
    return type;
  }

  private void showCurrentBookings() {
    // TODO Auto-generated method stub
    
  }

  private void searchListings() {
    // TODO Auto-generated method stub
    
  }

  // Print menu options
  private void menu() {
    System.out.println("Logged in as: " + this.username);
    System.out.println("=========USER MENU=========");
    System.out.println("0. Exit.");
    System.out.println("1. Search listings");
    System.out.println("2. My current bookings");
    System.out.println("3. Create a listing");
    System.out.println("4. My listings");
    System.out.println("5. Past bookings");
    System.out.println("6. Delete profile");
    System.out.print("Choose one of the previous options [0-6]: ");
  }
}

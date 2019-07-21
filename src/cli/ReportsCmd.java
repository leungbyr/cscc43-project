package cli;

import java.util.Scanner;

import controllers.SQLController;

public class ReportsCmd {

  private SQLController sqlMngr = null;
  private Scanner sc = null;
  
  protected ReportsCmd(SQLController sqlMngr, Scanner sc) {
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
              this.bookListing();
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

  private void bookListing() {
    // TODO Book listing    
  }

  // Print menu options
  private static void menu() {
    System.out.println("=========MENU=========");
    System.out.println("0. Exit.");
    System.out.println("1. Bookings");
    System.out.println("2. Listings");
    System.out.println("3. Hosts");
    System.out.println("4. Renters");
    System.out.println("5. Comments");
    System.out.print("Choose one of the previous options [0-5]: ");
  }
}

package cli;

import java.util.Scanner;

import controllers.SQLController;

public class UserCmd {

  private SQLController sqlMngr = null;
  private Scanner sc = null;
  
  protected UserCmd(SQLController sqlMngr, Scanner sc, String username) {
    this.sqlMngr = sqlMngr;
    this.sc = sc;
  }

  public boolean execute() {
    if (sc != null && sqlMngr != null) {
      System.out.println("");
      System.out.println("***************************");
      System.out.println("******ACCESS GRANTED*******");
      System.out.println("***************************");
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
    System.out.println("1. Book a listing.");
    System.out.print("Choose one of the previous options [0-4]: ");
  }
}

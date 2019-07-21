package cli;

import java.util.Scanner;

import controllers.SQLController;

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

  // Print menu options
  private static void menu() {
    System.out.println("=========SEARCH FILTERS=========");
    System.out.println("0. Back.");
    System.out.println("1. Location");
    System.out.println("2. Price");
    System.out.println("3. Date");
    System.out.print("Change a filter [1-3] or type s to search: ");
  }
}

package cli;

import java.util.Calendar;
import java.util.Scanner;

import controllers.ReportsController;
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
            this.bookingsReport();
            break;
          case 2:
            this.listingsReport();
            break;
          case 3:
            this.hostsReport();
            break;
          case 4:
            this.commercialHosts();
            break;
          case 5:
            this.rentersReport();
            break;
          case 6:
            this.renterCancellations();
            break;
          case 7:
            this.hostCancellations();
            break;
          case 8:
            this.commentsReport();
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

  private void commentsReport() {
    ReportsController reportsMngr = new ReportsController();
    reportsMngr.mostPopularNounForListings();
  }
  
  private void hostCancellations() {
    ReportsController reportsMngr = new ReportsController();
    reportsMngr.hostCancellationsRanking();
  }
  
  private void renterCancellations() {
    ReportsController reportsMngr = new ReportsController();
    reportsMngr.renterCancellationsRanking();
  }

  private void rentersReport() {
    String[] query = new String[3]; // User input
    do {
      System.out.println("==================");
      System.out.println("Enter a date range (default = all time)");
      System.out.print("Start date (yyyy-MM-dd): ");
      query[0] = sc.nextLine();
      System.out.print("End date (yyyy-MM-dd): ");
      query[1] = sc.nextLine();
      
      if (query[0].equals("") && query[1].equals("")) {
        break;
      }
    } while (!query[0].matches("([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))")
              || !query[1].matches("([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))"));
    System.out.print("City (unspecified = all): ");
    query[2] = sc.nextLine();
    
    ReportsController reportsMngr = new ReportsController();
    if (query[2].equals("")) {
      reportsMngr.printRentersReportOverall(query[0], query[1]);
    } else {
      reportsMngr.printRentersReportPerCity(query[0], query[1]);
    }
  }
  
  private void commercialHosts() {
    String[] query = new String[2];
    System.out.print("Country: ");
    query[0] = sc.nextLine();
    System.out.print("City (unspecified = only by country): ");
    query[1] = sc.nextLine();
    
    ReportsController reportsMngr = new ReportsController();
    if (query[1].equals("")) {
      reportsMngr.printCommercialHostsInCountry(query[0]);
    } else {
      reportsMngr.printCommercialHostsInCountryAndCity(query[0], query[1]);
    }
  }

  private void hostsReport() {
    String[] query = new String[1]; // User input
    System.out.print("Per country = 0, per city = 1: ");
    do {
      query[0] = sc.nextLine();
    } while (!query[0].matches("[01]"));
    
    ReportsController reportsMngr = new ReportsController();
    if (query[0].equals("0")) {
      reportsMngr.printHostsReportPerCountry();
    } else {
      reportsMngr.printHostsReportPerCity();
    }
  }

  private void listingsReport() {    
    ReportsController reportsMngr = new ReportsController();
    reportsMngr.printListingsReport();
  }

  private void bookingsReport() {
    String[] query = new String[3]; // User input
    do {
      System.out.println("==================");
      System.out.println("Enter a date range (default = all time)");
      System.out.print("Start date (yyyy-MM-dd): ");
      query[0] = sc.nextLine();
      System.out.print("End date (yyyy-MM-dd): ");
      query[1] = sc.nextLine();
      
      if (query[0].equals("") && query[1].equals("")) {
        break;
      }
    } while (!query[0].matches("([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))") 
              || !query[1].matches("([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))"));
    System.out.print("City (unspecified = all): ");
    query[2] = sc.nextLine();
    
    ReportsController reportsMngr = new ReportsController();
    if (query[2].equals("")) {
      reportsMngr.printBookingsReportByCity(query[0], query[1]);
    } else {
      reportsMngr.printBookingsReportByZipCode(query[0], query[1], query[2]);
    }
  }

  // Print menu options
  private static void menu() {
    System.out.println("=========REPORTS MENU=========");
    System.out.println("0. Exit.");
    System.out.println("1. Total booking count");
    System.out.println("2. Total listing count");
    System.out.println("3. Hosts by number of listings");
    System.out.println("4. Possible commercial hosts");
    System.out.println("5. Renters by number of bookings");
    System.out.println("6. Most cancellations (renters)");
    System.out.println("7. Most cancellations (hosts)");
    System.out.println("8. Comments");
    System.out.print("Choose one of the previous options [0-8]: ");
  }
}

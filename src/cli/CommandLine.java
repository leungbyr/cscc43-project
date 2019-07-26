package cli;

import java.util.ArrayList;
import java.util.Scanner;

import controllers.SQLController;
import controllers.UserController;

public class CommandLine {

  private SQLController sqlMngr = null;
  private Scanner sc = null;

  /*
   * Function used for initializing an instance of current class
   */
  public boolean startSession() {
    boolean success = true;
    if (sc == null) {
      sc = new Scanner(System.in);
    }
    if (sqlMngr == null) {
      sqlMngr = SQLController.getInstance();
    }
    try {
      success = sqlMngr.connect(this.getCredentials());
    } catch (ClassNotFoundException e) {
      success = false;
      System.err.println("Establishing connection triggered an exception!");
      e.printStackTrace();
      sc = null;
      sqlMngr = null;
    }
    
    // Initialize tables
    sqlMngr.initializeTables();
    
    return success;
  }
  
  /*
   * Function that acts as destructor of an instance of this class. Performs some
   * housekeeping setting instance's private field to null
   */
  public void endSession() {
    if (sqlMngr != null)
      sqlMngr.disconnect();
    if (sc != null) {
      sc.close();
    }
    sqlMngr = null;
    sc = null;
  }

  /*
   * Function that executes an infinite loop and activates the respective
   * functionality according to user's choice. At each time it also outputs the
   * menu of core functionalities supported from our application.
   */
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
            this.userLogin();
            break;
          case 2:
            this.createProfile();
            break;
          case 3:
            this.runReports();
            break;
          case 6:
            this.insertOperator();
            break;
          case 7:
            this.selectOperator();
            break;
          case 8:
            this.printSchema();
            break;
          case 9:
            this.printColSchema();
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

  private void runReports() {
    ReportsCmd reportsCmd = new ReportsCmd(sqlMngr, sc);
    reportsCmd.execute();
  }

  private void createProfile() {
    // Collect required information
    String[] info = new String[8];
    
    do {
      System.out.print("Username: ");
      info[0] = sc.nextLine();
    } while (!info[0].matches("^[A-Za-z0-9]+(?:[ _-][A-Za-z0-9]+)*$"));
    
    do {
      System.out.print("Password: ");
      info[1] = sc.nextLine();
    } while (!info[1].matches("^[A-Za-z0-9]+(?:[ _-][A-Za-z0-9]+)*$"));
    
    System.out.print("First name: ");
    info[2] = sc.nextLine();
    System.out.print("Last name: ");
    info[3] = sc.nextLine();
    System.out.print("Address: ");
    info[4] = sc.nextLine();
    
    do {
      System.out.print("Date of birth (yyyy-MM-dd): ");
      info[5] = sc.nextLine();
    } while (!info[5].matches("([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))"));
    
    do {
      System.out.print("Social insurance number: ");
      info[6] = sc.nextLine();
      
      if (info[6].contains("-") || info[6].contains(" ")) {
        info[6] = info[6].replace("-", "").replace(" ", "");
      }
    } while (!info[6].matches("^(\\d{9})$"));
  
    System.out.print("Occupation: ");
    info[7] = sc.nextLine();
    
    UserController userMngr = new UserController();
    userMngr.insertUser(info[0], info[1], info[2], info[3], info[4], info[5], info[6], info[7]);
  }

  private void userLogin() {
    String[] cred = new String[3];
    System.out.print("Username: ");
    cred[0] = sc.nextLine();
    System.out.print("Password: ");
    cred[1] = sc.nextLine();
    
    UserController userMngr = new UserController();
    boolean verified = userMngr.verifyLogin(cred[0], cred[1]);
    if (verified) {
      UserCmd userCmd = new UserCmd(sqlMngr, sc, cred[0]);
      userCmd.execute();
    } else {
      System.out.println("");
      System.out.println("*************************");
      System.out.println("******LOGIN FAILED*******");
      System.out.println("*************************");
      System.out.println("");
    }
  }

  // Print menu options
  private static void menu() {
    System.out.println("=========MENU=========");
    System.out.println("0. Exit.");
    System.out.println("1. Log in as user");
    System.out.println("2. Create a profile");
    System.out.println("3. Reports");
    System.out.println("6. Insert a record.");
    System.out.println("7. Select a record.");
    System.out.println("8. Print schema.");
    System.out.println("9. Print table schema.");
    System.out.print("Choose one of the previous options [0-4]: ");
  }

  // Called during the initialization of an instance of the current class
  // in order to retrieve from the user the credentials with which our program
  // is going to establish a connection with MySQL
  private String[] getCredentials() {
    String[] cred = new String[3];
    System.out.print("Username: ");
    cred[0] = sc.nextLine();
    System.out.print("Password: ");
    cred[1] = sc.nextLine();
    System.out.print("Database: ");
    cred[2] = sc.nextLine();
    return cred;
  }

  // Function that handles the feature: "3. Print schema."
  private void printSchema() {
    ArrayList<String> schema = sqlMngr.getSchema();

    System.out.println("");
    System.out.println("------------");
    System.out.println("Total number of tables: " + schema.size());
    for (int i = 0; i < schema.size(); i++) {
      System.out.println("Table: " + schema.get(i));
    }
    System.out.println("------------");
    System.out.println("");
  }

  // Function that handles the feature: "4. Print table schema."
  private void printColSchema() {
    System.out.print("Table Name: ");
    String tableName = sc.nextLine();
    ArrayList<String> result = sqlMngr.colSchema(tableName);
    System.out.println("");
    System.out.println("------------");
    System.out.println("Total number of fields: " + result.size() / 2);
    for (int i = 0; i < result.size(); i += 2) {
      System.out.println("-");
      System.out.println("Field Name: " + result.get(i));
      System.out.println("Field Type: " + result.get(i + 1));
    }
    System.out.println("------------");
    System.out.println("");
  }

  // Function that handles the feature: "2. Select a record."
  private void selectOperator() {
    String query = "";
    System.out.print("Issue the Select Query: ");
    query = sc.nextLine();
    query.trim();
    if (query.substring(0, 6).compareToIgnoreCase("select") == 0)
      sqlMngr.selectOp(query);
    else
      System.err.println("No select statement provided!");
  }

  // Function that handles the feature: "1. Insert a record."
  private void insertOperator() {
    int rowsAff = 0;
    int counter = 0;
    String query = "";
    System.out.print("Table: ");
    String table = sc.nextLine();
    System.out.print("Comma Separated Columns: ");
    String cols = sc.nextLine();
    System.out.print("Comma Separated Values: ");
    String[] vals = sc.nextLine().split(",");
    // transform the user input into a valid SQL insert statement
    query = "INSERT INTO " + table + " (" + cols + ") VALUES(";
    for (counter = 0; counter < vals.length - 1; counter++) {
      query = query.concat("'" + vals[counter] + "',");
    }
    query = query.concat("'" + vals[counter] + "');");
    System.out.println(query);
    rowsAff = sqlMngr.insertOp(query);
    System.out.println("");
    System.out.println("Rows affected: " + rowsAff);
    System.out.println("");
  }

}
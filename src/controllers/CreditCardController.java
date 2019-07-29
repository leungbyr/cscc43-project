package controllers;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Scanner;

public class CreditCardController {

  private SQLController sqlMngr = null;
  private Connection conn = null;
  private Statement st = null;
  private Scanner sc = null;

  public CreditCardController() {
    this.sqlMngr = SQLController.getInstance();
    this.conn = sqlMngr.conn;
    this.st = sqlMngr.st;
    this.sc = new Scanner(System.in);
  }

  public boolean getPaymentCard(String username) {
    String sql = "";

    boolean hasCards = false;
    PreparedStatement ps = null;
    ResultSet rs = null;
    int entryCount = 0;
    try {
      do {
        sql = "SELECT sin FROM users WHERE username=?";

        ps = conn.prepareStatement(sql);
        ps.setString(1, username);

        rs = ps.executeQuery();
        String sin = "";
        while (rs.next()) {
          sin = rs.getString("sin");
        }

        //
        sql = "SELECT number FROM pays_with WHERE sin=?";

        ps = conn.prepareStatement(sql);
        ps.setString(1, sin);

        rs = ps.executeQuery();

        while (rs.next()) {
          System.out.printf("%d. Card number: %s\n", ++entryCount, rs.getString("number"));
        }

        if (entryCount == 0) {
          if (!addPaymentCard(sin)) {
            return false;
          }
        } else {
          hasCards = true;
        }
      } while (!hasCards);
    } catch (Exception e) { e.printStackTrace(); }
    System.out.printf("Choose from one of the previous options [0-%d]\n", entryCount);
    sc.nextLine(); // LOL
    
    return true;
  }

  public boolean addPaymentCard(String sin) {
    String[] query = new String[3];
    System.out.println("Please enter your credit card information. Or enter '0' now to exit");
    System.out.print("Card number (16 digits): ");
    query[0] = sc.nextLine();
    if (query[0].equals("0"))
      return false;
    System.out.print("Expiry date (yyyy-MM): ");
    query[1] = sc.nextLine() + "-01";
    System.out.print("CVV: ");
    query[2] = sc.nextLine();

    String sql = "INSERT INTO card(number, expiry, cvv) VALUES (?, ?, ?)";

    PreparedStatement ps = null;
    try {
      ps = conn.prepareStatement(sql);
      ps.setString(1, query[0]);
      ps.setDate(2, Date.valueOf(query[1]));
      ps.setInt(3, Integer.parseInt(query[2]));

      ps.executeUpdate();

      sql = "INSERT INTO pays_with(number, sin) VALUES (?, ?)";

      ps = conn.prepareStatement(sql);
      ps.setString(1, query[0]);
      ps.setString(2, sin);

      ps.executeUpdate();
    } catch (Exception e) { e.printStackTrace(); }
    return true;
  }
}

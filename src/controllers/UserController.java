package controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UserController {
  
  private SQLController sqlMngr = null;
  private Connection conn = null;
  private Statement st = null;
  
  public UserController() {
    this.sqlMngr = SQLController.getInstance();
    this.conn = sqlMngr.conn;
    this.st = sqlMngr.st;
  }
  
  public boolean insertUser(String username, String password, String firstName, String lastName, String address, String dob, String sin, String occupation) {
    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    java.sql.Date sqlDate = null;
    
    try {
      Date birthDate = formatter.parse(dob);
      sqlDate = new java.sql.Date(birthDate.getTime());    
    } catch (ParseException e1) {
      e1.printStackTrace();
    }
    
    String sql = "INSERT INTO Users(sin, username, password, name, address, birthdate, occupation)" + " VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    try {
      PreparedStatement preparedStmt = conn.prepareStatement(sql);
      preparedStmt.setString(1, sin);
      preparedStmt.setString(2, username);
      preparedStmt.setString(3, password);
      preparedStmt.setString(4, firstName + " " + lastName);
      preparedStmt.setString(5, address);
      preparedStmt.setDate(6, sqlDate);
      preparedStmt.setString(7, occupation);
      preparedStmt.execute();
    } catch (Exception e) { 
      e.printStackTrace();
      return false;
    }
    
    return true;
  }

  public void deleteUser(String username) {
    String _sql = String.format("SELECT lat, lon FROM Hosted_by NATURAL JOIN Listings JOIN Users ON Hosted_by.sin=Users.sin WHERE username=?");
    try {
      PreparedStatement ps = null;
      ResultSet rs = null;
      
      ps = conn.prepareStatement(_sql);
      ps.setString(1, username);
      rs = ps.executeQuery();
      
      ListingController lc = new ListingController();
      while (rs.next()) {
        String _sql2 = "DELETE FROM Listings WHERE lat=? AND lon=?";
        ps = conn.prepareStatement(_sql2);
        ps.setBigDecimal(1, rs.getBigDecimal("lat"));
        ps.setBigDecimal(2, rs.getBigDecimal("lon"));
        ps.executeUpdate();
      }
    } catch (Exception e) { e.printStackTrace(); }
    String sql = String.format("DELETE FROM Users WHERE username='%s'",
                                username);

    try {
      this.st.execute(sql);
    } catch (Exception e) { e.printStackTrace(); }
  }

  public boolean verifyLogin(String username, String password) {
    String sql = String.format("SELECT * FROM Users WHERE (username='%s' AND password='%s')",
        username, password);
    try {
      ResultSet rs = this.st.executeQuery(sql);
      if (rs.next()) return true;
    } catch (Exception e) { e.printStackTrace(); }
   
    return false;
  }

 
}

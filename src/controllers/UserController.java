package controllers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class UserController {
  
  private SQLController sqlMngr = null;
  private Connection conn = null;
  private Statement st = null;
  
  public UserController() {
    this.sqlMngr = SQLController.getInstance();
    this.conn = sqlMngr.conn;
    this.st = sqlMngr.st;
  }
  
  public void insertUser(String username, String password, String firstName, String lastName, String address, String dob, String sin, String occupation) {
    // TODO Auto-generated method stub
    String sql = String.format("INSERT INTO Users(username, password, firstName, lastName, address, dob, sin, occupation) VALUES (%s, %s, %s, %s, %s, %s, %s)",
                                username, password, firstName, lastName, address, dob, sin, occupation);
    
    try {
      this.st.execute(sql);
    } catch (Exception e) { e.printStackTrace(); }
  }

  public void deleteUser(String username) {
    // TODO Auto-generated method stub
    String sql = String.format("DELETE FROM Users WHERE username=%s",
                                username);

    try {
      this.st.execute(sql);
    } catch (Exception e) { e.printStackTrace(); }
  }

  public boolean verifyLogin(String username, String password) {
    // TODO Auto-generated method stub
    String sql = String.format("SELECT * FROM Users WHERE (username=%s AND password=%s)",
        username);
    try {
      ResultSet rs = this.st.executeQuery(sql);
      if (rs.next()) return true;
    } catch (Exception e) { e.printStackTrace(); }
   
    return false;
  }

}

package controllers;

import java.sql.Connection;
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
  
  public void insertUser(String string) {
    // TODO Auto-generated method stub
    
  }

  public void deleteUser(String username) {
    // TODO Auto-generated method stub
    
  }

}

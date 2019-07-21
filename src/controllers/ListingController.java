package controllers;

import java.sql.Connection;
import java.sql.Statement;

import enums.ListingType;

public class ListingController {

  private SQLController sqlMngr = null;
  private Connection conn = null;
  private Statement st = null;
  
  public ListingController() {
    this.sqlMngr = SQLController.getInstance();
    this.conn = sqlMngr.conn;
    this.st = sqlMngr.st;
  }
  
  public void insertListing(String username, ListingType listingType, String address) {
    // TODO Auto-generated method stub
    
  }

}

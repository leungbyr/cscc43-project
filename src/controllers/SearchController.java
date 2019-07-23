package controllers;

import java.sql.Connection;
import java.sql.Statement;

import cli.SearchFilter;

public class SearchController {
  
  private SQLController sqlMngr = null;
  private Connection conn = null;
  private Statement st = null;
  
  public SearchController() {
    this.sqlMngr = SQLController.getInstance();
    this.conn = sqlMngr.conn;
    this.st = sqlMngr.st;
  }

  public void byVicinity(String lat, String lon, int maxDistance, int sort, SearchFilter filters) {
    // TODO Auto-generated method stub
    // sort: 1 = by price, 2 = by distance
    
  }

  public void byPostalCode(String postalCode, SearchFilter filters) {
    // TODO Auto-generated method stub
    
  }

  public void byAddress(String address) {
    // TODO Auto-generated method stub
    
  }
  
}

package controllers;

import java.sql.Connection;
import java.sql.Statement;

public class ReportsController {
  
  private SQLController sqlMngr = null;
  private Connection conn = null;
  private Statement st = null;
  
  public ReportsController() {
    this.sqlMngr = SQLController.getInstance();
    this.conn = sqlMngr.conn;
    this.st = sqlMngr.st;
  }
  
  public void printBookingsReport(String startDate, String endDate, String city, String postalCode) {
    // TODO Auto-generated method stub
    
  }

  public void printListingsReport(String country, String city, String postalCode) {
    // TODO Auto-generated method stub
    
  }

  public void printHostsReport(String country, String city) {
    // TODO Auto-generated method stub
    
  }

  public void printCommercialHosts() {
    // TODO Auto-generated method stub
    
  }

  public void printRentersReport(String string, String string2, String string3) {
    // TODO Auto-generated method stub
    
  }

  public void renterCancellationsRanking() {
    // TODO Auto-generated method stub
    
  }

  public void hostCancellationsRanking() {
    // TODO Auto-generated method stub
    
  }
}

package controllers;

import java.sql.Connection;
import java.sql.ResultSet;
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
  
  public void insertListing(String username, ListingType listingType, String address, String lat, String lon, String city, String country, String postalCode) {
    String listingSql = String.format("INSERT INTO Listings(lat, lon, address, postal, city, country, type) VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s')",
        lat, lon, address, postalCode, city, country, listingType.name());
    String hostedSql = String.format("INSERT INTO Hosted_by(sin, lat, lon) VALUES (%s, %s, %s)", this.getSin(username), lat, lon);
    try {
      this.st.execute(listingSql);
      this.st.execute(hostedSql);
    } catch (Exception e) { e.printStackTrace(); }
  }

  public void printUpcomingBookings(String username) {
    // TODO Auto-generated method stub
    
  }

  public void printHostedListings(String username) {
    // TODO Auto-generated method stub
    
  }

  public void printPastBookings(String username) {
    // TODO Auto-generated method stub
    
  }
  
  private String getSin(String username) {
    String sin = null;
    String sql = String.format("SELECT * FROM Users WHERE (username='%s')", username);
    
    try {
      ResultSet rs = this.st.executeQuery(sql);
      if (rs.next()) {
        sin = rs.getString("sin");
      } else {
        return "";
      }
    } catch (Exception e) { e.printStackTrace(); }
    
    return sin;
  }

}

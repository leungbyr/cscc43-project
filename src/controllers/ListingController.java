package controllers;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import cli.AvailableDate;
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
  
  public void insertListing(String username, ListingType listingType, String address, String lat, String lon, String city, String country, String postalCode, List<AvailableDate> availableDates) {
    String listingSql = String.format("INSERT INTO Listings(lat, lon, address, postal, city, country, type) VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s')",
        lat, lon, address, postalCode, city, country, listingType.name());
    String hostedSql = String.format("INSERT INTO Hosted_by(sin, lat, lon) VALUES (%s, %s, %s)", this.getSin(username), lat, lon);
    try {
      this.st.execute(listingSql);
      this.st.execute(hostedSql);
      
      // Insert available dates
      for (AvailableDate date : availableDates) {
        java.sql.Date sqlDate = new java.sql.Date(date.getDate().getTime());
        String sql = "INSERT INTO Available_on(lat, lon, date, price)" + " VALUES (?, ?, ?, ?)";
        PreparedStatement preparedStmt = conn.prepareStatement(sql);
        preparedStmt.setString(1, lat);
        preparedStmt.setString(2, lon);
        preparedStmt.setDate(3, sqlDate);
        preparedStmt.setDouble(4, date.getPrice());
        preparedStmt.execute();
      }
    } catch (Exception e) { e.printStackTrace(); }
  }

  public void printUpcomingBookings(String username) { 
    
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
  
  public BigDecimal[] getNearestListingPrices(String lat, String lon, int numListings) {
    BigDecimal[] prices = new BigDecimal[numListings];
    String sql = String.format("SELECT lat, lon, SQRT(POW(69.1 * (lat - %s), 2) + POW(69.1 * (%s - lon) * COS(lat / 57.3), 2)) AS distance FROM Available_on ORDER BY distance;",
        lat, lon);
    
    try {
      ResultSet rs = this.st.executeQuery(sql);
      for (int i = 0; rs.next() && i < numListings; i++) {
        String price = rs.getString("price");
        prices[i] = new BigDecimal(price);
      }
    } catch (Exception e) { e.printStackTrace(); }
    
    return prices;
  }

}

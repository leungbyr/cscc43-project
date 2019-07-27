package controllers;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;

import cli.AvailableDate;
import enums.Amenity;
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
  
  public boolean insertListing(String username, ListingType listingType, String address, String lat, String lon, String city, String country, String postalCode, List<AvailableDate> availableDates, List<Amenity> amenities) {
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
      
      // Insert amenities
      for (Amenity amenity : amenities) {
        String sql = String.format("INSERT INTO Offers(lat, lon, name) VALUES (%s, %s, '%s');", lat, lon, amenity.name());
        this.st.execute(sql);
      }
    } catch (java.sql.SQLIntegrityConstraintViolationException e) {
      return false;
    } catch (SQLException e) { e.printStackTrace(); }
    
    return true;
  }

  public void printUpcomingBookings(String username) { 
    Date currentDate = new Date();
    
  }

  public void printHostedListings(String username) {
    String sql = "SELECT * FROM LISTINGS LEFT JOIN Hosted_by ON Listings.lat = Hosted_by.lat AND Listings.lon = Hosted_by.lon AND Hosted_by.sin = "
                    + this.getSin(username) + ";";
    
    try {
      ResultSet rs = this.st.executeQuery(sql);
      System.out.println("=========LISTINGS=========");
      for (int i = 1; rs.next(); i++) {
        String lat = rs.getString("lat");
        String lon = rs.getString("lon");
        String type = ListingType.valueOf(rs.getString("type")).toString();
        String address = rs.getString("address");
        String city = rs.getString("city");
        String country = rs.getString("country");
        String postal = rs.getString("postal");
        
        System.out.println(i + ". " + type + " at "  + address + ", " + city + ", " + country + ", " + postal + " (" + lat + ", " + lon + ")");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
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
  
  private BigDecimal[] getNearestListingPrices(String lat, String lon, int numListings) {
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

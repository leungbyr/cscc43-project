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

  public ResultSet printUpcomingBookings(String username) { 
    String sql = "SELECT * FROM Listings INNER JOIN Has_rented ON Listings.lat = Has_rented.lat AND Listings.lon = Has_rented.lon WHERE canceled = 0 AND sin = ? AND date >= (SELECT CURDATE());";
    ResultSet rs = null;
    
    try {
      PreparedStatement preparedStmt = conn.prepareStatement(sql);
      preparedStmt.setString(1, this.getSin(username));
      rs = preparedStmt.executeQuery();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    
    return rs;
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

  public ResultSet printPastBookings(String username) {
    String sql = "SELECT * FROM Listings INNER JOIN Has_rented ON Listings.lat = Has_rented.lat AND Listings.lon = Has_rented.lon WHERE sin = ? AND date < (SELECT CURDATE());";
    ResultSet rs = null;
    
    try {
      PreparedStatement preparedStmt = conn.prepareStatement(sql);
      preparedStmt.setString(1, this.getSin(username));
      rs = preparedStmt.executeQuery();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    
    return rs;
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

  public boolean bookListing(String username, String lat, String lon, Date date, BigDecimal price) {
    java.sql.Date sqlDate = new java.sql.Date(date.getTime());
    String rentedSql = "INSERT INTO Has_rented(sin, lat, lon, date, price, canceled) " + "VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE canceled = 0;";
    String availableSql = "DELETE FROM Available_on WHERE lat = ? AND lon = ? AND date = ?;";
    
    try {
      PreparedStatement preparedStmt;
      preparedStmt = conn.prepareStatement(rentedSql);
      preparedStmt.setString(1, this.getSin(username));
      preparedStmt.setString(2, lat);
      preparedStmt.setString(3, lon);
      preparedStmt.setDate(4, sqlDate);
      preparedStmt.setBigDecimal(5, price);
      preparedStmt.setInt(6, 0);
      preparedStmt.execute();
      preparedStmt = conn.prepareStatement(availableSql);
      preparedStmt.setString(1, lat);
      preparedStmt.setString(2, lon);
      preparedStmt.setDate(3, sqlDate);
      preparedStmt.execute();
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
    
    return true;
  }

  public boolean cancelBooking(String username, String lat, String lon, Date date, BigDecimal price) {
    java.sql.Date sqlDate = new java.sql.Date(date.getTime());
    String rentedSql = "UPDATE Has_rented SET canceled = 1 WHERE sin = ? AND lat = ? AND lon = ? AND date = ?;";
    String availableSql = "INSERT INTO Available_on(lat, lon, date, price) VALUES (?, ?, ?, ?);";
    
    try {
      PreparedStatement preparedStmt;
      preparedStmt = conn.prepareStatement(rentedSql);
      preparedStmt.setString(1, this.getSin(username));
      preparedStmt.setString(2, lat);
      preparedStmt.setString(3, lon);
      preparedStmt.setDate(4, sqlDate);
      preparedStmt.execute();
      preparedStmt = conn.prepareStatement(availableSql);
      preparedStmt.setString(1, lat);
      preparedStmt.setString(2, lon);
      preparedStmt.setDate(3, sqlDate);
      preparedStmt.setBigDecimal(4, price);
      preparedStmt.execute();
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
    
    return true;  
  }
  
}


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
        String sql = "INSERT INTO Available_on(lat, lon, date, price, removed)" + " VALUES (?, ?, ?, ?, 0)";
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
  
  public double getAmenityValue(Amenity amenity) {
    String amenityInSql = String.format("SELECT AVG(price), COUNT(name) FROM Available_on INNER JOIN Offers ON Available_on.lat = Offers.lat AND Available_on.lon = Offers.lon WHERE name = '%s';", amenity.name());
    String amenityNotInSql = String.format("SELECT AVG(price), COUNT(name) FROM Available_on INNER JOIN Offers ON Available_on.lat = Offers.lat AND Available_on.lon = Offers.lon WHERE name <> '%s';", amenity.name());
    double amenityVal = -1;
    
    try {
      ResultSet rs = this.st.executeQuery(amenityInSql);
      rs.next();
      double amenityInAvg = rs.getDouble("AVG(price)");
      double amenityInCount = rs.getDouble("COUNT(name)");
      rs = this.st.executeQuery(amenityNotInSql);
      rs.next();
      double amenityNotInAvg = rs.getDouble("AVG(price)");
      double amenityNotInCount = rs.getDouble("COUNT(name)");
      double amenityInWeighted = (amenityInCount == 0) ? 0 : (amenityInAvg / amenityInCount);
      double amenityNotInWeighted = (amenityNotInCount == 0) ? 0 : (amenityNotInAvg / amenityNotInCount);
      amenityVal = amenityInWeighted - amenityNotInWeighted;
    } catch (SQLException e) {
      e.printStackTrace();
    }
    
    return amenityVal;
  }

  public ResultSet printUpcomingBookings(String username) { 
    String sql = "SELECT * FROM Listings INNER JOIN Has_rented ON Listings.lat = Has_rented.lat AND Listings.lon = Has_rented.lon WHERE canceled = 0 AND sin = ? AND date >= (SELECT CURDATE()) ORDER BY date;";
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

  public ResultSet printHostedListings(String username) {
    String sql = String.format(
        "SELECT DISTINCT Listings.lat, Listings.lon, price, date, type, address, city, country, postal "
        + "FROM Listings "
        + "INNER JOIN Hosted_by ON Listings.lat = Hosted_by.lat AND Listings.lon = Hosted_by.lon "
        + "INNER JOIN Available_on ON Listings.lat = Available_on.lat AND Listings.lon = Available_on.lon "
        + "WHERE sin = %s AND removed = 0 AND date >= CURDATE() ORDER BY date;", this.getSin(username));
    
    ResultSet rs = null;
    try {
      rs = this.st.executeQuery(sql);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    
    return rs;
  }
  
  public ResultSet printRentedOut(String username) {
    String sql = String.format(
        "SELECT DISTINCT Listings.lat, Listings.lon, price, date, type, address, city, country, postal "
        + "FROM Listings "
        + "INNER JOIN Hosted_by ON Listings.lat = Hosted_by.lat AND Listings.lon = Hosted_by.lon "
        + "INNER JOIN Has_rented ON Listings.lat = Has_rented.lat AND Listings.lon = Has_rented.lon "
        + "WHERE Hosted_by.sin = %s AND canceled = 0 AND date >= CURDATE() ORDER BY date;", this.getSin(username));
    
    ResultSet rs = null;
    try {
      rs = this.st.executeQuery(sql);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    
    return rs;
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
  
  public double getSuggestedPrice(String lat, String lon, int numListings) {
    String sql = String.format("SELECT lat, lon, price, SQRT(POW(69.1 * (lat - %s), 2) + POW(69.1 * (%s - lon) * COS(lat / 57.3), 2)) AS distance FROM Available_on WHERE removed = 0 ORDER BY distance;",
        lat, lon);
    double[] prices = new double[numListings];
    double[] distances = new double[numListings];
    double totalDist = 0;
    
    try {
      ResultSet rs = this.st.executeQuery(sql);
      int i = 0;
      for (; rs.next() && i < numListings; i++) {
        prices[i] = rs.getDouble("price");
        distances[i] = rs.getDouble("distance");
        totalDist = totalDist + distances[i];
      }
      
      numListings = i;
    } catch (Exception e) { e.printStackTrace(); }
    
    double suggestedPrice = 0;
    for (int i = 0; i < numListings; i++) {
      double distanceFrac = distances[i] / totalDist;
      suggestedPrice = suggestedPrice + (prices[i] * distanceFrac);  
    }
    
    return suggestedPrice;
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

  public boolean cancelBooking(String lat, String lon, Date date, BigDecimal price, boolean makeAvailable) {
    java.sql.Date sqlDate = new java.sql.Date(date.getTime());
    String rentedSql = "UPDATE Has_rented SET canceled = 1 WHERE lat = ? AND lon = ? AND date = ?;";
    String availableSql = "INSERT INTO Available_on(lat, lon, date, price) VALUES (?, ?, ?, ?);";
    
    try {
      PreparedStatement preparedStmt;
      preparedStmt = conn.prepareStatement(rentedSql);
      preparedStmt.setString(1, lat);
      preparedStmt.setString(2, lon);
      preparedStmt.setDate(3, sqlDate);
      preparedStmt.execute();
      if (makeAvailable) {
        preparedStmt = conn.prepareStatement(availableSql);
        preparedStmt.setString(1, lat);
        preparedStmt.setString(2, lon);
        preparedStmt.setDate(3, sqlDate);
        preparedStmt.setBigDecimal(4, price);
        preparedStmt.execute();
      }
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
    
    return true;  
  }

  public boolean removeListing(String lat, String lon, Date date) {
    java.sql.Date sqlDate = new java.sql.Date(date.getTime());
    String availableSql = "UPDATE Available_on SET removed = 1 WHERE lat = ? AND lon = ? AND date = ?;";
    
    try {
      PreparedStatement preparedStmt;
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

  public boolean updatePrice(String lat, String lon, Date date, BigDecimal price) {
    java.sql.Date sqlDate = new java.sql.Date(date.getTime());
    String availableSql = "UPDATE Available_on SET price = ? WHERE lat = ? AND lon = ? AND date = ? AND date > CURDATE() + 1;";
    int updated = 0;
    
    try {
      PreparedStatement preparedStmt;
      preparedStmt = conn.prepareStatement(availableSql);
      preparedStmt.setBigDecimal(1, price);
      preparedStmt.setString(2, lat);
      preparedStmt.setString(3, lon);
      preparedStmt.setDate(4, sqlDate);
      updated = preparedStmt.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
    
    if (updated == 0) {
      return false;
    }
    
    return true;
  }

  public ResultSet printPastRentedOut(String username) {
    String sql = String.format(
        "SELECT DISTINCT Listings.lat, Listings.lon, price, date, type, address, city, country, postal "
        + "FROM Listings "
        + "INNER JOIN Hosted_by ON Listings.lat = Hosted_by.lat AND Listings.lon = Hosted_by.lon "
        + "INNER JOIN Has_rented ON Listings.lat = Has_rented.lat AND Listings.lon = Has_rented.lon "
        + "WHERE Hosted_by.sin = %s AND canceled = 0 AND date < CURDATE() ORDER BY date;", this.getSin(username));
    
    ResultSet rs = null;
    try {
      rs = this.st.executeQuery(sql);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    
    return rs;
  }
  
}


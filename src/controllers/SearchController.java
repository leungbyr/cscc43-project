package controllers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import cli.SearchFilter;
import enums.Amenity;
import enums.ListingType;

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
    // sort: 1 = by price, 2 = by distance
    // Building sql query
    String priceSql = "";
    if (filters.minPrice > 0 && filters.maxPrice == -1) {
      priceSql = "price >= " + Integer.toString(filters.minPrice);
    } else if (filters.maxPrice != -1) {
      priceSql = String.format("price BETWEEN %d AND %d", filters.minPrice, filters.maxPrice);
    }
    
    String datesSql = "";
    if (!filters.startDate.equals("") && !filters.endDate.equals("")) {
      datesSql = String.format("Available_on.date BETWEEN '%s' AND '%s'", filters.startDate, filters.endDate);
    }
    
    String sql = String.format(
        "SELECT DISTINCT Listings.lat, Listings.lon, price, date, type, address, city, country, postal, SQRT(POW(111.2 * (Listings.lat - %s), 2) + POW(111.2 * (%s - Listings.lon) * COS(Listings.lat / 57.3), 2)) AS distance "
        + "FROM Listings "
        + "INNER JOIN Available_on ON Listings.lat = Available_on.lat AND Listings.lon = Available_on.lon "
        + "INNER JOIN Offers ON Listings.lat = Offers.lat AND Listings.lon = Offers.lon ", lat, lon);

    if (!priceSql.equals("") && !datesSql.equals("")) {
      sql = sql + "WHERE " + priceSql + " AND " + datesSql;
    } else if (!priceSql.equals("")) {
      sql = sql + "WHERE " + priceSql;
    } else if (!datesSql.equals("")) {
      sql = sql + "WHERE " + datesSql;
    }
    
    if (filters.amenities.size() > 0) {
      if (priceSql.equals("") && datesSql.equals("")) {
        sql = sql + "WHERE name IN (";
      } else {
        sql = sql + "AND name IN (";
      }
      
      for (Amenity amenity : filters.amenities) {
        sql = sql + "'" + amenity.name() + "', ";
      }
      
      sql = sql.substring(0, sql.length() - 2) + ") GROUP BY Listings.lat, Listings.lon, date HAVING COUNT(*) = " + filters.amenities.size() + " AND ";
    } else {
      sql = sql + "HAVING ";
    }
    
    sql = sql + String.format("distance <= %d ", maxDistance);
    
    if (sort == 1) {
      sql = sql + "ORDER BY price, date;";
    } else if (sort == 2) {
      sql = sql + "ORDER BY distance, date;";
    }
    
    try {
      ResultSet rs = this.st.executeQuery(sql);
      for (int i = 1; rs.next(); i++) {
        String rsLat = rs.getString("lat");
        String rsLon = rs.getString("lon");
        String type = ListingType.valueOf(rs.getString("type")).toString();
        String address = rs.getString("address");
        String city = rs.getString("city");
        String country = rs.getString("country");
        String postal = rs.getString("postal");
        String date = rs.getString("date");
        String price = rs.getString("price");
        String distance = rs.getString("distance");
        System.out.println(i + ". " + type + " at "  + address + ", " + city + ", " + country + ", " + postal + " (" + rsLat + ", " + rsLon + ")");
        System.out.println("     Date: " + date + ", " + " Price: $" + price + ", Distance: " + distance + "km");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void byPostalCode(String postalCode, SearchFilter filters) {
    // TODO Auto-generated method stub
    
  }

  public void byAddress(String address) {
    // TODO Auto-generated method stub
    
  }
  
}

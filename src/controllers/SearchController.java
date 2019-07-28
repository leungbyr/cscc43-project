package controllers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import cli.SearchFilter;
import enums.Amenity;

public class SearchController {
  
  private SQLController sqlMngr = null;
  private Connection conn = null;
  private Statement st = null;
  
  public SearchController() {
    this.sqlMngr = SQLController.getInstance();
    this.conn = sqlMngr.conn;
    this.st = sqlMngr.st;
  }

  public ResultSet byVicinity(String lat, String lon, int maxDistance, int sort, SearchFilter filters) {
    // sort: 1 = by price, 2 = by distance
    // Building sql query
    String sql = String.format(
        "SELECT DISTINCT Listings.lat, Listings.lon, price, date, type, address, city, country, postal, SQRT(POW(111.2 * (Listings.lat - %s), 2) + POW(111.2 * (%s - Listings.lon) * COS(Listings.lat / 57.3), 2)) AS distance "
        + "FROM Listings "
        + "INNER JOIN Available_on ON Listings.lat = Available_on.lat AND Listings.lon = Available_on.lon "
        + "INNER JOIN Offers ON Listings.lat = Offers.lat AND Listings.lon = Offers.lon ", lat, lon);
    
    String priceSql = "";
    if (filters.minPrice > 0 && filters.maxPrice == -1) {
      priceSql = String.format("price >= %d ", filters.minPrice);
    } else if (filters.maxPrice != -1) {
      priceSql = String.format("price BETWEEN %d AND %d ", filters.minPrice, filters.maxPrice);
    }
    
    String datesSql = "";
    if (!filters.startDate.equals("") && !filters.endDate.equals("")) {
      datesSql = String.format("Available_on.date BETWEEN '%s' AND '%s' ", filters.startDate, filters.endDate);
    } else {
      datesSql = ("Available_on.date >= CURDATE() ");
    }
    
    if (!priceSql.equals("") && !datesSql.equals("")) {
      sql = sql + "WHERE " + priceSql + "AND " + datesSql;
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
      sql = sql + "ORDER BY price ASC, date;";
    } else if (sort == 2) {
      sql = sql + "ORDER BY price DESC, date";
    } else if (sort == 3) {
      sql = sql + "ORDER BY distance, date;";
    }
    
    ResultSet rs = null;
    try {
      rs = this.st.executeQuery(sql);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    
    return rs;
  }

  public ResultSet byPostalCode(String postalCode, SearchFilter filters, int sort) {
    // sort: 1 = by price, 2 = by date
    String sql = String.format(
        "SELECT DISTINCT Listings.lat, Listings.lon, price, date, type, address, city, country, postal "
        + "FROM Listings "
        + "INNER JOIN Available_on ON Listings.lat = Available_on.lat AND Listings.lon = Available_on.lon "
        + "INNER JOIN Offers ON Listings.lat = Offers.lat AND Listings.lon = Offers.lon "
        + "WHERE SUBSTRING(postal, 1, 3) = '%s' ", postalCode.substring(0, 3));
    
    String priceSql = "";
    if (filters.minPrice > 0 && filters.maxPrice == -1) {
      priceSql = String.format("price >= %d ", filters.minPrice);
    } else if (filters.maxPrice != -1) {
      priceSql = String.format("price BETWEEN %d AND %d ", filters.minPrice, filters.maxPrice);
    }
    
    String datesSql = "";
    if (!filters.startDate.equals("") && !filters.endDate.equals("")) {
      datesSql = String.format("Available_on.date BETWEEN '%s' AND '%s' ", filters.startDate, filters.endDate);
    }
    
    if (!priceSql.equals("") && !datesSql.equals("")) {
      sql = sql + "AND " + priceSql + "AND " + datesSql;
    } else if (!priceSql.equals("")) {
      sql = sql + "AND " + priceSql;
    } else if (!datesSql.equals("")) {
      sql = sql + "AND " + datesSql;
    }
    
    if (filters.amenities.size() > 0) {
      sql = sql + "AND name IN (";
      
      for (Amenity amenity : filters.amenities) {
        sql = sql + "'" + amenity.name() + "', ";
      }
      
      sql = sql.substring(0, sql.length() - 2) + ") GROUP BY Listings.lat, Listings.lon, date HAVING COUNT(*) = " + filters.amenities.size() + " ";
    }
        
    if (sort == 1) {
      sql = sql + "ORDER BY price ASC;";
    } else if (sort == 2) {
        sql = sql + "ORDER BY date DESC;";
    } else if (sort == 3) {
      sql = sql + "ORDER BY date ASC;";
    } else if (sort == 3) {
      sql = sql + "ORDER BY date DESC;";
    }
    
    ResultSet rs = null;
    try {
      rs = this.st.executeQuery(sql);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    
    return rs;
  }

  public ResultSet byAddress(String address) {
    String sql = String.format(
        "SELECT DISTINCT Listings.lat, Listings.lon, price, date, type, address, city, country, postal "
        + "FROM Listings "
        + "INNER JOIN Available_on ON Listings.lat = Available_on.lat AND Listings.lon = Available_on.lon "
        + "WHERE address = '%s' ORDER BY date;", address);
    
    ResultSet rs = null;
    try {
      rs = this.st.executeQuery(sql);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    
    return rs;
  }
  
}

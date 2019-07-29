package controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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

  private String helperFunction(ArrayList<String> arr, String splitToken) {
    String sql = "";
    for (int i=0; i<arr.size(); i++) {
      if (i != 0) {
        sql += splitToken + " ";
      }
      sql += arr.get(i);
      if (arr.size() >= 2 && i == 0) {
        sql += " ";
      }
    }
    
    return sql;
  }
  
  private String constructSql(ArrayList<String> selectPart, ArrayList<String> fromPart, ArrayList<String> wherePart, ArrayList<String> groupByPart, ArrayList<String> havingPart) {
    String sql = "";
    if (!selectPart.isEmpty())
      sql += "SELECT " + helperFunction(selectPart, ",") + " ";
    if (!fromPart.isEmpty())
      sql += "FROM " + helperFunction(fromPart, "NATURAL JOIN") + " ";
    if (!wherePart.isEmpty())
      sql += "WHERE " + helperFunction(wherePart, "AND") + " ";
    if (!groupByPart.isEmpty())
      sql += "GROUP BY " + helperFunction(groupByPart, ",") + " ";
    if (!havingPart.isEmpty())
      sql += "HAVING " + helperFunction(havingPart, ",") + " ";
    
    return sql;
  }
  
  public ResultSet byVicinity(String lat, String lon, int maxDistance, int sort, SearchFilter filters) {
    
    ResultSet ret = null;
    
    ArrayList<String> selectPart = new ArrayList<>();
    ArrayList<String> fromPart = new ArrayList<>();
    ArrayList<String> wherePart = new ArrayList<>();
    ArrayList<String> groupByPart = new ArrayList<>();
    ArrayList<String> havingPart = new ArrayList<>();
    
    // Foundation
    selectPart.add("lat");
    selectPart.add("lon");
    selectPart.add("date");
    selectPart.add("price");
    
    fromPart.add("Available_on");
    
    // Custom price range?
    if (filters.minPrice > 0 && filters.maxPrice == -1) {
      wherePart.add(String.format("price >= %d ", filters.minPrice));
    } else if (filters.maxPrice != -1) {
      wherePart.add(String.format("price BETWEEN %d AND %d ", filters.minPrice, filters.maxPrice));
    }
    
    // Custom date range?
    if (!filters.startDate.equals("") && !filters.endDate.equals("")) {
      wherePart.add(String.format("date >= CURDATE() AND date BETWEEN '%s' AND '%s' ", filters.startDate, filters.endDate));
    } else {
      wherePart.add("date >= CURDATE() ");
    }
    
    // Custom amenities?
    if (filters.amenities.size() > 0) {
      selectPart.add("name");
      
      fromPart.add("Offers");
      
      // Generate string of amenities
      String amenities = "name in (";
      for (Amenity amenity : filters.amenities) {
        amenities = amenities + "'" + amenity.name() + "', ";
      }
      amenities = amenities.substring(0, amenities.length() - 2) + ")";
      
      wherePart.add(amenities);
      
      groupByPart.add("lat");
      groupByPart.add("lon");
      groupByPart.add("date");
      groupByPart.add("price");
      groupByPart.add("name");
    }
    
    // Form the complex query as view
    String sql = constructSql(selectPart, fromPart, wherePart, groupByPart, havingPart);
    sql = String.format("CREATE OR REPLACE VIEW a AS (%s)", sql);
    
    // Create view
    try {
      PreparedStatement ps = null;
      ResultSet rs = null;
      
      ps = conn.prepareStatement(sql);
      ps.executeUpdate();
    } catch (Exception e) { e.printStackTrace(); }
    
    selectPart.clear();
    fromPart.clear();
    wherePart.clear();
    groupByPart.clear();
    havingPart.clear();
    
    // Extra level of query if amenities
    if (filters.amenities.size() > 0) {
      // Foundation
      selectPart.add("lat");
      selectPart.add("lon");
      selectPart.add("date");
      selectPart.add("price");
      selectPart.add("COUNT(*) AS counts");
      
      fromPart.add("a");
      
      groupByPart.add("lat");
      groupByPart.add("lon");
      groupByPart.add("date");
      groupByPart.add("price");
      
      havingPart.add(String.format("counts=%d", filters.amenities.size()));
      
      sql = constructSql(selectPart, fromPart, wherePart, groupByPart, havingPart);
      sql = String.format("CREATE OR REPLACE VIEW temp AS (%s)", sql);
    } else {
      sql = "CREATE OR REPLACE VIEW temp AS (SELECT * FROM a)";
    }
    // Create view
    try {
      PreparedStatement ps = null;
      ResultSet rs = null;
      
      ps = conn.prepareStatement(sql);
      ps.executeUpdate();
    } catch (Exception e) { e.printStackTrace(); }
    
    // More simple query, so just hard-code
    sql = String.format("SELECT *, SQRT(POW(111.2 * (lat - %s), 2) + POW(111.2 * (%s - lon) * COS(lat / 57.3), 2)) AS distance"
        + " " + "FROM temp NATURAL JOIN Available_on NATURAL JOIN Listings WHERE removed=0 HAVING distance <= %d", lat, lon, maxDistance);
    
    if (sort == 1) {
      sql = sql + " ORDER BY price ASC, date";
    } else if (sort == 2) {
      sql = sql + " ORDER BY price DESC, date";
    } else if (sort == 3) {
      sql = sql + " ORDER BY distance ASC, date";
    } else if (sort == 4) {
      sql = sql + " ORDER BY distance DESC, date";
    }
    
    try {   
      PreparedStatement ps = null;
      ResultSet rs = null;
      
      ps = conn.prepareStatement(sql);
      
      rs = ps.executeQuery();
      
      ret = rs;
    } catch (SQLException e) {
      e.printStackTrace();
    }
    
    // Delete views
    sql = "DROP VIEW a, temp";
    try {   
      PreparedStatement ps = null;      
      
      ps = conn.prepareStatement(sql);
      ps.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    
    return ret;
  }
  
  public ResultSet byPostalCode(String postalCode, SearchFilter filters, int sort) {
    
    ResultSet ret = null;
    
    ArrayList<String> selectPart = new ArrayList<>();
    ArrayList<String> fromPart = new ArrayList<>();
    ArrayList<String> wherePart = new ArrayList<>();
    ArrayList<String> groupByPart = new ArrayList<>();
    ArrayList<String> havingPart = new ArrayList<>();
    
    // Foundation
    selectPart.add("lat");
    selectPart.add("lon");
    selectPart.add("date");
    selectPart.add("price");
    
    fromPart.add("Available_on");
    
    // Custom price range?
    if (filters.minPrice > 0 && filters.maxPrice == -1) {
      wherePart.add(String.format("price >= %d ", filters.minPrice));
    } else if (filters.maxPrice != -1) {
      wherePart.add(String.format("price BETWEEN %d AND %d ", filters.minPrice, filters.maxPrice));
    }
    
    // Custom date range?
    if (!filters.startDate.equals("") && !filters.endDate.equals("")) {
      wherePart.add(String.format("date >= CURDATE() AND date BETWEEN '%s' AND '%s' ", filters.startDate, filters.endDate));
    } else {
      wherePart.add("date >= CURDATE() ");
    }
    
    // Custom amenities?
    if (filters.amenities.size() > 0) {
      selectPart.add("name");
      
      fromPart.add("Offers");
      
      // Generate string of amenities
      String amenities = "name in (";
      for (Amenity amenity : filters.amenities) {
        amenities = amenities + "'" + amenity.name() + "', ";
      }
      amenities = amenities.substring(0, amenities.length() - 2) + ")";
      
      wherePart.add(amenities);
      
      groupByPart.add("lat");
      groupByPart.add("lon");
      groupByPart.add("date");
      groupByPart.add("price");
      groupByPart.add("name");
    }
    
    // Form the complex query as view
    String sql = constructSql(selectPart, fromPart, wherePart, groupByPart, havingPart);
    sql = String.format("CREATE OR REPLACE VIEW a AS (%s)", sql);
    
    // Create view
    try {
      PreparedStatement ps = null;
      ResultSet rs = null;
      
      ps = conn.prepareStatement(sql);
      ps.executeUpdate();
    } catch (Exception e) { e.printStackTrace(); }
    
    selectPart.clear();
    fromPart.clear();
    wherePart.clear();
    groupByPart.clear();
    havingPart.clear();
    
    // Extra level of query if amenities
    if (filters.amenities.size() > 0) {
      // Foundation
      selectPart.add("lat");
      selectPart.add("lon");
      selectPart.add("date");
      selectPart.add("price");
      selectPart.add("COUNT(*) AS counts");
      
      fromPart.add("a");
      
      groupByPart.add("lat");
      groupByPart.add("lon");
      groupByPart.add("date");
      groupByPart.add("price");
      
      havingPart.add(String.format("counts=%d", filters.amenities.size()));
      
      sql = constructSql(selectPart, fromPart, wherePart, groupByPart, havingPart);
      sql = String.format("CREATE OR REPLACE VIEW temp AS (%s)", sql);
    } else {
      sql = "CREATE OR REPLACE VIEW temp AS (SELECT * FROM a)";
    }
    // Create view
    try {
      PreparedStatement ps = null;
      ResultSet rs = null;
      
      ps = conn.prepareStatement(sql);
      ps.executeUpdate();
    } catch (Exception e) { e.printStackTrace(); }
    
    // More simple query, so just hard-code
    sql = String.format("SELECT *"
        + " " + "FROM temp NATURAL JOIN Available_on NATURAL JOIN Listings WHERE removed=0 AND SUBSTRING(postal, 1, 3) = '%s'", postalCode.substring(0, 3));
    
    if (sort == 1) {
      sql = sql + " ORDER BY price ASC, date";
    } else if (sort == 2) {
      sql = sql + " ORDER BY price DESC, date";
    } else if (sort == 3) {
      sql = sql + " ORDER BY date ASC, price";
    } else if (sort == 4) {
      sql = sql + " ORDER BY date DESC, price";
    }
    
    try {   
      PreparedStatement ps = null;
      ResultSet rs = null;
      
      ps = conn.prepareStatement(sql);
      
      rs = ps.executeQuery();
      
      ret = rs;
    } catch (SQLException e) {
      e.printStackTrace();
    }
    
    // Delete views
    sql = "DROP VIEW a, temp";
    try {   
      PreparedStatement ps = null;      
      
      ps = conn.prepareStatement(sql);
      ps.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    
    return ret;
  }

  public ResultSet byAddress(String address) {
    String sql = String.format(
        "SELECT DISTINCT Listings.lat, Listings.lon, price, date, type, address, city, country, postal "
        + "FROM Listings "
        + "INNER JOIN Available_on ON Listings.lat = Available_on.lat AND Listings.lon = Available_on.lon "
        + "WHERE address = '%s' AND date >= CURDATE() ORDER BY date;", address);
    
    ResultSet rs = null;
    try {
      rs = this.st.executeQuery(sql);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    
    return rs;
  }
  
}

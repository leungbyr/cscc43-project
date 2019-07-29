package controllers;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.PropertiesUtils;

public class ReportsController {

  private SQLController sqlMngr = null;
  private Connection conn = null;
  private Statement st = null;

  public ReportsController() {
    this.sqlMngr = SQLController.getInstance();
    this.conn = sqlMngr.conn;
    this.st = sqlMngr.st;
  }

  /*
   * Provides total number of bookings in a specific date range by city.
   */
  public void printBookingsReportByCity(String startDate, String endDate) {
    // By city
    String sql;
    if (startDate.equals("")) {
      sql = "SELECT city, COUNT(*) AS counts FROM Has_rented NATURAL JOIN Listings GROUP BY city";
    } else {
      sql = "SELECT city, COUNT(*) AS counts FROM Has_rented NATURAL JOIN Listings WHERE date <= ? AND date >= ? GROUP BY city";
    }

    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = conn.prepareStatement(sql);
      if (!startDate.equals("")) {
        ps.setDate(1, Date.valueOf(endDate));
        ps.setDate(2, Date.valueOf(startDate));
      }

      rs = ps.executeQuery();

      System.out.println("=== By City ===");
      while (rs.next()) {
        System.out.printf("%s: %d\n", rs.getString("city"), rs.getInt("counts"));
      }
    } catch (Exception e) { e.printStackTrace(); }
  }

  /*
   * Provides total number of bookings in a specific date range by zip code within a city.
   */
  public void printBookingsReportByZipCode(String startDate, String endDate, String city) {  
    // By zip code
    String sql;
    if (startDate.equals("")) {
      sql = "SELECT postal, COUNT(*) AS counts FROM Has_rented NATURAL JOIN Listings WHERE city = ? GROUP BY postal";
    } else {
      sql = "SELECT postal, COUNT(*) AS counts FROM Has_rented NATURAL JOIN Listings WHERE date <= ? AND date >= ? AND city = ? GROUP BY postal";
    }

    int i = 0;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = conn.prepareStatement(sql);
      if (!startDate.equals("")) {
        ps.setDate(++i, Date.valueOf(endDate));
        ps.setDate(++i, Date.valueOf(startDate));
      }
      ps.setString(++i, city);

      rs = ps.executeQuery();

      System.out.println("=== By Zip Code ===");
      while (rs.next()) {
        System.out.printf("%s: %d\n", rs.getString("postal"), rs.getInt("counts"));
      }
    } catch (Exception e) { e.printStackTrace(); }
  }

  /*
   * Provide total number of listings per country, per country and city, and per country, city, and postal code
   */
  public void printListingsReport() {
    // Per country
    String sql = "SELECT country, COUNT(*) AS counts FROM Hosted_by NATURAL JOIN Listings GROUP BY country";

    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = conn.prepareStatement(sql);

      rs = ps.executeQuery();

      System.out.println("=== Per Country ===");

      while (rs.next()) {
        System.out.printf("%s: %d\n", rs.getString("country"), rs.getInt("counts"));
      }
    } catch (Exception e) { e.printStackTrace(); }

    // Per country, city
    sql = "SELECT country, city, COUNT(*) AS counts FROM Hosted_by NATURAL JOIN Listings GROUP BY country, city";

    ps = null;
    rs = null;
    try {
      ps = conn.prepareStatement(sql);

      rs = ps.executeQuery();

      System.out.println("=== Per Country, City ===");

      while (rs.next()) {
        System.out.printf("%s, %s: %d\n", rs.getString("country"), rs.getString("city"), rs.getInt("counts"));
      }
    } catch (Exception e) { e.printStackTrace(); }

    // Per country, city, postal
    sql = "SELECT country, city, postal, COUNT(*) AS counts FROM Hosted_by NATURAL JOIN Listings GROUP BY country, city, postal";

    ps = null;
    rs = null;
    try {
      ps = conn.prepareStatement(sql);

      rs = ps.executeQuery();

      System.out.println("=== Per Country, City, Postal ===");

      while (rs.next()) {
        System.out.printf("%s, %s, %s: %d\n", rs.getString("country"), rs.getString("city"), rs.getString("postal"), rs.getInt("counts"));
      }
    } catch (Exception e) { e.printStackTrace(); }
  }

  /*
   * Provides ranks of hosts by total number of listings per country
   */
  public void printHostsReportPerCountry() {
    String sql = "SELECT sin, country, COUNT(*) AS counts FROM Hosted_by NATURAL JOIN Listings GROUP BY sin, country ORDER BY country, counts DESC";

    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = conn.prepareStatement(sql);

      rs = ps.executeQuery();

      int rank = 1;
      String oldCountry = "";
      while (rs.next()) {
        String newCountry = rs.getString("country"); 
        if (!newCountry.equals(oldCountry)) {
          oldCountry = newCountry;
          rank = 1;
          System.out.printf("=== Ranks of Hosts in %s ===\n", oldCountry);
        }

        System.out.printf("%d. Host sin %s: %d listings\n", rank++, rs.getString("sin"), rs.getInt("counts"));
      }
    } catch (Exception e) { e.printStackTrace(); }
  }

  /*
   * Provides ranks of hosts by total number of listings per city
   */
  public void printHostsReportPerCity() {
    String sql = "SELECT sin, country, city, COUNT(*) AS counts FROM Hosted_by NATURAL JOIN Listings GROUP BY sin, country, city ORDER BY country, city, counts DESC";

    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = conn.prepareStatement(sql);

      rs = ps.executeQuery();

      int rank = 1;
      String oldCity = "";
      while (rs.next()) {
        String newCity = rs.getString("city") + ", " + rs.getString("country"); 
        if (!newCity.equals(oldCity)) {
          oldCity = newCity;
          rank = 1;
          System.out.printf("=== Ranks of Hosts in %s ===\n", oldCity);
        }

        System.out.printf("%d. Host sin %s: %d listings\n", rank, rs.getString("sin"), rs.getInt("counts"));
      }
    } catch (Exception e) { e.printStackTrace(); }
  }

  public void printCommercialHostsInCountry(String country) {
    String sql = "CREATE VIEW PossibleCommercialHosts AS (SELECT Hosted_by.sin, COUNT(*) AS counts FROM Listings NATURAL JOIN Hosted_by JOIN Users ON Users.sin=Hosted_by.sin WHERE country = ? GROUP BY sin)";

    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      // Create view
      ps = conn.prepareStatement(sql);
      ps.setString(1, country);

      ps.executeUpdate();
      // Perform query on view
      sql = "SELECT sin FROM PossibleCommercialHosts AS a INNER JOIN (SELECT SUM(counts) AS sum FROM PossibleCommercialHosts) AS b ON a.counts > 0.10*b.sum";

      ps = conn.prepareStatement(sql);

      rs = ps.executeQuery();

      System.out.printf("=== Potential Commercial Hosts in %s ===\n", country);
      while (rs.next()) {
        System.out.printf("User sin %s\n", rs.getString("sin"));
      }
      // Delete view now that we're done
      sql = "DROP VIEW PossibleCommercialHosts";

      ps = conn.prepareStatement(sql);

      ps.executeUpdate();
    } catch (Exception e) { e.printStackTrace(); }
  }

  public void printCommercialHostsInCountryAndCity(String country, String city) {
    String sql = "CREATE VIEW PossibleCommercialHosts AS (SELECT Hosted_by.sin, COUNT(*) AS counts FROM Listings NATURAL JOIN Hosted_by JOIN Users ON Users.sin=Hosted_by.sin WHERE country = ? AND city = ? GROUP BY sin)";

    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      // Create view
      ps = conn.prepareStatement(sql);
      ps.setString(1, country);
      ps.setString(2, city);

      ps.executeUpdate();
      // Perform query on view
      sql = "SELECT sin FROM PossibleCommercialHosts AS a INNER JOIN (SELECT SUM(counts) AS sum FROM PossibleCommercialHosts) AS b ON a.counts > 0.10*b.sum";

      ps = conn.prepareStatement(sql);

      rs = ps.executeQuery();

      System.out.printf("=== Potential Commercial Hosts in %s, %s ===\n", city, country);
      while (rs.next()) {
        System.out.printf("User sin %s\n", rs.getString("sin"));
      }
      // Delete view now that we're done
      sql = "DROP VIEW PossibleCommercialHosts";

      ps = conn.prepareStatement(sql);

      ps.executeUpdate();
    } catch (Exception e) { e.printStackTrace(); }
  }

  public void printRentersReportOverall(String startDate, String endDate) {
    String sql;
    if (startDate.equals("")) {
      sql = "SELECT sin, COUNT(*) AS counts FROM Users NATURAL JOIN Has_rented GROUP BY sin ORDER BY counts DESC";
    } else {
      sql = "SELECT sin, COUNT(*) AS counts FROM Users NATURAL JOIN Has_rented WHERE date <= ? AND date >= ? GROUP BY sin ORDER BY counts DESC"; 
    }


    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = conn.prepareStatement(sql);
      if (!startDate.equals("")) {
        ps.setDate(1, Date.valueOf(endDate));
        ps.setDate(2, Date.valueOf(startDate));
      }

      rs = ps.executeQuery();

      int rank = 1;
      System.out.println("=== Ranks of Renters Overall ===");
      while (rs.next()) {
        System.out.printf("%d. User sin %s\n", rank++, rs.getString("sin"));
      }
    } catch (Exception e) { e.printStackTrace(); }
  }

  public void printRentersReportPerCity(String startDate, String endDate) {
    String sql;
    if (startDate.equals("")) {
      sql = "SELECT sin, city, counts FROM (SELECT sin, COUNT(*) AS inyear_counts FROM Users NATURAL JOIN Has_rented WHERE YEAR(date) = YEAR(CURDATE()) GROUP BY sin HAVING inyear_counts >= 2) AS a NATURAL JOIN (SELECT sin, city, COUNT(*) AS counts FROM Users NATURAL JOIN Has_rented NATURAL JOIN Listings GROUP BY sin, city) AS b ORDER BY counts DESC";
    } else {
      sql = "SELECT sin, city, counts FROM (SELECT sin, COUNT(*) AS inyear_counts FROM Users NATURAL JOIN Has_rented WHERE YEAR(date) = YEAR(CURDATE()) GROUP BY sin HAVING inyear_counts >= 2) AS a NATURAL JOIN (SELECT sin, city, COUNT(*) AS counts FROM Users NATURAL JOIN Has_rented NATURAL JOIN Listings WHERE date <= ? AND date >= ? GROUP BY sin, city) AS b ORDER BY counts DESC";
    }

    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = conn.prepareStatement(sql);
      if (!startDate.equals("")) {
        ps.setDate(1, Date.valueOf(endDate));
        ps.setDate(2, Date.valueOf(startDate));
      }

      rs = ps.executeQuery();

      int rank = 1;
      String oldCity = "";
      System.out.println("=== Ranks of Renters Per City ===");
      while (rs.next()) {
        String newCity = rs.getString("city");
        if (!newCity.equals(oldCity)) {
          oldCity = newCity;
          rank = 1;
          System.out.printf("=== In %s ===\n", newCity);
        }
        System.out.printf("%d. User sin %s\n", rank++, rs.getString("sin"));
      }
    } catch (Exception e) { e.printStackTrace(); }
  }

  public void renterCancellationsRanking() {
    String sql = "CREATE VIEW RenterCancelationsWithinYear AS (SELECT sin, COUNT(*) as counts FROM Has_rented WHERE date > CURDATE() - 365 AND canceled = 1 GROUP BY sin)";

    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      // Create view as a way to refer to the complex query
      ps = conn.prepareStatement(sql);

      ps.executeUpdate();
      // Perform query on the view      
      sql = "SELECT sin, counts FROM RenterCancelationsWithinYear NATURAL JOIN (SELECT MAX(counts) as counts FROM RenterCancelationsWithinYear) AS temp";

      ps = conn.prepareStatement(sql);

      rs = ps.executeQuery();

      System.out.println("=== Largest Number of Cancelations By Renters ===");
      while (rs.next()) {
        System.out.printf("User sin %s: %d cancelations\n", rs.getString("sin"), rs.getInt("counts"));
      }
      // Remove view now that we're done
      sql = "DROP VIEW RenterCancelationsWithinYear";

      ps = conn.prepareStatement(sql);

      ps.executeUpdate();
    } catch (Exception e) { e.printStackTrace(); }
  }

  public void hostCancellationsRanking() {
    String sql = "CREATE VIEW HostCancelationsWithinYear AS (SELECT sin, COUNT(*) as counts FROM Hosted_by NATURAL JOIN Available_on WHERE date > DATE_SUB(CURDATE(), INTERVAL 365 DAY) AND removed = 1 GROUP BY sin)";

    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      // Create view as a way to refer to the complex query
      ps = conn.prepareStatement(sql);

      ps.executeUpdate();
      // Perform query on the view      
      sql = "SELECT sin, counts FROM HostCancelationsWithinYear NATURAL JOIN (SELECT MAX(counts) as counts FROM HostCancelationsWithinYear) AS temp";

      ps = conn.prepareStatement(sql);

      rs = ps.executeQuery();

      System.out.println("=== Largest Number of Cancelations By Hosts ===");
      while (rs.next()) {
        System.out.printf("User sin %s: %d cancelations\n", rs.getString("sin"), rs.getInt("counts"));
      }
      // Remove view now that we're done
      sql = "DROP VIEW HostCancelationsWithinYear";

      ps = conn.prepareStatement(sql);

      ps.executeUpdate();
    } catch (Exception e) { e.printStackTrace(); }
  }

  public void mostPopularNounForListings() {
    HashMap<String, HashMap<String, Integer>> coordToFreqList = new HashMap<>();
    
    String sql = "SELECT text, lat, lon FROM comments NATURAL JOIN posted_on_listing ORDER BY lat, lon";

    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = conn.prepareStatement(sql);

      rs = ps.executeQuery();

      while (rs.next()) {
        String[] tokens = rs.getString("text").split(" ");
        String key = rs.getString("lat") + ", " + rs.getString("lon");
        
        if (!coordToFreqList.containsKey(key)) {
          coordToFreqList.put(key, new HashMap<String, Integer>());
        }
        
        HashMap<String, Integer> freqList = coordToFreqList.get(key);
        
        for (String s : tokens) {
          if (!freqList.containsKey(s)) {
            freqList.put(s, 0);
          }
          freqList.replace(s, freqList.get(s) + 1);
        }
      }
      
      for (String lat_lon : coordToFreqList.keySet()) {
        HashMap<String, Integer> temp = coordToFreqList.get(lat_lon);
        System.out.printf("Top 5 nouns for (%s): ", lat_lon);
        ArrayList<String> freqListOrdered = new ArrayList<>();
        for (String noun : temp.keySet()) {
          int target = temp.get(noun);
          int i=0;
          int opponent = freqListOrdered.size() > 0 ? temp.get(freqListOrdered.get(i)) : 0;
          while (i < freqListOrdered.size() && opponent > target) {
            if (opponent <= target) {
              freqListOrdered.add(i, noun);
            }
            opponent = temp.get(freqListOrdered.get(++i));
          }
          freqListOrdered.add(i, noun);
        }
        
        int limit = freqListOrdered.size() > 5 ? 5 : freqListOrdered.size();
        for (int i=0; i<limit; i++) {
          if (i != 0) {
            System.out.print(", ");
          }
          System.out.print(freqListOrdered.get(i));
        }
        System.out.println();
      }
    } catch (Exception e) { e.printStackTrace(); }
  }
}

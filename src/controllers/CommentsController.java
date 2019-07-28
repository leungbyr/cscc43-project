package controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

public class CommentsController {

  private SQLController sqlMngr = null;
  private Connection conn = null;
  private Statement st = null;
  
  public CommentsController() {
    this.sqlMngr = SQLController.getInstance();
    this.conn = sqlMngr.conn;
    this.st = sqlMngr.st;
  }

  public void renterComment(String hostUsername, String lat, String lon, Date date, int rating, String comment) {
    java.sql.Date sqlDate = new java.sql.Date(date.getTime());
    String commentsSql = "INSERT INTO Comments(date, text, rating) VALUES (CURDATE(), ?, ?);";
    String postedOnSql = "INSERT INTO Posted_on_profile(id, sin) SELECT LAST_INSERT_ID(), sin FROM Has_rented WHERE lat = ? AND lon = ? AND date = ?;";
    String postedBySql = "INSERT INTO Posted_by(id, sin) VALUES (LAST_INSERT_ID(), ?);";
    
    try {
      PreparedStatement preparedStmt = conn.prepareStatement(commentsSql);
      preparedStmt.setString(1, comment);
      preparedStmt.setInt(2, rating);
      preparedStmt.execute();
      preparedStmt = conn.prepareStatement(postedOnSql);
      preparedStmt.setString(1, lat);
      preparedStmt.setString(2, lon);
      preparedStmt.setDate(3, sqlDate);
      preparedStmt.execute();
      preparedStmt = conn.prepareStatement(postedBySql);
      preparedStmt.setString(1, this.getSin(hostUsername));
      preparedStmt.execute();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
  
  public void hostComment(String renterUsername, String lat, String lon, Date date, int rating, String comment) {
    String commentsSql = "INSERT INTO Comments(date, text, rating) VALUES (CURDATE(), ?, ?);";
    String postedOnSql = "INSERT INTO Posted_on_profile(id, sin) SELECT LAST_INSERT_ID(), sin FROM Hosted_by WHERE lat = ? AND lon = ?;";
    String postedBySql = "INSERT INTO Posted_by(id, sin) VALUES (LAST_INSERT_ID(), ?);";
    
    try {
      PreparedStatement preparedStmt = conn.prepareStatement(commentsSql);
      preparedStmt.setString(1, comment);
      preparedStmt.setInt(2, rating);
      preparedStmt.execute();
      preparedStmt = conn.prepareStatement(postedOnSql);
      preparedStmt.setString(1, lat);
      preparedStmt.setString(2, lon);
      preparedStmt.execute();
      preparedStmt = conn.prepareStatement(postedBySql);
      preparedStmt.setString(1, this.getSin(renterUsername));
      preparedStmt.execute();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
  
  public void listingComment(String renterUsername, String lat, String lon, Date date, int rating, String comment) {
    String commentsSql = "INSERT INTO Comments(date, text, rating) VALUES (CURDATE(), ?, ?);";
    String postedOnSql = String.format("INSERT INTO Posted_on_listing(id, lat, lon) VALUES (LAST_INSERT_ID(), ?, ?)", lat, lon);
    String postedBySql = "INSERT INTO Posted_by(id, sin) VALUES (LAST_INSERT_ID(), ?);";
    
    try {
      PreparedStatement preparedStmt = conn.prepareStatement(commentsSql);
      preparedStmt.setString(1, comment);
      preparedStmt.setInt(2, rating);
      preparedStmt.execute();
      preparedStmt = conn.prepareStatement(postedOnSql);
      preparedStmt.setString(1, lat);
      preparedStmt.setString(2, lon);
      preparedStmt.execute();
      preparedStmt = conn.prepareStatement(postedBySql);
      preparedStmt.setString(1, this.getSin(renterUsername));
      preparedStmt.execute();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public ResultSet getHostComments(String lat, String lon, Date date) {
    ResultSet rs = null;
    String sql = String.format("SELECT text, rating FROM Comments INNER JOIN Posted_on_profile ON Comments.id = Posted_on_profile.id WHERE sin = (SELECT sin FROM Hosted_by WHERE lat = %s AND lon = %s);", 
        lat, lon);
    
    try {
      rs = this.st.executeQuery(sql);
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

  public ResultSet getComments(String username) {
    ResultSet rs = null;
    String sql = String.format("SELECT text, rating FROM Comments INNER JOIN Posted_on_profile ON Comments.id = Posted_on_profile.id WHERE sin = '%s'", this.getSin(username));
    
    try {
      rs = this.st.executeQuery(sql);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    
    return rs;
  }

  public ResultSet getListingComments(String lat, String lon) {
    ResultSet rs = null;
    String sql = String.format("SELECT text, rating FROM Comments INNER JOIN Posted_on_listing ON Comments.id = Posted_on_listing.id WHERE lat = %s AND lon = %s;", 
        lat, lon);
    
    try {
      rs = this.st.executeQuery(sql);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    
    return rs;
  }

}


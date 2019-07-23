package controllers;

import java.sql.Connection;
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
    // TODO Auto-generated method stub
    
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

}

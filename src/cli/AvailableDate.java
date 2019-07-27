package cli;

import java.math.BigDecimal;
import java.util.Date;

public class AvailableDate {
  Date date;
  BigDecimal price;
  
  public AvailableDate(Date date, BigDecimal price) {
    this.date = date;
    this.price = price;
  }

  public Date getDate() {
    return this.date;
  }

  public double getPrice() {
    return this.price.doubleValue();
  }
}

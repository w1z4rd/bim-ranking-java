package org.costa;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public final class Result {
  private static final NumberFormat POSITION_FORMAT = new DecimalFormat("000");
  private static final NumberFormat BIB_FORMAT = new DecimalFormat("0000");
  private final String race;
  private final int generalPosition;
  private final int bib;
  private final String fullName;
  private final String nationality;
  private final String category;
  private final int categoryPosition;
  private final String officialTime;
  private final String netTime;

  public Result(final String race, final int generalPosition, final int bib,
      final String fullName, final String nationality, final String category,
      final int categoryPosition, final String officialTime,
      final String netTime) {
    this.race = race;
    this.generalPosition = generalPosition;
    this.bib = bib;
    this.fullName = fullName;
    this.nationality = nationality;
    this.category = category;
    this.categoryPosition = categoryPosition;
    this.officialTime = officialTime;
    this.netTime = netTime;
  }

  public String getRace() {
    return race;
  }
  public int getGeneralPosition() {
    return generalPosition;
  }
  public int getBib() {
    return bib;
  }
  public String getFullName() {
    return fullName;
  }
  public String getNationality() {
    return nationality;
  }
  public String getCategory() {
    return category;
  }
  public int getCategoryPosition() {
    return categoryPosition;
  }
  public String getOfficialTime() {
    return officialTime;
  }
  public String getNetTime() {
    return netTime;
  }

  @Override
  public String toString() {
    return "[race=" + race + ", genPos="
        + POSITION_FORMAT.format(generalPosition) + ", bib="
        + BIB_FORMAT.format(bib) + ", name=" + fullName + ", nat=" + nationality
        + ", cat=" + category + ", catPos="
        + POSITION_FORMAT.format(categoryPosition) + ", offTime=" + officialTime
        + ", netTime=" + netTime + "]";
  }
}

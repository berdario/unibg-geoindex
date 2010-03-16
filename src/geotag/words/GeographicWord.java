/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag.words;

import java.util.Date;

/**
 * Classe che estende la classe padre Word e rappresenta le zone geografiche
 * reperite nei documenti. Esse sono caratterizzate da nome, latitudine, longitudine, ecc.
 * @author Giorgio Ghisalberti
 */
public class GeographicWord extends Word {
    private String asciiName = null;
    private float latitude = 0;
    private float longitude = 0;
    private String featureClass = null;
    private String featureCode = null;
    private String countryCode = null;
    private String cc2 = null;
    private String admin1Code = null;
    private String admin2Code = null;
    private String admin3Code = null;
    private String admin4Code = null;
    private int population = 0;
    private int elevation = 0;
    private int gtopo30 = 0;
    private String timeZone = null;
    private Date modificationDate = null;
    private boolean multi = false;
    private double geoScore = 0;
    private boolean adminZone = false;
    private String zoneDocName = null;
    private boolean location = false;
    private boolean maxPop = false;
    private boolean multiLow = false;
    
    //nuovi elementi
    private	double mbr_x1;
    private	double mbr_y1;
    private	double mbr_x2;
    private	double mbr_y2;
        
    public GeographicWord() {
    }
    
    public double getmbr_x1() {
        return mbr_x1;
    }
    public double getmbr_y1() {
        return mbr_y1;
    }
    public double getmbr_x2() {
        return mbr_x2;
    }
    public double getmbr_y2() {
        return mbr_y2;
    }
    public void setmbr_y2(double mbr_y2) {
        this.mbr_y2=mbr_y2;
    }
    public void setmbr_x2(double mbr_x2) {
        this.mbr_x2=mbr_x2;
    }
    public void setmbr_x1(double mbr_x1) {
        this.mbr_x1=mbr_x1;
    }
    public void setmbr_y1(double mbr_y1) {
        this.mbr_y1=mbr_y1;
    }
    
    public String getAdmin1Code() {
        return admin1Code;
    }

    public void setAdmin1Code(String admin1Code) {
        this.admin1Code = admin1Code;
    }

    public String getAdmin2Code() {
        return admin2Code;
    }

    public void setAdmin2Code(String admin2Code) {
        this.admin2Code = admin2Code;
    }

    public String getAdmin3Code() {
        return admin3Code;
    }

    public void setAdmin3Code(String admin3Code) {
        this.admin3Code = admin3Code;
    }

    public String getAdmin4Code() {
        return admin4Code;
    }

    public void setAdmin4Code(String admin4Code) {
        this.admin4Code = admin4Code;
    }

    public String getAsciiName() {
        return asciiName;
    }

    public void setAsciiName(String asciiName) {
        this.asciiName = asciiName;
    }

    public String getCc2() {
        return cc2;
    }

    public void setCc2(String cc2) {
        this.cc2 = cc2;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public int getElevation() {
        return elevation;
    }

    public void setElevation(int elevation) {
        this.elevation = elevation;
    }

    public String getFeatureClass() {
        return featureClass;
    }

    public void setFeatureClass(String featureClass) {
        this.featureClass = featureClass;
    }

    public String getFeatureCode() {
        return featureCode;
    }

    public void setFeatureCode(String featureCode) {
        this.featureCode = featureCode;
    }

    public int getGtopo30() {
        return gtopo30;
    }

    public void setGtopo30(int gtopo30) {
        this.gtopo30 = gtopo30;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public Date getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(Date modificationDate) {
        this.modificationDate = modificationDate;
    }

    public int getPopulation() {
        return population;
    }

    public void setPopulation(int population) {
        this.population = population;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public double getGeoScore() {
        return geoScore;
    }

    public void setGeoScore(double geoWeight) {
        this.geoScore = geoWeight;
    }

    public boolean isMulti() {
        return multi;
    }

    public void setMulti(boolean multi) {
        this.multi = multi;
    }

    public boolean isAdminZone() {
        return adminZone;
    }

    public void setAdminZone(boolean adminZone) {
        this.adminZone = adminZone;
    }

    public String getZoneDocName() {
        return zoneDocName;
    }

    public void setZoneDocName(String zoneDocName) {
        this.zoneDocName = zoneDocName;
    }

    public double getImportance() {
        return importance;
    }

    public boolean isLocation() {
        return location;
    }

    public void setLocation(boolean location) {
        this.location = location;
    }

    public boolean isMaxPop() {
        return maxPop;
    }

    public void setMaxPop(boolean maxPop) {
        this.maxPop = maxPop;
    }

    public boolean isMultiLow() {
        return multiLow;
    }

    public void setMultiLow(boolean multiLow) {
        this.multiLow = multiLow;
    }

    
    
    

}

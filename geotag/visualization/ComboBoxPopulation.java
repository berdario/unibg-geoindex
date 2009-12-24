/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag.visualization;

import geotag.DatabaseGazetteerConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;
import javax.swing.JComboBox;

/**
 * Classe che realizza gerstisce le operazioni relative alle combo box per l'inserimento
 * delle località geografiche.
 * @author Giorgio Ghisalberti
 */
public class ComboBoxPopulation {
    
    /**
     * Metodo che accede al DataBase, reperisce i campi desiderati e popola la combo box
     * @param countryComboBox
     * @param geoLocationComboBox
     * @param dbConnection
     * @throws java.sql.SQLException
     */
    void pop(JComboBox countryComboBox, JComboBox geoLocationComboBox, DatabaseGazetteerConnection dbConnection) throws SQLException {
        Statement stmt = dbConnection.getStatement();
        
        String country = (String) countryComboBox.getSelectedItem();
        if(!country.equals("all countries")){                          
            Vector<String> zones = new Vector<String>();
            String countryCode = selectCountryCode(country, stmt);
            zones = selectItems("geoname", countryCode, stmt);

            for (int i = 0; i < zones.size(); i++) {
                geoLocationComboBox.addItem(zones.elementAt(i));
            }
            geoLocationComboBox.setEnabled(true);
        }
        
        stmt.close();
    }
    

    private Vector<String> selectItems(String table, String countryCode, Statement stmt) throws SQLException{
        Vector<String> countries = new Vector<String>();
        String query = "";
        
        if(table.equals("countryinfo"))
            query = "SELECT name FROM " + table;               
        else if(table.equals("geoname")){
            query = "SELECT name FROM " + table + "WHERE countrycode='" + countryCode + "'";
        }
        
        ResultSet result = stmt.executeQuery(query);

        while (result.next()) {
            countries.add(result.getString(1));
        }
        
        return countries; 
    }
    
    private String selectCountryCode(String country, Statement stmt) throws SQLException{
        String countryCode = "";

        ResultSet result = stmt.executeQuery("SELECT iso_alpha_2 FROM countryinfo" +
                "WHERE name='" + country + "'");

        while (result.next()) {
            countryCode = result.getString(1);
        }

        return countryCode; 
    }

}

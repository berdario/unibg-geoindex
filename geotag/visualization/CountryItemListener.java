/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag.visualization;

import geotag.DatabaseGazetteerConnection;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComboBox;

/**
 * Classe che estende ItemListener ed ha il compito di reperire la richiesta utente
 * ed in base ad essa aggiornare alcuni valori relativi alle Nazioni visualizzate sull'interfaccia.
 * @author Giorgio Ghisalberti
 */
public class CountryItemListener implements ItemListener {
    JComboBox countryComboBox = null;
    JComboBox locationComboBox = null;
    DatabaseGazetteerConnection dbConnection = null;

    public CountryItemListener() {

    }

    public CountryItemListener(JComboBox countryComboBox, JComboBox locationComboBox, DatabaseGazetteerConnection dbConnection) throws SQLException {
        this.countryComboBox = countryComboBox;
        this.locationComboBox = locationComboBox;
        this.dbConnection = dbConnection;
    }

    public void itemStateChanged(ItemEvent evt) {
        JComboBox cb = (JComboBox)evt.getSource();

        // Get the affected item
        Object item = evt.getItem();

        if (evt.getStateChange() == ItemEvent.SELECTED) {
            String country = (String) countryComboBox.getSelectedItem();
            
            if (!country.equals("select a country")) {
                try {
                    Statement stmt = dbConnection.getStatement();
                    Vector<String> zones = new Vector<String>();
                    
                    String countryCode = selectCountryCode(country, stmt);
                    zones = selectItems("geoname", countryCode, stmt);

                    //Rimuovo gli items...
                    locationComboBox.removeAllItems();
                    
                    //Ordino alfabeticamente
                    Collections.sort(zones);

                    //Popolo di nuovo la combobox...
                    for (int i = 0; i < zones.size(); i++) {
                        if(!zones.elementAt(i).isEmpty())
                            locationComboBox.addItem(zones.elementAt(i));
                    }
                    locationComboBox.setEnabled(true);

                } catch (SQLException ex) {
                    Logger.getLogger(CountryItemListener.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else if (evt.getStateChange() == ItemEvent.DESELECTED) {
            // Item is no longer selected

        }
    }

    /**
     * Metodo che accede al DataBase per prelevare i countryCode della
     * nazione selezionata
     * @param country
     * @param stmt
     * @return
     * @throws java.sql.SQLException
     */
    private String selectCountryCode(String country, Statement stmt) throws SQLException{
        String countryCode = "";

        ResultSet result = stmt.executeQuery("SELECT iso_alpha_2 FROM countryinfo" +
                " WHERE name='" + country + "'");

        while (result.next()) {
            countryCode = result.getString(1);
        }

        return countryCode; 
    }
        
        

    /**
     * Metodo che accede al DataBase per reperire tutte le zone della Nazione
     * avente il country code ricevuto in input
     * @param table
     * @param countryCode
     * @param stmt
     * @return
     * @throws java.sql.SQLException
     */            
    private Vector<String> selectItems(String table, String countryCode, Statement stmt) throws SQLException{
        Vector<String> countries = new Vector<String>();
        String query = "";
        
        if(table.equals("countryinfo"))
            query = "SELECT name FROM " + table;
        else if(table.equals("admin1codesascii")){
            query = "SELECT nameascii FROM " + table + " WHERE code LIKE '" + countryCode + "%'";
        }
        else if(table.equals("geoname")){
            query = "SELECT asciiname FROM " + table + " WHERE countrycode='" + countryCode + "'";
        }
        
        ResultSet result = stmt.executeQuery(query);

        while (result.next()) {
            countries.add(result.getString(1));
        }
        
        return countries; 
    }


        
        
    }



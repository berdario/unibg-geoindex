/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag.visualization;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Vector;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import geotag.words.GeoRefDoc;
import geotag.words.GeographicWord;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author giorgio
 */
public class GeoWordsTable {
    NumberFormat formatter = new DecimalFormat("#0.00");  //Formatto il numero a 2 cifre decimali
    
    public GeoWordsTable(JTable table, Vector<GeoRefDoc> results, String docName){
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        Vector<String> row = null;
        
        table.setColumnSelectionAllowed(true);
        table.setRowSelectionAllowed(true);
        
        // Si eliminano dalla tabella i risultati precendenti
        if (table.getRowCount() > 0) {
            for(int i = model.getRowCount() - 1; i >= 0; i--) {
                model.removeRow(i);
            }
        }

        
        for(int i = 0; i < results.size(); i++){
            GeoRefDoc doc = results.elementAt(i);
            if(doc.getNomeDoc().equalsIgnoreCase(docName)){                
                if(doc.getScores() != null){
                    HashMap<GeographicWord, Double> hash = doc.getScores();
                    Iterator keyList = hash.keySet().iterator();
                    while(keyList.hasNext()){
                        GeographicWord gw = (GeographicWord) keyList.next();
                        row = new Vector<String>();

                        row.add(gw.getName());
                        row.add(formatter.format(gw.getGeoScore()));
                        row.add(formatter.format(doc.getScores().get(gw)));
                        row.add(gw.getCountryCode());
                        row.add(gw.getAdmin1Code());
                        row.add(gw.getAdmin2Code());
                        row.add(formatter.format(gw.getLatitude()));
                        row.add(formatter.format(gw.getLongitude()));
                        row.add(Integer.toString(gw.getPopulation()));
                        row.add(Integer.toString(gw.getElevation()));
                        

                        ((DefaultTableModel)table.getModel()).addRow(row);
                    }
                }
            }
                
        }
        
        
        
    }
    
    

}

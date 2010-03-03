/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag.visualization;

import geotag.GeoApplication;
import java.text.DecimalFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Vector;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import geotag.words.GeoRefDoc;

/**
 *
 * @author giorgio
 */
public class ResultsTable {
    NumberFormat formatter = new DecimalFormat("#0.00");  //Formatto il numero a 2 cifre decimali
    
    
    /**
     * Costruttore della classe
     */
    public ResultsTable(){
        
    }
    
    /**
     * Metodo che crea la lista dei risutlati della ricerca
     * @param table
     * @param results
     * @param constLocationImportance
     */
    public void createTable(JTable table, Vector<GeoRefDoc> results, int constLocationImportance){     
        //Creo il ranking dei documenti in base alla costante
        results = GeoApplication.createRanking(results, constLocationImportance*0.01);
        
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        Vector<String> row = null;
        
        // Si eliminano dalla tabella i risultati precendenti
        if (table.getRowCount() > 0) {
            for(int i = model.getRowCount() - 1; i >= 0; i--) {
                model.removeRow(i);
            }
        }
        
        // Popolo la tabella
        for(int i = 0; i < results.size(); i++){
            GeoRefDoc doc = results.elementAt(i);
            row = new Vector<String>();
            String name = doc.id;
            String textScore = formatter.format(doc.getTextScore());
            String distanceScore = formatter.format(doc.getDistanceScore());
            
            row.add(Integer.toString(i+1));
            row.add(name);
            row.add(textScore);
            row.add(distanceScore);
                
            
            ((DefaultTableModel)table.getModel()).addRow(row);
        }   
    }
}

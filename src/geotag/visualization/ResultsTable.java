/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag.visualization;

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
        results = createRanking(results, constLocationImportance);
        
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
            String name = doc.getNomeDoc();
            String textScore = formatter.format(doc.getTextScore());
            String distanceScore = formatter.format(doc.getDistanceScore());
            
            row.add(Integer.toString(i+1));
            row.add(name);
            row.add(textScore);
            row.add(distanceScore);
                
            
            ((DefaultTableModel)table.getModel()).addRow(row);
        }
        
        
    }

    /**
     * Metodo che ordina il vettore dei documenti reperiti rispetto al campo "sortScore"
     * @param results : vettore dei docuemnti
     * @param locationImportance : costante che indica l'importanza della "query" e dela "location"
     * @return il vettore dei documenti ordinato
     */
    public Vector<GeoRefDoc> createRanking(Vector<GeoRefDoc> results, int locationImportance) {
        double queryImp = 1.0;
        double locationImp = 1.0;
        double sortScore = 0.0;
        Vector<GeoRefDoc> newResults = new Vector<GeoRefDoc>();
        
        /*
        // Calcolo i parametri di importanza della query e della location
        if(locationImportance > 50){
            locationImp = locationImp + locationImportance*0.01;
            queryImp = (double) queryImp - locationImportance*0.01;
        }else if(locationImportance < 50){
            queryImp = queryImp + (1-locationImportance)*0.01;
            locationImp = locationImportance*0.01;
        }
        */
        
        double beta = locationImportance * 0.01;
        
        //Calcolo il sortScore
        for(int i = 0; i < results.size(); i++){
            GeoRefDoc doc = results.elementAt(i);
            
            /*if(queryImp == 0)
                sortScore = doc.getDistanceScore() * locationImp;
            else if(locationImp == 0)
                sortScore = doc.getTextScore() * queryImp;
            else
                sortScore = doc.getTextScore()*queryImp * doc.getDistanceScore()*locationImp;
            */
            
            sortScore = beta * doc.getDistanceScore() + (1 - beta) * doc.getTextScore() ;            
            doc.setSortScore(sortScore);
        }
        
        //Creo il ranking dei documenti in base al campo "sortScore"
        while(!results.isEmpty()){
            GeoRefDoc newDoc = findMax(results);
            newResults.add(newDoc);
            
            Vector<GeoRefDoc> oldResults = new Vector<GeoRefDoc>();
            for(int i = 0; i < results.size(); i++){
                GeoRefDoc doc = results.elementAt(i);
                if(doc != newDoc)                
                    oldResults.add(doc);
            }
            results = oldResults;            
        }
   
        return newResults;
    }

    /**
     * Trovo il documento che ha "sortScore" massimo
     * @param results : Vettore dei documenti reperiti
     * @return il documento con "sortScore" massimo
     */
    private GeoRefDoc findMax(Vector<GeoRefDoc> results) {
        double max = 0.0;
        GeoRefDoc grd = new GeoRefDoc();        
        Vector<GeoRefDoc> oldResults = new Vector<GeoRefDoc>();
                
        //Trovo massimo
        for(int i = 0; i < results.size(); i++){
            GeoRefDoc doc = results.elementAt(i);
            if(doc.getSortScore() >= max){
                max = doc.getSortScore();
                grd = doc;
            }
        }
        
        return grd;
    }
    
    

}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag;

import java.io.File;

/**
 * Classe che stabilisce quali sono i formati di file testuali accettati dall'applicazione.
 * @author Giorgio Ghisalberti
 */
class MyFileExtensionFilter extends javax.swing.filechooser.FileFilter  {

    public MyFileExtensionFilter() {
    }

    /**
     * Metodo che determina se il formato di file in ingrasso è accettabile o meno
     * @param formato del file in ingresso.
     * @return TRUE se file accettato, FALSE in caso contrario
     */
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        else if (f.getName().toLowerCase().endsWith(".pdf")) {
            return true;
        }
        else if (f.getName().toLowerCase().endsWith(".html") || f.getName().toLowerCase().endsWith(".htm")) {
            return true;
        }
        else if (f.getName().toLowerCase().endsWith(".xml")) {
            return true;
        }
        return false;
    }

    public String getDescription() {
        return "PDF, HTML or XML files"; //"PDF or HTML files"
    }
    
    

}

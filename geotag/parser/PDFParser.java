/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag.parser;

import java.io.IOException;
import org.pdfbox.exceptions.CryptographyException;
import org.pdfbox.exceptions.InvalidPasswordException;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.util.PDFTextStripper;

/**
 * Classe che esegue il parsing del file in formato PDF.
 * Per eseguire questa operazione mi affido alla libreria PDFBox.
 * @author Giorgio GHisalberti
 */
public class PDFParser {
    PDDocument document = null;
    PDFTextStripper stripper = null;
                
    /**
     * Costruttore della classe
     */
    public PDFParser(){
        
    }
    
    /**
     * Metodo responsabile del'esecuzione del parsing del file PDF.
     * Per decriptare i documenti ho usate le librerie "bcprov" e "bcmail"
     * @param docName : nome del file da cui leggere il contenuto
     * @return la stringa di testo reperita
     * @throws java.io.IOException
     * @throws org.pdfbox.exceptions.CryptographyException
     * @throws org.pdfbox.exceptions.InvalidPasswordException
     */
    public String parsinfPDFFile(String docName) throws IOException, CryptographyException, InvalidPasswordException{
        String docContent = "";
      
        try {
            document = PDDocument.load(docName);

            // Decripto il documento
            if( document.isEncrypted() ){
                try {
                    document.decrypt( "-password" );
                }
                catch (CryptographyException ex) {
                //System.err.println( "Error: The document is encrypted." ); 
                }catch( InvalidPasswordException e ){
                    System.err.println( "Error: invalid password." );                        
                }
            }                        

            // Lettura del contenuto del documento
            stripper = new PDFTextStripper();
            docContent = stripper.getText(document);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                    document.close();
            } catch (IOException ex) {
                    ex.printStackTrace();
            }
        }
        
        
        return docContent;
    }
    
    
    
    

}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.exceptions.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.util.PDFTextStripper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Classe che esegue il parsing del file in formato PDF.
 * Per eseguire questa operazione mi affido alla libreria PDFBox.
 * @author Giorgio Ghisalberti, Dario Bertini
 */
public class PDFParser {
    String docContent;
    String pdfTitle;
                
    /**
     * Costruttore della classe
     *
     * Metodo responsabile del'esecuzione del parsing del file PDF.
     * Per decriptare i documenti ho usate le librerie "bcprov" e "bcmail"
     * //ndDario: gli import sono assenti, e quindi anche la funzionalità è assente
     * //quindi ora bcprov e bcmail sono stati rimossi dalle librerie usate
     * @param docName : nome del file da cui leggere il contenuto
     * @throws java.io.IOException
     * @throws org.pdfbox.exceptions.CryptographyException
     * @throws org.pdfbox.exceptions.InvalidPasswordException
     */
    public PDFParser(String docName) throws IOException, CryptographyException, InvalidPasswordException{
        PDDocument document = null;
        PDFTextStripper stripper = null;

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
            
            PDDocumentCatalog catalog = document.getDocumentCatalog();
            PDMetadata metadata = catalog.getMetadata();

            // Lettura del contenuto del documento
            stripper = new PDFTextStripper();
            docContent = stripper.getText(document);

            if (metadata != null){//TODO non tutti i documenti hanno dei metadata xmp, ma possono avere lo stesso un titolo... indagare dove si trova ed eventualmente implementare
                pdfTitle=parseTitle(metadata.createInputStream());
            } else { //per ora, se non troviamo metadati, settiamo come titolo la prima riga del pdf
                pdfTitle=docContent.split("\n",2)[0];
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                    document.close();
            } catch (IOException ex) {
                    ex.printStackTrace();
            }
        }
    }
    
    /**
    */
    public String getContent(){
        return docContent;
    }

    public String getTitle(){
        return pdfTitle;
    }

    String parseTitle(InputStream xmlstream){
        String title=null;
        try {
            
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document metadata = docBuilder.parse(xmlstream);
            metadata.getDocumentElement().normalize();
            Element titleElement = (Element) metadata.getDocumentElement().getElementsByTagName("dc:title").item(0);
            if (titleElement == null){
                return null;
            }
            title = titleElement.getElementsByTagName("rdf:li").item(0).getFirstChild().getNodeValue();//TODO, sistemare per bene... occhio ai controlli
            title = title.replaceAll("\n", " ").trim();
            
        } catch (SAXException ex) {
            Logger.getLogger(PDFParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PDFParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(PDFParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        return title;
    }

}

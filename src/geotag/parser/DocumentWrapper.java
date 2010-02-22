package geotag.parser;

import java.io.File;
import java.io.IOException;
import org.jdom.JDOMException;
import org.pdfbox.exceptions.CryptographyException;
import org.pdfbox.exceptions.InvalidPasswordException;

/**
 *
 * @author Giorgio Ghisalberti, Dario Bertini
 */
public class DocumentWrapper {
    //Campi dei documenti in esame (HTML e XML)

    public String content;
    public String title = "";
    public String description = "";
    public String dateline = "";
    public String keywords = "";

    /**
     * Costruttore
     * Metodo per la gestione dell'apertura dei file da indicizzare.
     * Sono accettati 3 formati: PDF, HTML e XML
     * @param documentName : nome del documento
     * @param documentExtension : estensione del file
     * @param fileName : nome completo del file
     * @return il contenuto del documento
     * @throws ParserException
     */
    public DocumentWrapper(File fileName, String documentExtension) throws UnsupportedFileException {
        String content = "";
        if (documentExtension.equalsIgnoreCase("pdf")) {
            openPDFDocument(fileName.toString());
        } else if (documentExtension.equalsIgnoreCase("html") || documentExtension.equalsIgnoreCase("htm")) {
            openHTMLDocument(fileName.toString());
        } else if (documentExtension.equalsIgnoreCase("xml")) {
            openXMLDocument(fileName);
        } else {
            throw new UnsupportedFileException();
        }
    }

    /**
     * Apertura del file in formato PDF .
     * Per eseguire questa operazione mi sono affidato alle librerie PDFBox e FontBox
     * @param documentName : nome del documento
     * @return il contenuto del file PDF
     */
    void openPDFDocument(String documentName) {
        try {
            PDFParser pdfFileParser = new PDFParser(documentName);
            content = pdfFileParser.getContent();
            title = pdfFileParser.getTitle();

        } catch (IOException ex) {
            ex.printStackTrace();
            //errortext+="Error onening PDF file\n";//era un errorlabel
        } catch (CryptographyException ex) {
            ex.printStackTrace();
            //statusMessageLabel.setText("Error");
        } catch (InvalidPasswordException ex) {
            ex.printStackTrace();
            //errortext+="Error: password invalid\n";//era un errorlabel
        }
    }

    /**
     * Apertura del documento il formato HTML:
     * Estraggo il testo dal documento contenuto all'interno del tag <body>,
     * escludendo i LINK perché potrebbero essere fuorvianti.
     * In particolare memorizzo i campi "title", "description" e "keywords" per i quali, se
     * conterranno un elemento geografico, dovrò aumentare il peso di rilevanza.
     * Per eseguire queste operazioni mi sono affidato alle librerie: HTMLParser, Jericho-HTML
     * @param docURL : indirizzo del documento
     * @return il contenuto del file HTML
     * @throws org.htmlparser.util.ParserException
     */
    void openHTMLDocument(String docURL) {

        try {
            HTMLParser htmlFileParser = new HTMLParser(docURL);
            content = htmlFileParser.getContent();

            //Document title
            title = htmlFileParser.getTitle();

            //Document description
            description = htmlFileParser.getMetaValue("description");

            //Document keywords
            keywords = htmlFileParser.getMetaValue("keywords");

        } catch (IOException ex) {
            ex.printStackTrace();
            //errortext+="Error opening HTML file\n";//era un errorlabel
        }
    }

    /**
     * Apertura del file nel formato XML.
     * Per eseguire questa operazione mi sono affidato alla libreria SAX e JDOM
     * @param fileName : nome del file
     * @return il contenuto del file
     */
    void openXMLDocument(File fileName) {



        try {
            XMLParser xmlFileParser = new XMLParser(fileName);

            content = xmlFileParser.parsingXMLfile("");

            //Document title
            title = xmlFileParser.parsingXMLfile("title");
            String t = title;

            //Document dateline
            dateline = xmlFileParser.parsingXMLfile("dateline");
            String d = dateline;
            String a = d;
        } catch (JDOMException ex) {
            ex.printStackTrace();
            //errortext+="Error parsing XML file\n";//erano errorlabel
        } catch (IOException ex) {
            ex.printStackTrace();
            //errortext+="Error opening XML file\n";
        }


    }

    public class UnsupportedFileException extends Exception {
    }
}

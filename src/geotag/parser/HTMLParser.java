/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag.parser;

import au.id.jericho.lib.html.CharacterReference;
import au.id.jericho.lib.html.Element;
import au.id.jericho.lib.html.HTMLElementName;
import au.id.jericho.lib.html.Source;
import au.id.jericho.lib.html.StartTag;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

/**
 * Classe che esegue il parsing del file in formato HTML.
 * In particolare è possibile estrarre dal file il contentuto vero e proprio, 
 * ovvero il testo all'interno dei tag "body" oppure anche altri campi:
 * "titolo", "descrizione" e "keywords".
 * Per svolgere queste operazioni mi affido alla  libreria HTMLParser.
 * @author Giorgio Ghisalberti
 */
public class HTMLParser {
    String body, title = null;
    HashMap<String,String> meta = new HashMap<String,String>();

    /**
     * Costruttore della classe
     */
    public HTMLParser(String docURL) throws MalformedURLException, IOException{
        if (docURL.indexOf(':') == -1) {
            docURL = "file:" + docURL;
        }

       Source source = new Source(new URL(docURL));

       // Call fullSequentialParse manually as most of the source will be parsed.
       source.fullSequentialParse();

       //Tutto il testo del documento, escluso quello dentro SCRIPT and STYLE
       body = source.getTextExtractor().setIncludeAttributes(true).toString();

       Element titleElement = source.findNextElement(0,HTMLElementName.TITLE);
       if (titleElement != null){// TITLE element never contains other tags so just decode it collapsing whitespace:
           title = CharacterReference.decodeCollapseWhiteSpace(titleElement.getContent());
       }

       String[] interestingKeys = {"description","keywords"};

        for (String key : interestingKeys) {
            for (int pos = 0; pos < source.length();) {
                StartTag startTag = source.findNextStartTag(pos, "name", key, false);
                if (startTag == null){
                    break;
                }
                if (startTag.getName().equals(HTMLElementName.META)) {
                    meta.put(key, startTag.getAttributeValue("content")); // Attribute values are automatically decoded
                    break;
                }
                pos = startTag.getEnd();
            }
        }
    }
    
    /**
     * Metodo che legge il contenuto del campo "body" del file HTML e ne restituisce il risultato
     * @return la stringa di testo reperita
     */
    public String getContent() {
        return body;
    }
    
    
    /**
     * Legge il contenuto del campo "title" del doc HTML
     * @return la stringa di testo reperita
     */
    public String getTitle() {        
        return title;
    }
    
    
    /**
     * Legge il contenuto del METATAG del doc HTML specificato dal parametro in ingrasso
     * @param key : nome del metatags da analizzare
     * @return la stringa di testo reperita
     */
    public String getMetaValue(String key) {
        return meta.get(key);
    }
    
    
}

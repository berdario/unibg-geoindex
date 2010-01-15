/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag.parser;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * Classe che esegue il parsing del documento in formato XML.
 * Viene estratto il contenuto del file presente all'interno dei tag "text",
 * "title" e "dateline".
 * @author Giorgio Ghisalberti
 */
public class XMLParser {
    SAXBuilder builder = new SAXBuilder();
    Document doc = null;
    Element root = null;
    String title = "";
    String dateline = "";
    
    
    /**
     * Costruttore della classe
     */
    public XMLParser(File fileName) throws JDOMException, IOException{
        doc = builder.build(fileName);
        root = doc.getRootElement();
    }
    
    
    /**
     * Metodo che gestice il parsing del file XML. Viene reperito il testo
     * contenuto all'interno dei tag definiti dal parametro in ingresso
     * @param field : nome del tag di interesse
     * @return la stringa di testo reperita
     */
    public String parsingXMLfile(String field) {
        String result = "";               
        result = getContent(root, 0, "", field);
        
        return result;
    }
    
    
    
    /**
    * Viene reperito il testo contenuto nel file XML, all'interno del campo "body" (o "text"),
     "title", oppure "dateline
    * @param current
    * @param depth
    * @param content
    * @return
    */
    public String getContent(Element current, int depth, String content, String field) {
        List children = current.getChildren();
        Iterator iterator = children.iterator(); 
        String cn = current.getName();
        
        if(cn.equalsIgnoreCase("title"))
            title = current.getContent(0).getValue();
        

        if(cn.equalsIgnoreCase("dateline"))
            dateline = current.getContent(0).getValue();
        

        while (iterator.hasNext()) {
            Element child = (Element) iterator.next();
            if(cn.equalsIgnoreCase("text") || cn.equalsIgnoreCase("body") ){
                String text = child.getText();
                content = content + text + " ";
            }
            String s = child.getText();  
            content = getContent(child, depth+1, content, field);
        }

        if(field.equals("title"))
            return title;
        else if(field.equals("dateline"))
            return dateline;
        
        return content;
    }
    
    

}

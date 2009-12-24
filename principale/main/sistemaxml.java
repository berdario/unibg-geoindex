package principale.main;

import java.io.File;

import java.io.FileOutputStream;
import java.io.FileReader;

import java.io.OutputStreamWriter;

public class sistemaxml {

	//immagino che serva per convertire il charset di osm, da utf16 (apparentemente problematico) a utf8
	public void sistema(String path,String path2) {
		try{
		File fileIn = new File(path);
	    File fileOut = new File(path2);

	    FileReader streamIn = new FileReader(fileIn);
	    
	    OutputStreamWriter streamOut = 
	      new OutputStreamWriter(new FileOutputStream(fileOut), "UTF-8");
	    
	    int carattereLetto;
	    

	    while( (carattereLetto=streamIn.read()) != -1 ) {
	      streamOut.write(carattereLetto);
	    }
	    streamIn.close();
	    streamOut.close();
	    fileIn.delete();
		}
		catch (Exception e) {
			// 
		}
	}

}

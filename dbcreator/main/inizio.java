package dbcreator.main;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;


public class inizio {
	
	public static void main(String[] args) {
		CommandLineParser parser= new PosixParser();
		Options options=new Options();
		options.addOption("g", "gui", false, "starts with the gui");
		options.addOption("i", "interactive", false, "ask confirmation before each step");
		options.addOption("f","force",false, "force the creation of the db, even if it already exists");
		options.addOption("h", "help", false, "print this message");
		HelpFormatter usagehelp = new HelpFormatter();
		try {
			CommandLine cmd=parser.parse(options, args);
			args=cmd.getArgs();
			boolean interactive=cmd.hasOption("interactive");
			
			if (cmd.hasOption("help")){
				StackTraceElement[] stack = Thread.currentThread ().getStackTrace ();
				String programName = stack[stack.length - 1].getFileName();
				String usage=programName+" [configfile] [--interactive] [--force]";
				usagehelp.printHelp(usage, options);
			} else if (cmd.hasOption("gui")){
				menu frame = new menu();
				
		        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
				Dimension frameSize = frame.getSize();
				
		        if (frameSize.height > screenSize.height) {
		            frameSize.height = screenSize.height;
		        }
		        if (frameSize.width > screenSize.width) {
		            frameSize.width = screenSize.width;
		        }
		        frame.setLocation( ( screenSize.width - frameSize.width ) / 2, ( screenSize.height - frameSize.height ) / 2 );
		        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		        frame.setVisible(true);
	        } else if(cmd.hasOption("force") && args.length<2){
	        	Dbcreator creator;//attenzione: saltando la verifica non controlla neanche che esistano i file necessari per la creazione
	        	if (args.length==1){
					creator= new Dbcreator(args[0]);
				} else {
					creator= new Dbcreator(null);
				}
	        	creator.createDB(interactive);
	        }/*else if (args.length==0){
				Dbcreator creator= new Dbcreator();
				creator.verificaFile();
			}*/else if (args.length<2){
				Dbcreator creator;
				if (args.length==1){
					creator= new Dbcreator(args[0]);
				} else {
					creator= new Dbcreator(null);
				}
				if (!creator.verificaFile()){
					creator.createDB(interactive);
				}
			}
		} catch (org.apache.commons.cli.ParseException e) {
			e.printStackTrace();
		}
	}
}

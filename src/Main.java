import hanium.pdfreport.util.Util;
import scouter.plugin.server.hanium.pdfreport.email.PdfEmail;

public class Main {
	
	/* Main.java is just test class for local environment */
	public static void main(String[] args) {
		//Util.CUR_PATH = "C:\\Users\\occid\\workspace\\Hanium-PdfReport\\";
		PdfEmail p = new PdfEmail();
		p.sendEmail();
	}
}

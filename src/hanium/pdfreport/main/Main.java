package hanium.pdfreport.main;

import hanium.pdfreport.pdf.*;
import hanium.pdfreport.email.*;
import hanium.pdfreport.util.*;

public class Main {
	public static void main(String[] args) {
		PdfReport pdfReport = new PdfReport();
		
		boolean createSuccess = pdfReport.createPdfReport();
		boolean sendEmailSuccess = false;
		
		if(createSuccess) {
			String to = "occidere@naver.com, ygh1kr@naver.com, marching0531@naver.com";
			
			Email email = new Email();
			sendEmailSuccess = email.sendEmail(to, Util.REPORT_FILE_PATH);
			
			if(sendEmailSuccess) System.out.println("Email Sent Success!");
			else System.err.println("Email Sent Fail!");
		}
		else {
			System.err.println("PDF Creation Fail!");
		}
	}
}

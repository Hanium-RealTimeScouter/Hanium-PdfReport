package hanium.pdfreport.email;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.MultiPartEmail;

public class Email {
	
	public boolean sendEmail(String to, String attachmentPath) {
		MultiPartEmail email = null;
		EmailAttachment attach = null;

		boolean sendSuccess = true;
		
		try {
			email = new MultiPartEmail();
			
			email.setHostName("smtp.gmail.com");
			email.setSmtpPort(587);
			email.setStartTLSEnabled(true);
	        email.setAuthenticator(new DefaultAuthenticator("haniumscouter@gmail.com", "dkqorhvk!@#$"));
	        email.setFrom("haniumscouter@gmail.com");
	        email.setSubject("Hanium PDF Report");
	        email.setMsg(String.format("[%s] Hanium PDF Report\n", 
	        		new SimpleDateFormat("yyyy-MM-dd").format(new Date())));
	        
	        
	        File attachedFile = new File(attachmentPath);
	        
	        if(attachedFile != null && attachedFile.exists() && attachedFile.isFile()) {
	        	attach = new EmailAttachment();
	        	attach.setDescription("Hanium PDF Report");
	        	attach.setName("");
	        	attach.setPath(attachmentPath);
	        	
	        	email.attach(attach);
	        }
	        
	        to = to.replaceAll(" ", ""); //공백 모두 제거
	        //수신자는 , 를 기준으로 분리
            for (String addr : to.split(",")) {
            	email.addTo(addr);
            }
	        
            /* 메일 전송 */
	        email.send();
		}
		catch(Exception e) {
			e.printStackTrace();
			sendSuccess = false;
		}
		
		return sendSuccess;
		
	}
}

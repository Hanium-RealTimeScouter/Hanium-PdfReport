package scouter.plugin.server.hanium.pdfreport.email;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.MultiPartEmail;

import hanium.pdfreport.pdf.PdfReport;
import hanium.pdfreport.util.Util;
import scouter.lang.conf.ConfigDesc;
import scouter.lang.pack.Pack;
import scouter.lang.plugin.PluginConstants;
import scouter.lang.plugin.annotation.ServerPlugin;
import scouter.server.Configure;
import scouter.server.Logger;

public class PdfEmail {
	
	private final Configure conf = Configure.getInstance();
	
	private long lastSentTime = -1; //초기값
	
	public PdfEmail() {
		Pack pack = null; //method invoke를 위한 의미없는 Pack 강제 사용
		sendEmail(pack);
	}
	
	/**
	 * 이메일 보내는 스레드.</br>
	 * 이것을 호출하면 10초 간격으로 스케쥴링을 하며, 주기에 맞춰 PDF 생성 및 메일 전송을 시도한다.
	 */
	@ServerPlugin(PluginConstants.PLUGIN_SERVER_OBJECT)
	public void sendEmail(Pack pack) { //method invoke를 위한 의미없는 Pack 강제 사용
		
		/* PDF 전송이 false면 종료 */
		if(conf.getBoolean("ext_plugin_email_send_pdf", true)) {
			try {
				
				new Thread() {
					
					@Override
					public void run() {
						
						/* PDF 생성 및 메일 전송 관련
						 * 1일: 86400000
						 * 7일: 604800000
						 * 30일: 2592000000
						 */
						long sendPeriod = conf.getLong("ext_plugin_email_send_pdf_period", 86400000);
						
						long curTime = System.currentTimeMillis();
						long elapsedTime = curTime - sendPeriod;
						
						/* 마지막 전송 시간으로부터 전송 주기만큼 지났으면 pdf 생성 및 전송 */
						if(lastSentTime<0 || elapsedTime >= lastSentTime) {
							

							////////* 데이터 요청 메서드 콜 삽입 부분*//////////
							
							
							PdfReport pdfReport = new PdfReport();
							
							boolean createSuccess = pdfReport.createPdfReport();
							boolean sendEmailSuccess = false;
							
							if(createSuccess) {
								Logger.println("PDF Creation Success!");
								System.out.println("PDF Creation Success!");
								
								//String to = "occidere@naver.com, ygh1kr@naver.com, marching0531@naver.com";
								String to = "occidere@naver.com";
								
								sendEmailSuccess = sendEmail(to, Util.REPORT_FILE_PATH);
								
								if(sendEmailSuccess) {
									Logger.println("Report Email Sent Success!");
									System.out.println("Report Email Sent Success!");
									
									lastSentTime = curTime; //현재 시간으로 마지막 전송 시간을 갱신
									
									//pdfReport.deleteAllData(); //PDF 및 생성에 사용됬던 자료들 모두 삭제
								}
								else Logger.println("Report Email Sent Fail!");
							}
							else {
								Logger.println("PDF Creation Fail!");
								System.out.println("PDF Creation Fail!");
							}
						}
					}
				}.start(); //end of the new Thread()
			}
			catch(Throwable t) {
				t.printStackTrace();
				t.printStackTrace(Logger.pw());
			}
		}
		
	}
	
	/**
	 * 내부적으로 호출해 사용하는 메일 전송 메서드.</br>
	 * @param to 전송 대상으로 여러명일 경우 , 로 구분하여 기입한다.
	 * @param attachmentPath 첨부할 PDF 파일의 경로
	 * @return 전송 성공시 true, 실패시 false
	 */
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
	        
	        /* PDF 첨부파일 있을 경우만 메일 전송 */
	        if(attachedFile != null && attachedFile.exists() && attachedFile.isFile()) {
	        	attach = new EmailAttachment();
	        	attach.setDescription("Hanium PDF Report");
	        	attach.setName("");
	        	attach.setPath(attachmentPath);
	        	
	        	email.attach(attach);
	        	
	        	to = to.replaceAll(" ", ""); //공백 모두 제거
		        //수신자는 , 를 기준으로 분리
	            for (String addr : to.split(",")) {
	            	email.addTo(addr);
	            }
		        
	            /* 메일 전송 */
		        email.send();
	        }
	        else {
	        	sendSuccess = false;
	        	System.err.println("Attachement was not found!");
	        	Logger.println("Attachement was not found");
	        }
		}
		catch(Exception e) {
			e.printStackTrace();
			sendSuccess = false;
			
			e.printStackTrace(Logger.pw());
		}
		
		return sendSuccess;
	}
}


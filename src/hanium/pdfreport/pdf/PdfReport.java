package hanium.pdfreport.pdf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chapter;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;

import hanium.pdfreport.util.*;
import scouter.server.Logger;

import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Image;


public class PdfReport {
	/* 사용할 폰트 */
	private static Font kor10, kor10Red, kor15, kor15Red, kor20, kor20Red;
	
	public static AtomicInteger ai = new AtomicInteger(0);
	
	/**
	 * 생성자에서 폰트 생성 & 데이터 폴더 생성
	 */
	public PdfReport() {
		
		try {
			/* 데이터 폴더 없을 시 자동 생성 */
			new File(Util.DATA_PATH).mkdirs();
			
			/* FONT_PATH로 부터 한글폰트를 적용시킨 뒤 BaseFont를 가져온다 */
			BaseFont bf = BaseFont.createFont(Util.FONT_PATH, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
			kor10 = new Font(bf, 10);
			kor10Red = new Font(bf, 10, Font.BOLD, BaseColor.RED);
			kor15 = new Font(bf, 15);
			kor15Red = new Font(bf, 15, Font.BOLD, BaseColor.RED);
			kor20 = new Font(bf, 20);
			kor20Red = new Font(bf, 20, Font.BOLD, BaseColor.RED);
		}
		catch(Exception e) {
			e.printStackTrace();
			e.printStackTrace(Logger.pw());
		}
	}
	
	/**
	 * 보고서 생성 메서드.
	 * @return 생성 성공시 true, 실패시 false
	 */
	public synchronized boolean createPdfReport() {
		
		boolean isSuccess = true;
		
		try {
			/* 기초적인 PDF 생성 */
			/* /home/haniumPdfReport/[20170807] Scouter Report.pdf */
			FileOutputStream fos = new FileOutputStream(Util.REPORT_FILE_PATH);
			Document document = new Document(PageSize.A4, 30, 30, 30, 30);
			PdfWriter writer = PdfWriter.getInstance(document, fos);
		    document.open();
		    
		    document.addAuthor("author_occidere");
		    document.addCreator("creator_occidere");
		    document.addTitle("Hanium Scouter Project");
		    
		    Paragraph title = new Paragraph("한이음 스카우터 PDF 보고서\n" + Util.REPORT_NAME + "\n", kor20Red);
		    title.setAlignment(Element.ALIGN_CENTER); //가운데정렬
		    document.add(title);

		    
		    /* 이미지 삽입 */
		    Image image = Image.getInstance(Util.DATA_PATH + "scouter-logo-w200.png");
		    image.setAlignment(Element.ALIGN_CENTER);
		    document.add(image);

		    
		    /* 텍스트 삽입 */
		    document.add(new Paragraph("[주의사항]\n", kor15));
		    document.add(new Paragraph(""
		    		+ "1. 사진 사이즈 너무 큰거 쓰면 안됨\n"
		    		+ "2. 이미지 파일 이름은 챕터 번호에 맞춰서 image1.jpg, image2.jpg ...로 통일해야 됨\n"
		    		+ "3. 텍스트 파일 이름 역시 챕터 번호에 맞춰서 text1.txt, text2.txt로 통일해야 됨\n", kor15));
		    document.add(new Chunk("* 단, 영문 이외의 텍스트가 있는 경우 반드시 UTF-8 형식이어야 됨\n", kor15Red));
		    document.add(new Paragraph(String.format("created at %s\n", 
		    		new SimpleDateFormat("yyyy-MM-dd, HH:mm:ss").format(new Date()))));
		    
		    
		    /* 각 챕터번호 별 챕터 삽입 */
		    Chapter chapter = null;
		    for(int chapterNum = 1; chapterNum<=3; chapterNum++) {
		    	chapter = makeChapter(document, chapterNum); //챕터 생성
		    	document.add(chapter); //생성된 챕터를 document에 삽입
		    }
			
			document.close();//문서 닫기
			writer.close();
			fos.close();
			
			System.out.println("Document Created!");
			Logger.println("Document Created at "+ Util.DATA_PATH + Util.REPORT_NAME + ".pdf");
			
			/* PDF 파일에 워터마크 삽입 */
			insertStamp(Util.DATA_PATH + Util.REPORT_NAME + ".pdf");
			
		}
		catch(Exception e) {
			isSuccess = false;
			e.printStackTrace();
			e.printStackTrace(Logger.pw());
		}
//		finally {
//			ai.set(0); //모든 작업 종료 후 다시 0으로 세팅
//			System.out.println("ai set to 0");
//		}
		
		return isSuccess;
	}
	
	/**
	 * 디폴트 챕터 생성 메서드.</br>
	 * Document 객체와 chapterNum 만 입력받는다.</br>
	 * 나머지는 chapterNum을 바탕으로 자동 생성된다.</br>
	 * <ul>
	 * <li> title: 챕터 1</li>
	 * <li> image: DATA_PATH에서 image1.jpg를 불러옴</li>
	 * <li> text: DATA_PATH에서 text1.txt를 불러옴</li>
	 * </ul>
	 * @param document Document 객체
	 * @param chapterNum 챕터 번호
	 * @return 생성된 챕터 객체
	 * @throws Exception
	 */
	public Chapter makeChapter(Document document, int chapterNum) throws Exception {
		
		String title = null, text = null;
		Image image = null;

		/* 타이틀을 안 정했으면 "챕터 1" 처럼 기본 설정 */
		if (title == null || title.length() == 0)
			title = "챕터 " + chapterNum;

		/* 이미지를 안 정했으면 DATA_PATH에서 image1.jpg와 같이 챕터 번호에 맞는 사진을 불러옴 */
		if (image == null)
			image = Image.getInstance(Util.DATA_PATH + "image" + chapterNum + ".jpg");

		/* 부연설명이 없으면 DATA_PATH에서 text1.txt와 같이 챕터 번호에 맞는 텍스트를 불러욤 */
		if (text == null || text.length() == 0)
			text = readText(Util.DATA_PATH + "text" + chapterNum + ".txt");
		
		return makeChapter(document, chapterNum, title, image, text);
	}
	
	/**
	 * 커스텀 챕터 생성 메서드.</br>
	 * 모든 파라미터를 직접 입력받아서 챕터를 생성한다.
	 * @param document Document 객체
	 * @param chapterNum 챕터 번호
	 * @param title 챕터 타이틀
	 * @param image 챕터에 삽입될 이미지
	 * @param text 챕터 부연설명
	 * @return 생성된 챕터
	 * @throws Exception
	 */
	public Chapter makeChapter(Document document, int chapterNum, String title, Image image, String text) throws Exception {
		
		Chapter chapter = null;
		
		/* 챕터 삽입 */
		chapter = new Chapter(chapterNum); //챕터 생성
		chapter.setTitle(new Paragraph(title, kor15));//타이틀을 맑은고딕 15 사이즈로 생성

		/* 이미지가 null이 아니면 크기 조정 후 삽입 */
		if (image != null) {
			setImageSize(document, image);
			chapter.add(image);
		}
		
		chapter.add(new Paragraph(text, kor10)); //부연설명 삽입
		
		return chapter;
	}

	/**
	 * DATA_PATH 내부의 text3.txt 등의 파일을 읽어서 String으로 반환</b>
	 * 만약 읽어들이는 과정에서 어떠한 에러라도 발생하면 N/A를 반환
	 * @param path text3.txt등의 부연설명 파일이 저장된 경로
	 * @return 읽어들인 내용의 스트링
	 */
	public String readText(String path) {
		
		StringBuilder contents = new StringBuilder();
		try {
			FileInputStream fis = new FileInputStream(path);
			BufferedReader in = new BufferedReader(new InputStreamReader(fis));
			String line;
			while((line = in.readLine()) != null) contents.append(line+"\n");
			in.close();
			fis.close();
			
			System.out.println("Read " + path + " Success!");
		}
		catch(Exception e) {
			e.printStackTrace();
			contents = new StringBuilder("N/A");
			
			System.err.println("Read " + path + " Fail!");
			Logger.println("Read " + path + " Fail!");
		}
		return contents.toString();
	}
	
	/**
	 * 이미지 사이즈를 문서에 맞게 수정
	 * @param document 전체 문서 객체
	 * @param image 이미지 객체
	 */
	public void setImageSize(Document document, Image image){
		
		float imgHeight = image.getHeight(), imgWidth = image.getWidth();
	    float docHeight = document.getPageSize().getHeight(), docWidth = document.getPageSize().getWidth();
	    image.setAlignment(Element.ALIGN_CENTER);
	    if(imgHeight > docHeight || imgWidth > docWidth){
	    	image.scaleToFit(document.getPageSize());
	    }
	}
	
	/**
	 * PDF 파일의 페이지 마다 워터마크 삽입
	 * 우선 문서를 1차적으로 저장한 뒤, 다시 읽어들여서 각 페이지마다 백그라운드 이미지를 레이어 구조로 삽입
	 * @param path 원본 문서 이름을 포함한 경로 ex) haniumPdfReport/[20170721] Scouter Report.pdf
	 */
	public void insertStamp(String path) {
		
		try {
			String tmpPath = Util.DATA_PATH + "tmp.pdf";
			File originFile = new File(path);
			File tmpFile = new File(tmpPath);

			PdfReader pdfReader = new PdfReader(path);
			PdfStamper pdfStamper = new PdfStamper(pdfReader, new FileOutputStream(tmpFile));
			
			Image image = Image.getInstance(Util.DATA_PATH + "stamp.png");
			for(int i=1;i<=pdfReader.getNumberOfPages();i++){
				image.scalePercent(50F);
				image.setAbsolutePosition(PageSize.A4.getWidth()/2 - image.getWidth()/4, 
						PageSize.A4.getHeight()/2 - image.getHeight()/4); //절대값 위치 필수
				//setImageSize(document, image);
				image.setAlignment(Element.ALIGN_MIDDLE);
				PdfContentByte content = pdfStamper.getOverContent(i);
				content.addImage(image);
			}
			pdfStamper.close();
			pdfReader.close();

			/* tmp로 만들어진 문서 이름을 변경 */
			originFile.delete();
			tmpFile.renameTo(originFile);
			
			System.out.println("Watermark Stamping Success!");
			Logger.println("Watermark Stamping Success!");
		}
		catch(Exception e) {
			e.printStackTrace();
			
			System.err.println("Watermark Stamping Fail!");
			Logger.println("Watermark Stamping Fail!");
		}
	}
	
	/**
	 * 메일 보낸 뒤 PDF파일 및 PDF 파일 생성에 사용됬던 자료들을 삭제하는 메서드
	 */
	public void deleteAllData() {
		
		//DATA_PATH 내부의 모든 파일들을 가져옴
		File dataPathFile[] = new File(Util.DATA_PATH).listFiles();
		File renamed = null;
		String fileName = null;
		
		for(File each : dataPathFile) {
			try {
				fileName = each.getName();
				
				if(!fileName.contains("(OLD)") && !fileName.endsWith(".pdf")) {
					renamed = new File(Util.DATA_PATH + String.format("(OLD)%s", fileName));
					renamed.createNewFile();
					
					each.renameTo(renamed);
					//each.delete();
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}
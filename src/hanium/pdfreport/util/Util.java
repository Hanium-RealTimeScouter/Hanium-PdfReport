package hanium.pdfreport.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 여러 곳에서 사용되는 상수들을 모아놓은 클래스
 */
public class Util {
	public static final String REPORT_NAME = new SimpleDateFormat("[yyyyMMdd]").format(new Date()) + " Scouter Report";

	public static final String PATH_SEPARATOR = File.separator; //디렉토리 구분자

	public static final String CUR_PATH = System.getProperty("user.dir") + PATH_SEPARATOR;
	public static final String DATA_PATH = CUR_PATH + "haniumPdfReport" + PATH_SEPARATOR; //사진, 텍스트 등 모든 자료가 담긴 경로
	public static final String FONT_PATH = DATA_PATH + "malgun.ttf"; //경로에 반드시 폰트가 있어야 된다.
	
	public static final String REPORT_FILE_PATH = DATA_PATH + REPORT_NAME + ".pdf"; //pdf 파일의 경로
	
	/**
	 * path의 파일이 있으면 true, 없으면 false
	 * @param path 검사할 파일의 경로
	 * @return 파일이 있으면 true, 없으면 false
	 */
	public static boolean fileExistChecker(String path) {
		return new File(path).exists();
	}
}
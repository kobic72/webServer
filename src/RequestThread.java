import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;


public class RequestThread extends Thread {

	//요청 파일 종류 설정을 위한 값 설정 
	public enum ContentType{
		NONE,
		JSON,
		PDF,
		MP4,
		GIF,
		JPEG,
		PNG,
		CSS,
		HTML,
		JAVASCRIPT,
		PLAIN,
		XML,
		OCTET
	}
	
	//응답 코드 설정 
	public enum ResponseCode{
		NONE,
		OK,
		REDIRECTION,
		NOT_FOUND,
		SERVER_ERROR
	}

	private DBManager dbManager = null;
	public static final String NEWLINE = System.getProperty("line.separator");
	public static final String DEFAULT_WEBAPPS_DIR = "./webapps";

	//클라이언트와 연결된 소켓 
	Socket connection;

	ContentType requestedType = ContentType.NONE;
	ResponseCode responseCode = ResponseCode.NONE;
	
	//서버로부터 인자로 넘겨받은 소켓을 멤버 변수에 맵핑한다.
	public RequestThread(Socket connection, DBManager manager){
		this.connection = connection;
		this.dbManager = manager;
	}

	//작업 시작  
	public void run(){
		//logger.info("request thread started");

		InputStream is;
		OutputStream os;

		try {
			//현재 클라이언트와 연결된 소켓에다가 input / output용 스트림을 할당한다.
			is = connection.getInputStream();
			os = connection.getOutputStream();

			//성능 혹은 안정성을 위한 버퍼 할당 
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			//빈 요청은 스킵 
			String header = br.readLine();
			if (header == null){
				connection.close();
				return;
			}
			
			//클라이언트의 요청 데이터 타입 확인
			String requestedMethod = header.substring(0, header.indexOf(" ") );
			
			//일단 GET 방식의 요청이 아니면 지원 안 함 
			if ( !"GET".equals(requestedMethod) ){
				responseCode = ResponseCode.SERVER_ERROR;
			}
			
			/////////////////////////////////
			// for debug
			/////////////////////////////////
			System.out.println(header);
			
			//요청받은 파일 형식을 확인하고 나중에 응답할 때 쓰기 위해 기록해 둔다.
			setRequestedDataType(header);

			//요청받은 내용 처리할 객체 생성를 생성하고 요청받은 내용을 넘겨서 처리한다.
			HttpRequest request = new HttpRequest();
			//처리 결과는 요청받은 url을 반환하는데, 이는 결국 클라이언트에게 어떤 데이터를 보내주어야 하는지 확인하는 수단이 된다.
			String requestUrl = request.parsingUrl(header);
			
			//주소만 넣어도 기본 페이지가 보이도록 설정
			if ( !requestUrl.contains(".") ){
				requestUrl += "index.html";
			}
			
			//요청 받은 데이터를 보낼 스트림 생성  
			DataOutputStream dos = new DataOutputStream(os);
			
			//db조회가 필요한지 확인 
			String tempString = requestUrl.substring(requestUrl.lastIndexOf("_") + 1, requestUrl.length() );
			if ("newsTag.json".equals(tempString) ){
				//newsTag 관련 요청이므로 db조회 필요
				String newspaperName = requestUrl.substring(requestUrl.lastIndexOf("/") + 1, requestUrl.lastIndexOf("_") );
				String result = dbManager.getTags(newspaperName);
				
				if (result != null){
					responseCode = ResponseCode.OK;
				}
				System.out.println(">>> " + result);
				
				//db 조회 결과를 json 형식으로 전송
				responseHTML(dos, result.getBytes("UTF-8").length );
				dos.write(result.getBytes("UTF-8"));
				
				//encoding problem
				//dos.writeUTF(result);
				//dos.writeBytes(""[[\"test\", 4], [\"테스트\", 2]]");
			} else {
				//db조회가 필요하지 않음 - 폴더 안에서 요청 파일을 찾아보자 
				//보내줄 파일들을 저장하는 폴더 아래에서 요청받은 파일을 찾아서 변수에 할당
				File requesstFile = new File(DEFAULT_WEBAPPS_DIR + requestUrl);
				
				//찾은 파일이 존재하지 않으면 404 코드를 설정하고, 만약 요청받은 파일이 html형식이라면 에러 메시지를 표시할 수 있는 오류 페이지 전송
                if ( !requesstFile.exists() ){
					responseCode = ResponseCode.NOT_FOUND;
					
					//error message out
					if (requestedType == ContentType.HTML){
						requesstFile = new File(DEFAULT_WEBAPPS_DIR + "/not_found_html.html");
					}
					
					/////////////////////////////////
					// for debug - not found 로그 표시
					/////////////////////////////////
					System.out.println("NOT FOUND : " + requestUrl);
				} else {
					responseCode = ResponseCode.OK;
				}
				
				/////////////////////////////////
				// for debug - 보내줄 파일 로그 표시 
				/////////////////////////////////
				System.out.println(requesstFile);
				
				//요청 처리 메시지를 만든다
				responseHTML(dos, requesstFile.length() );
				
				FileInputStream fis = new FileInputStream(requesstFile);
		
				//본격적으로 요청받은 데이터를 쓴다
				int data = fis.read();

				while(data != -1){
					dos.write(data);
					data = fis.read();
				}
				fis.close();
			}

			//다 썼으면 스트림들 정리하고 소켓을 정리(연결을 끊는다. http에서는 서버가 연결을 끊으니까)
			dos.close();
			connection.close(); //keep-alive는 지원하지 않음

		} catch (IOException e) {
			//e.printStackTrace();
			//logger.warning(e.getMessage());
		}
	}

	private void responseHTML(DataOutputStream dos, long length) throws IOException {
		//성공한 경우 해당하는 메시지 생성 - 실패인 경우 상위에서 판단할 지 여기서 할 지 확인 필요함 
		String code = selectResponseCode();
		String type = selectTypeHeader();
		response(dos, length, code, type);
	}

	//클라이언트가 요청한 파일을 전송하는 메시지를 버퍼에다 쓴다.
	private void response(DataOutputStream dos, long length, String code, String type) throws IOException {
		dos.writeBytes("HTTP/1.0 " + code + " Document Follows " + NEWLINE);
		dos.writeBytes("Content-Type: " + type + " ;charset=utf-8" + NEWLINE);
		if (requestedType == ContentType.JSON){
			dos.writeBytes("Cache-Control: max-age=10, must-revalidate" + NEWLINE);
		} else {
			dos.writeBytes("Cache-Control: max-age=60, must-revalidate" + NEWLINE);
		}
		dos.writeBytes("Content-Length: " + length + NEWLINE);
		dos.writeBytes(NEWLINE); //데이터가 들어가기 전에는 빈 줄 생성 
		
		System.out.println("    " + "HTTP/1.0 " + code + " Document Follows " + NEWLINE);
		System.out.println("    " + "Content-Type: " + type + " ;charset=utf-8" + NEWLINE);
		System.out.println("    " + "Content-Length: " + length + NEWLINE);
	}

	private void setRequestedDataType(String header){
		int startPosition = header.indexOf(".") + 1;
		int endPosition = header.lastIndexOf(" ");
		String type = null;
		
		if (startPosition >= endPosition)
			type = "html";
		else
			type = header.substring(startPosition, endPosition);

		if ( "json".equals(type) ){
			requestedType = ContentType.JSON;
			return;
		}

		if ( "html".equals(type) ){
			requestedType = ContentType.HTML;
			return;
		}

		if ( "css".equals(type) ){
			requestedType = ContentType.CSS;
			return;
		}

		if ( "js".equals(type) ){
			requestedType = ContentType.JAVASCRIPT;
			return;
		}

		if ( "xml".equals(type) ){
			requestedType = ContentType.XML;
			return;
		}

		if ( "gif".equals(type) ){
			requestedType = ContentType.GIF;
			return;
		}

		if ( "jpg".equals(type) ){
			requestedType = ContentType.JPEG;
			return;
		}

		if ( "jpeg".equals(type) ){
			requestedType = ContentType.JPEG;
			return;
		}

		if ( "png".equals(type) ){
			requestedType = ContentType.PNG;
			return;
		}

		if ( "pdf".equals(type) ){
			requestedType = ContentType.PDF;
			return;
		}

		if ( "mp4".equals(type) ){
			requestedType = ContentType.MP4;
			return;
		}

		if ( "".equals(type) ){
			requestedType = ContentType.PLAIN;
			return;
		}

		requestedType = ContentType.OCTET;
		return;
	}

	private String selectTypeHeader(){
		String typeHeader = null;

		switch(requestedType){
		case NONE:
			break;
		case JSON:
			typeHeader = "application/json";
			break;
		case PDF:
			typeHeader = "application/pdf";
			break;
		case MP4:
			typeHeader = "audio/mp4";
			break;
		case GIF:
			typeHeader = "image/gif";
			break;
		case JPEG:
			typeHeader = "image/jpeg";
			break;
		case PNG:
			typeHeader = "image/png";
			break;
		case CSS:
			typeHeader = "text/css";
			break;
		case HTML:
			typeHeader = "text/html";
			break;
		case JAVASCRIPT:
			typeHeader = "text/javascript";
			break;
		case PLAIN:
			typeHeader = "text/plain";
			break;
		case XML:
			typeHeader = "text/xml";
			break;
		case OCTET:
			typeHeader = "text/octet-stream";
			break;
		}

		return typeHeader;
	}
	
	private String selectResponseCode(){
		String code = null;
		
		switch(responseCode){
		case OK:
			code = "200";
			break;
		case REDIRECTION:
			code = "301";
			break;
		case NOT_FOUND:
			code = "404";
			break;
		case SERVER_ERROR:
			code = "501";
			break;
		default:
			break;	
		}
		
		return code;
	}
}

import java.io.BufferedReader;
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

	//��û ���� ���� ������ ���� �� ���� 
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
	
	//���� �ڵ� ����  
	public enum ResponseCode{
		NONE,
		OK,
		REDIRECTION,
		NOT_FOUND,
		SERVER_ERROR
	}

	//public static LofHandler logger = 
	public static final String NEWLINE = System.getProperty("line.separator");
	public static final String DEFAULT_WEBAPPS_DIR = "./webapps";

	//Ŭ���̾�Ʈ�� ����� ���� 
	Socket connection;

	ContentType requestedType = ContentType.NONE;
	ResponseCode responseCode = ResponseCode.NONE;
	
	//�����κ��� ���ڷ� �Ѱܹ��� ������ ��� ������ �����Ѵ�.
	public RequestThread(Socket connection){
		this.connection = connection;
	}

	//�۾� ���� 
	public void run(){
		//logger.info("request thread started");

		//text�̸� reader/writer - image�� ���� ó�� �ʿ� 
		InputStream is;
		OutputStream os;

		try {
			//���� Ŭ���̾�Ʈ�� ����� ���Ͽ��ٰ� input / output�� ��Ʈ���� �Ҵ��Ѵ�.
			is = connection.getInputStream();
			os = connection.getOutputStream();

			//���� Ȥ�� �������� ���� ���� �Ҵ� 
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			//Ŭ���̾�Ʈ�� ��û ������ Ÿ�� Ȯ�� 
			String header = br.readLine();
			if (header == null)
				return;
			
			String requestedMethod = header.substring(0, header.indexOf(" ") );
			
			//�ϴ� GET ����� ��û�� �ƴϸ� ���� �� �� 
			if ( !"GET".equals(requestedMethod) ){
				responseCode = ResponseCode.SERVER_ERROR;
			}
			
			/////////////////////////////////
			// for debug
			/////////////////////////////////
			System.out.println(header);
			
			//��û���� ���� ������ Ȯ���ϰ� ���߿� ������ �� ���� ���� ����� �д�.
			setRequestedDataType(header);

			//��û���� ���� ó���� ��ü ������ �����ϰ� ��û���� ������ �Ѱܼ� ó���Ѵ�.
			HttpRequest request = new HttpRequest();
			//ó�� ����� ��û���� url�� ��ȯ�ϴµ�, �̴� �ᱹ Ŭ���̾�Ʈ���� � �����͸� �����־�� �ϴ��� Ȯ���ϴ� ������ �ȴ�.
			String requestUrl = request.parsingUrl(header);
			
			//�ּҸ� �־ �⺻ �������� ���̵��� ����
			if ( !requestUrl.contains(".") ){
				requestUrl += "index.html";
			}
			
			//������ ���ϵ��� �����ϴ� ���� �Ʒ����� ��û���� ������ ã�Ƽ� ������ �Ҵ� 
			File requesstFile = new File(DEFAULT_WEBAPPS_DIR + requestUrl);
			
			//ã�� ������ �������� ������ 404 �ڵ带 �����ϰ�, ���� ��û���� ������ html�����̶�� ���� �޽����� ǥ���� �� �ִ� ���� ������ ����
			if ( !requesstFile.exists() ){
				responseCode = ResponseCode.NOT_FOUND;
				
				//error message out
				if (requestedType == ContentType.HTML){
					requesstFile = new File(DEFAULT_WEBAPPS_DIR + "/not_found_html.html");
				}
				
				/////////////////////////////////
				// for debug - not found �α� ǥ��
				/////////////////////////////////
				System.out.println("NOT FOUND : " + requestUrl);
			} else {
				responseCode = ResponseCode.OK;
			}

			/////////////////////////////////
			// for debug - ������ ���� �α� ǥ�� 
			/////////////////////////////////
			System.out.println(requesstFile);

			//���Ͽ� �Ҵ�� output��Ʈ�����ٰ� 
			DataOutputStream dos = new DataOutputStream(os);
			
			//��û ó�� �޽����� �����
			responseHTML(dos, requesstFile.length() );
			
			FileInputStream fis = new FileInputStream(requesstFile);
	
			//���������� ��û���� �����͸� ����
			int data = fis.read();

			while(data != -1){
				dos.write(data);
				data = fis.read();
			}
			
			//�� ������ ��Ʈ���� �����ϰ� ������ ����(������ ���´�. http������ ������ ������ �����ϱ�)
			fis.close();
			dos.close();
			
			//keep-alive�� �������� ���� 
			connection.close();

		} catch (IOException e) {
			//e.printStackTrace();
			//logger.warning(e.getMessage());
		}
	}

	private void responseHTML(DataOutputStream dos, long length) throws IOException {
		//������ ��� �ش��ϴ� �޽��� ���� - ������ ��� �������� �Ǵ��� �� ���⼭ �� �� Ȯ�� �ʿ��� 
		String code = selectResponseCode();
		String type = selectTypeHeader();
		response(dos, length, code, type);
	}

	//Ŭ���̾�Ʈ�� ��û�� ������ �����ϴ� �޽����� ���ۿ��� ����.
	private void response(DataOutputStream dos, long length, String code, String type) throws IOException {
		dos.writeBytes("HTTP/1.0 " + code + " Document Follows " + NEWLINE);
		dos.writeBytes("Content-Type: " + type + " ;charset=utf-8" + NEWLINE);
		dos.writeBytes("Cache-Control: max-age=3600, must-revalidate" + NEWLINE);
		dos.writeBytes("Content-Length: " + length + NEWLINE);
		dos.writeBytes(NEWLINE); //�����Ͱ� ���� ������ �� �� ���� 
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

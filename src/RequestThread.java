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

	//public static LofHandler logger = 
	public static final String NEWLINE = System.getProperty("line.separator");
	public static final String DEFAULT_WEBAPPS_DIR = "./webapps";

	//Ŭ���̾�Ʈ�� ����� ���� 
	Socket connection;

	ContentType requestedType = ContentType.NONE;

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

			System.out.println(header);
			setRequestedDataType(header);

			//debug
			String path = header;
			

			//header�� null�� �ƴϰ� ����ִ� ����(�� ���� ����)�� �ƴ϶�� ��� �� �پ� �а� �ֿܼ� ��� 
			/*
			while( !"".equals(header) && header != null){
				header = br.readLine();
				System.out.println(header);
			}
			 */
			//��û���� ���� ó���� ��ü ������ �����ϰ� ��û���� ������ �Ѱܼ� ó���Ѵ�.
			HttpRequest request = new HttpRequest();
			//ó�� ����� ��û���� url�� ��ȯ�ϴµ�, �̴� �ᱹ Ŭ���̾�Ʈ���� � �����͸� �����־�� �ϴ��� Ȯ���ϴ� ������ �ȴ�.
			String requestUrl = request.parsingUrl(path);

			//������ ���ϵ��� �����ϴ� ���� �Ʒ����� ��û���� ������ ã�Ƽ� ������ �Ҵ� 
			File requesstFile = new File(DEFAULT_WEBAPPS_DIR + requestUrl);

			System.out.println(requesstFile);

			//���Ͽ� �Ҵ�� output��Ʈ�����ٰ� 
			DataOutputStream dos = new DataOutputStream(os);
			FileInputStream fis = new FileInputStream(requesstFile);

			//��û ó�� �޽����� ����� 
			responseHTMLOK(dos, requesstFile.length() );

			//���������� ��û���� �����͸� ���� - do while���� 
			int data = fis.read();

			while(data != -1){
				dos.write(data);
				data = fis.read();
			}

			//�� ������ ��Ʈ���� �����ϰ� ������ ����(������ ���´�. http������ ������ ������ �����ϱ�)
			fis.close();
			dos.close();
			connection.close();

		} catch (IOException e) {
			//e.printStackTrace();
			//logger.warning(e.getMessage());
		}
	}

	private void responseHTMLOK(DataOutputStream dos, long length) throws IOException {
		//������ ��� �ش��ϴ� �޽��� ���� - ������ ��� �������� �Ǵ��� �� ���⼭ �� �� Ȯ�� �ʿ��� 
		String type = selectTypeHeader();
		responseOk(dos, length, type);
	}

	//Ŭ���̾�Ʈ�� ��û�� ������ �����ϴ� �޽����� ���ۿ��� ����.
	private void responseOk(DataOutputStream dos, long length, String string) throws IOException {
		dos.writeBytes("HTTP/1.0 200 Document Follows " + NEWLINE);
		dos.writeBytes("Content-Type: " + string + " ;charset=utf-8" + NEWLINE);
		dos.writeBytes("Content-Length: " + length + NEWLINE);
		dos.writeBytes(NEWLINE); //�����Ͱ� ���� ������ �� �� ���� 

		System.out.println(string);
	}

	private void setRequestedDataType(String header){
		int startPosition = header.indexOf(".") + 1;
		int endPosition = header.lastIndexOf(" ");
		String type = null;
		
		if (startPosition >= endPosition)
			type = "html";
		else
			//String type = header.substring(startPosition, header.indexOf(" ", startPosition) );
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
}

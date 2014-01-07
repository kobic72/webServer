import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class WebServer 
{
	public static void main(String[] args) throws UnknownHostException, IOException 
	{
		//서버용 소켓을 준비한다. (listen하기 위해서 - port는 test니까 8080으로)
		ServerSocket server = new ServerSocket(8083);
		
		//				reuse옵션 줘야될 수도?				//
		
		//logger.info("webserver started");
		
		//요청에 의해서 연결되면 그걸 소켓으로 반환하고 그 소켓을 통해서 요청한 클라이언트와 통신한다.
		Socket connection = null;
		
		//요청이 있는지 계속 확인한다.
		while ( true )
		{
			//요청 들어온 것이 있으면 일단 임시로 소켓에 저장
			connection = server.accept();
			//임시로 저장한 소켓 정보를 인자로 주면서 요청을 처리하는 스레드를 생성하고 시작한다.
			RequestThread thread = new RequestThread(connection);
			thread.start();
		}
	}
}

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

public class WebServer 
{
	public static void main(String[] args) throws UnknownHostException, IOException 
	{
		//������ ������ �غ��Ѵ�. (listen�ϱ� ���ؼ� - port�� test�ϱ� 8080����)
		ServerSocket server = new ServerSocket(8080);
		//Socket server = new Socket();
		
		//SocketAddress addr = new InetSocketAddress("127.0.0.1", 8080);
		//server.bind(addr);
		
		//server.setTcpNoDelay(true);
		
		//server.
		
		//logger.info("webserver started");
		
		//��û�� ���ؼ� ����Ǹ� �װ� �������� ��ȯ�ϰ� �� ������ ���ؼ� ��û�� Ŭ���̾�Ʈ�� ����Ѵ�.
		Socket connection = null;
		
		//��û�� �ִ��� ��� Ȯ���Ѵ�.
		while ( true )
		{
			//��û ���� ���� ������ �ϴ� �ӽ÷� ���Ͽ� ����
			connection = server.accept();
			//�ӽ÷� ������ ���� ������ ���ڷ� �ָ鼭 ��û�� ó���ϴ� �����带 �����ϰ� �����Ѵ�.
			RequestThread thread = new RequestThread(connection);
			thread.start();
		}
	}
}

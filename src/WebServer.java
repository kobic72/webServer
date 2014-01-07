import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class WebServer 
{
	public static void main(String[] args) throws UnknownHostException, IOException 
	{
		//������ ������ �غ��Ѵ�. (listen�ϱ� ���ؼ� - port�� test�ϱ� 8080����)
		ServerSocket server = new ServerSocket(8083);
		
		//				reuse�ɼ� ��ߵ� ����?				//
		
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

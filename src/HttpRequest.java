
public class HttpRequest {

	public String parsingUrl(String path){
		//GET �Ǵ� POST �ٷ� ������ ���� �κ��� ��û�� �ڷ� - ���⸦ �������� ���еǾ� �����Ƿ� ó������ ���� �Ǿ� �ִ� ������ �� ���� �������
		String requestUrl = path.substring(path.indexOf(" ") + 1, path.lastIndexOf(" ") );
		
		return requestUrl;
	}
}

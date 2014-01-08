
public class HttpRequest {

	public String parsingUrl(String path){
		//GET 또는 POST 바로 다음에 오는 부분이 요청한 자료 - 띄어쓰기를 기준으로 구분되어 있으므로 처음으로 띄어쓰기 되어 있는 곳부터 그 다음 띄어쓰기까지
        String requestUrl = path.substring(path.indexOf(" ") + 1, path.lastIndexOf(" ") );
		
		return requestUrl;
	}
}

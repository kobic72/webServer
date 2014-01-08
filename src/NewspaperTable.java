import java.util.HashMap;


public class NewspaperTable {
	public HashMap<String, String> newspaperTable = null;
	
	NewspaperTable(){
		this.newspaperTable = new HashMap<String, String>();
		
		this.newspaperTable.put("kh", "경향신문");
		this.newspaperTable.put("joins", "중앙일보");
		this.newspaperTable.put("cb", "조선비즈");
		this.newspaperTable.put("bloter", "블로터닷넷");
		this.newspaperTable.put("mediatoday", "미디어오늘");
	}
	
	public String getNewspaperName(String key){
		return this.newspaperTable.get(key);
	}
}

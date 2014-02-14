package models;

public class ResponseFormat {

	public String contentType;
	public Object content;
	
	public ResponseFormat(String contentType, Object content){
		this.contentType = contentType;
		this.content = content;
	}
	
	public String getContentType() {
		return contentType;
	}

	public Object getContent() {
		return content;
	}
}

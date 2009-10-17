package gui.util;

public class Result {

	private String text;
	private String url;
	
	public Result() {
		this.text = "";
		this.url = "";
	}

	public Result( String text, String url ) {
		this.text = text;
		this.url = url;
	}
	
	public boolean equals(Result o) {
		return o.getText().equals(this.text) && o.getUrl().equals(this.url) ;
	}

	public String getText() {
		return text;
	}

	public String getUrl() {
		return url;
	}
}

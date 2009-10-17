package gui.util;

public class Result {

	private String text;
	private String url;

	public Result( String text, String url ) {
		this.text = text;
		this.url = url;
	}

	public String getText() {
		return text;
	}

	public String getUrl() {
		return url;
	}
}

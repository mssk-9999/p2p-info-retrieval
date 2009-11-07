package p2p.info.retrieval.web.model;

import org.directwebremoting.annotations.DataTransferObject;
import org.directwebremoting.annotations.RemoteProperty;
import org.directwebremoting.convert.ObjectConverter;

@DataTransferObject(converter = ObjectConverter.class)
public class Result {

	@RemoteProperty
	public String text;
	
	@RemoteProperty
	public String link;

	public Result(String text) {
		this.text = text;
		this.link = "http://www.google.ca";
	}

}

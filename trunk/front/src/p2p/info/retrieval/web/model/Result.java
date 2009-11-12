package p2p.info.retrieval.web.model;

import org.apache.lucene.document.Document;
import org.directwebremoting.annotations.DataTransferObject;
import org.directwebremoting.annotations.RemoteProperty;
import org.directwebremoting.convert.ObjectConverter;

@DataTransferObject(converter = ObjectConverter.class)
public class Result {

	@RemoteProperty
	public String title;
	
	@RemoteProperty
	public String path;
	
	public Result(Document doc) {
		this.title = doc.get("title");
		this.path = doc.get("path");
	}

}

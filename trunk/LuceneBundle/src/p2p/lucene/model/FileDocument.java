package p2p.lucene.model;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.ContentHandlerDecorator;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/** A utility for making Lucene Documents from a File. */

public class FileDocument {
	/** Makes a document for a File.
    <p>
    The document has three fields:
    <ul>
    <li><code>path</code>--containing the pathname of the file, as a stored,
    untokenized field;
    <li><code>size</code>--containing the size of the file, as a stored,
    untokenized field;
    <li><code>modified</code>--containing the last modified date of the file as
    a field as created by <a
    href="lucene.document.DateTools.html">DateTools</a>; and
    <li><code>contents</code>--containing the full contents of the file, as a
    Reader field;
	 * @throws TikaException 
	 * @throws SAXException 
	 * @throws IOException 
	 */
	public static Document Document(File f, File docDir) throws IOException, SAXException, TikaException {

		// make a new, empty document
		Document doc = new Document();

		// Add the path of the file as a field named "path".  Use a field that is 
		// indexed (i.e. searchable), but don't tokenize the field into words.
		String absFilePath = f.getPath();
		String relFilePath = absFilePath.replace(docDir.getPath(), "");
		doc.add(new Field("path", relFilePath, Field.Store.YES, Field.Index.NOT_ANALYZED));

		// Add the path of the file as a field named "path".  Use a field that is 
		// indexed (i.e. searchable), but don't tokenize the field into words.
		doc.add(new Field("size", Long.toString(f.length()), Field.Store.YES, Field.Index.NOT_ANALYZED));

		// Add the last modified date of the file a field named "modified".  Use 
		// a field that is indexed (i.e. searchable), but don't tokenize the field
		// into words.
		doc.add(new Field("modified",
				DateTools.timeToString(f.lastModified(), DateTools.Resolution.MINUTE),
				Field.Store.YES, Field.Index.NOT_ANALYZED));

		// Add the contents of the file to a field named "contents".  Specify a Reader,
		// so that the text of the file is tokenized and indexed, but not stored.
		// Note that FileReader expects the file to be in the system's default encoding.
		// If that's not the case searching for special characters will fail.
		InputStream stream = new FileInputStream(f);
		ContentHandler handler = new BodyContentHandler(); 
		ContentHandlerDecorator decorator = new ContentHandlerDecorator(handler);
		
		Metadata data = new Metadata();
		data.add("title", "");
		data.add("author", "");
		data.add("subject", "");
		
		ParseContext context = new ParseContext();
		AutoDetectParser parser = new AutoDetectParser();
		parser.parse(stream, handler, data, context);
		
		doc.add(new Field("contents", new StringReader(decorator.toString())));

		//Add the Title of the file in a field named "title". Use a field that is indexed
		//(i.e. searchable), but don't tokenize the field into words. If this file does not
		//contain a title this field is left empty.
		doc.add(new Field("title", data.get("title"), Field.Store.YES, Field.Index.NOT_ANALYZED));
		
		//Add the Author of the file in a field named "author". Use a field that is indexed
		//(i.e. searchable), but don't tokenize the field into words. If the file does not contain
		//an author this field is left empty.
		doc.add(new Field("author", data.get("author"), Field.Store.YES, Field.Index.NOT_ANALYZED));
		
		//Add the subject of the file in a field named "subject". Use a field that is indexed
		//(i.e. searchable), but don't tokenize the field into words. If the file does not contain
		//a subject this field is left empty. 
		doc.add(new Field("subject", data.get("subject"), Field.Store.YES, Field.Index.NOT_ANALYZED));

		// return the document
		return doc;
	}

	private FileDocument() {}
}


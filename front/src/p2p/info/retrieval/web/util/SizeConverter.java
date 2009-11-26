package p2p.info.retrieval.web.util;

public class SizeConverter {
	
	private static final long KILOBYTE = 1024L;
	private static final long MEGABYTE = KILOBYTE * 1024L;
	private static final long GIGABYTE = MEGABYTE * 1024L;
	private static final long TERABYTE = GIGABYTE * 1024L;
	
	public static String convert (long bytes) {
		String size;
		
		if(bytes < KILOBYTE)
			size = bytes + " B";
		else if(bytes >= KILOBYTE && bytes < MEGABYTE)
			size = bytes/KILOBYTE + " KB";
		else if(bytes >= MEGABYTE && bytes < GIGABYTE)
			size = bytes/MEGABYTE + " MB";
		else if(bytes >= GIGABYTE && bytes < TERABYTE)
			size = bytes/GIGABYTE + " GB";
		else
			size = bytes/TERABYTE + " TB";
		return size;
	}

}

package p2p.web.util;

import java.text.DecimalFormat;

public class SizeFormatter {

	private static final long KILOBYTE = 1024L;
	private static final long MEGABYTE = KILOBYTE * 1024L;
	private static final long GIGABYTE = MEGABYTE * 1024L;
	private static final long TERABYTE = GIGABYTE * 1024L;

	public static String format(long bytes) {
		String size;

		if(bytes < KILOBYTE)
			size = bytes + " B";
		else if(bytes >= KILOBYTE && bytes < MEGABYTE)
			size = roundTwoDecimals(bytes/KILOBYTE) + " KB";
		else if(bytes >= MEGABYTE && bytes < GIGABYTE)
			size = roundTwoDecimals(bytes/MEGABYTE) + " MB";
		else if(bytes >= GIGABYTE && bytes < TERABYTE)
			size = roundTwoDecimals(bytes/GIGABYTE) + " GB";
		else
			size = roundTwoDecimals(bytes/TERABYTE) + " TB";
		return size;
	}

	private static long roundTwoDecimals(long d) {
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		return Long.valueOf(twoDForm.format(d));
	}

}

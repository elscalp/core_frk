// JAVA CUSTOM FUNCTION FOR REPLACING STRING BASED ON REGULAR EXPRESSION

package core_common.xpath_functions;

import java.net.URLEncoder;
import java.util.regex.*;

public class FString
{

	// In case you need static initialisation
	//static {
	//}

	public static final String HELP_STRINGS[][] = {
		{"ApplyRegexp", "Apply a regexp match/replace to a string, and return result.", "ApplyRegexp(\"[ab]\", \"Z\", \"abcdabc\")", "ZZcdZZc"},
		{"lTrim", "Trim by the left a string", "lTrim(\"0000ABC\", \"0\")", "ABC"},
		{"toSAPDateString", "Return a SAP formatted date", "toSAPDateString(\"2013-02-15\", \"YYYY-MM-DD\")", "20130215"},
		{"toSAPDeliveryDateString", "Return a SAP formatted delivery date", "toSAPDeliveryDateString(\"2013-02-15\", \"YYYY-MM-DD\")", "15.02.2013"},
		{ "URLEncode",
				"Encode the given string as an URL parameter string and using the given encoding, then return the result.",
				"Example",
				"URLEncode(\"The string �@foo-bar\", \"UTF-8\") returns The+string+%C3%BC%40foo-bar" }
	};

	public static String ApplyRegexp(String findRegexp, String replaceRegexp, String inputString)
	{				
		Pattern p = Pattern.compile(findRegexp,Pattern.MULTILINE);
		Matcher m = p.matcher(inputString);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(sb, replaceRegexp);
		}
		m.appendTail(sb);
		return sb.toString();
	}
	
	public static final String URLEncode(String expression, String encoding) {
		try {
			return URLEncoder.encode(expression, encoding);
		} catch (Exception e) {
			throw new RuntimeException(e.toString() + ": " + e.getMessage());
		}
	}

	public static String lTrim(String srcString, String searchString)
	{				
		String str = srcString;
		while (str.startsWith(searchString)) {
			str = str.substring(1);
		}
		return str;
	}
	
	public static String toSAPDateString(String srcString, String srcFormat)
	{				
		// return format is YYYYMMDD
		String str = "";
		if (srcString!=null && srcString.length()>7) str = srcString.substring(0,8);
		
		if (srcFormat.equals("YYYY-MM-DD") && srcString!=null && srcString.length()>9) {
			str = srcString.substring(0,4)+srcString.substring(5,7)+srcString.substring(8,10);
		}
		else if (srcFormat.equals("DD/MM/YYYY") && srcString!=null && srcString.length()>9) {
			str = srcString.substring(6,10)+srcString.substring(3,5)+srcString.substring(0,2);
		}
		return str;
	}
	
	public static String toSAPDeliveryDateString(String srcString, String srcFormat)
	{				
		// return format is DD.MM.YYYY
		String str = "";
		if (srcString!=null && srcString.length()>7) str = srcString.substring(0,8);
		
		if (srcFormat.equals("YYYY-MM-DD") && srcString!=null && srcString.length()>9) {
			str = srcString.substring(8,10)+"."+srcString.substring(5,7)+"."+srcString.substring(0,4);
		}
		return str;
	}
	
	public static void main(String[] args)
	{
		try {
			System.out.println("ApplyRegexp(\"[ab]\", \"Z\", \"abcdabc\") returns [" + ApplyRegexp("[ab]","Z","abcdabc") + "].");
			System.out.println("lTrim(\"0000ABC\", \"0\") returns [" + lTrim("0000ABC", "0") + "].");
			System.out.println("toSAPDateString(\"2013-02-15\", \"YYYY-MM-DD\") returns [" + toSAPDateString("2013-02-15", "YYYY-MM-DD") + "].");
			System.out.println("toSAPDateString(\"11/04/2013\", \"DD/MM/YYYY\") returns [" + toSAPDateString("11/04/2013", "DD/MM/YYYY") + "].");
			System.out.println("toSAPDeliveryDateString(\"2013-02-15\", \"YYYY-MM-DD\") returns [" + toSAPDeliveryDateString("2013-02-15", "YYYY-MM-DD") + "].");
			System.out.println("URLEncode(\"The string �@foo-bar\", \"UTF-8\") returns "
					+ URLEncode("nSxqbZItg25G2yCRduwYAg==", "UTF-8"));
		}catch (Exception e)
		{
			System.out.println("Failure: "  + e.toString() + " - " + e.getMessage());
		}
	}
}


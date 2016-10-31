package core_common.xpath_functions;

import java.util.GregorianCalendar;

public class FTime
{
	static GregorianCalendar gcal = (GregorianCalendar)GregorianCalendar.getInstance();
	
	// In case you need static initialization
	//static {
		// 
	//}

	public static final String HELP_STRINGS[][] = {
		{"TimestampMSToXMLDate", "Converts a time represented in milleseconds (since January 1, 1970, 00:00:00 GMT) into XML compliant (ISO8601) Date", "Example", "TimestampMSToXMLDate(1332768306418) returns [2012-03-26T15:25:06.418+02:00]."},
		{"XMLDateToTimestampMS", "Converts an XML compliant Date (ISO8601) into time in millisec (since January 1, 1970, 00:00:00 GMT)", "Example", "XMLDateToTimestampMS(\"2012-03-26T15:25:06.418+02:00\") returns [1332768306418]."}
	};

	public static String TimestampMSToXMLDate(long MSdate)
	{
		// Using 
		try {			
			
            gcal.setTimeInMillis(MSdate);
            return javax.xml.datatype.DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal).toString();
        }
        catch (Exception ex) {
            throw new RuntimeException ("Exception while converting millisecond timestamp to XML Date - " + ex.toString());
        }		
	}
	
	public static long XMLDateToTimestampMS(String sDate)
	{
		try {
			return javax.xml.datatype.DatatypeFactory.newInstance().newXMLGregorianCalendar(sDate).toGregorianCalendar().getTimeInMillis(); 
        }
        catch (Exception ex) {
            throw new RuntimeException ("Exception while converting XML date to timestamp in milliseconds - " + ex.toString());
        }		
	}
	
	public static void main(String[] args)
	{
		try {
			
			// Conversions XML date and date millisec
			long datems = System.currentTimeMillis(); 
			String sDate = TimestampMSToXMLDate(datems);
			System.out.println("TimestampMSToXMLDate(" + Long.toString(datems) + ") returns [" + sDate + "].");
			System.out.println("XMLDateToTimestampMS(" + sDate + ") returns [" + XMLDateToTimestampMS(sDate) + "].");
			
		}catch (Exception e)
		{
			System.out.println("Failure: "  + e.toString() + " - " + e.getMessage());
		}
	}
}
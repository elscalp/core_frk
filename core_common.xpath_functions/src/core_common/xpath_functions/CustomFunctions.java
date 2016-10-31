package core_common.xpath_functions;

import java.net.URLEncoder;
import java.security.Key;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.tibco.xml.cxf.common.annotations.XPathFunction;
import com.tibco.xml.cxf.common.annotations.XPathFunctionGroup;
import com.tibco.xml.cxf.common.annotations.XPathFunctionParameter;

@XPathFunctionGroup(category = "TIBCO Custom Functions", prefix = "customFunctions", namespace = "http://com.tibco.esb.java", helpText = "TIBCO Framework Custom Functions")
public class CustomFunctions {
	
	/** private variables for CustomFunctions**/
	private static Random  m_random;
	private static String localHostName=null;
	private static String fileSeparator=null;
	
	/** private variables for FCrypt**/
	private static MessageDigest md;
	private static IvParameterSpec iv;
	private static final byte[] IVValue = new byte[] { 'v', 'V', 'Q', 'n', 'F',
			'5', 'n', 's', '4', 'X', 'f', 'v', 'g', 'd', 'Q', 'Z' };

	static {
		try {
			md = MessageDigest.getInstance("MD5");
			iv = new IvParameterSpec(IVValue);
		} catch (Exception e) {
			throw new RuntimeException(e.toString() + ": " + e.getMessage());
		}
	}
	
	/** private variables for FLocalCache**/
	private static Hashtable<String,Hashtable<String,String>> localStorage=new Hashtable<String,Hashtable<String,String>>(100);
	protected static CustomFunctions singleton=new CustomFunctions();
	public static CustomFunctions getInstance()
	 {
	   return singleton;
	 }
	
	/** private variables for FTime**/
	static GregorianCalendar gcal = (GregorianCalendar)GregorianCalendar.getInstance();

	public static final String HELP_STRINGS[][] = {
		{"getGUID", 
        	"same as getUUIDNoDashes : Generate globally unique 128bit UUID without dashes('-').", 
        	"Example", 
        	"getUUIDNoDashes returns dcd05d5f6b35465eb588e7c72e3112d9"},
		{"getGUIDwithTimeStamp",
			"same as getGUID : Generate globally unique 128bit UUID without dashes('-') plus TimeStamp with milliseconds",
			"Example",
			"getGUIDwithTimeStamp returns dcd05d5f6b35465eb588e7c72e3112d920160404120808532" 
		},     
        {"getUUID", 
        	"Generate globally unique 128bit UUID with dashes('-').", 
        	"Example", 
        	"getUUID returns 360b03f4-61b9-4eec-b0d8-8ceb31cbd570"},
        {"getUUIDNoDashes", 
        	"Generate globally unique 128bit UUID without dashes('-').", 
        	"Example", 
        	"getUUIDNoDashes returns dcd05d5f6b35465eb588e7c72e3112d9"},
		{"getRandom", 
        	"Generate a random float in [0..1]", 
        	"Example", 
        	"getRandom returns 0.78"},
		{"getLocalHostname", 
			"Returns the local Host name where this BW engines runs. This function caches the hostname. Use this for all your BW project implementations!", 
			"Example", 
			"getLocalHostName() returns myhost1"},
		{"getFS", 
			"Returns the system-dependent file separator character, represented as a string for convenience.This function caches the FileSeparator. Use this for all your BW project implementations!", 
			"Example", 
			"getSP() returns \"\\\""},
		{"getEnvString", 
			"Returns the value of an environment system variable.", 
			"Example", 
			"getEnvString(\"PATH\") returns the value of the environment PATH variable."},
		{ "getHashcode",
			"Returns a hashcode value for a key. The value will be between 1 and max (included). This function is immutable.",
			"Example",
			"getHashcode(\"test\", 2) returns 1 or 2."},
		{ "getTimestampNanoSeconds",
			"Returns the value of timestamp in NanoSeconds.",
			"Example",
			"getTimestampNanoSeconds()."},
		{ "rTrim",
			"Returns the value of timestamp in NanoSeconds.",
			"Example",
			"rTrim(\"   test\") returns \"test\"."},
		{ "getMD5",
			"Calculate MD5 hash from string and return 32-byte hexadecimal representation.",
			"Example",
			"getMD5(\"TIBCO Software\"; \"UTF-8\") returns e3f2b373ca278cef3b25d80b178b8238" },
		{ "decryptAES", 
			"Decrypt the input string using AES algorythm",
			"Example",
			"decryptAES(\"VdRsS+fjBWcKDt3sisIo9A==\") returns TIBCO Software" },
		{ "encryptAES", "Encrypt the input string using AES algorythm",
			"Example",
			"encryptAES(\"TIBCO Software\") returns VdRsS+fjBWcKDt3sisIo9A==" },
		{ "getMD5",
			"Calculate MD5 hash from string and return 32-byte hexadecimal representation.",
			"Example",
			"getMD5(\"TIBCO Software\"; \"UTF-8\") returns e3f2b373ca278cef3b25d80b178b8238" },
		{ "decryptAES", 
			"Decrypt the input string using AES algorithm",
			"Example",
			"decryptAES(\"VdRsS+fjBWcKDt3sisIo9A==\") returns TIBCO Software" },
		{ "encryptAES", 
			"Encrypt the input string using AES algorithm",
			"Example",
			"encryptAES(\"TIBCO Software\") returns VdRsS+fjBWcKDt3sisIo9A==" },
		{ "getMD5",
			"Calculate MD5 hash from string and return 32-byte hexadecimal representation.",
			"Example",
			"getMD5(\"TIBCO Software\"; \"UTF-8\") returns e3f2b373ca278cef3b25d80b178b8238" },
		{ "decryptAES", 
			"Decrypt the input string using AES algorithm",
			"Example",
			"decryptAES(\"VdRsS+fjBWcKDt3sisIo9A==\") returns TIBCO Software" },
		{ "encryptAES", 
			"Encrypt the input string using AES algorithm",
			"Example",
			"encryptAES(\"TIBCO Software\") returns VdRsS+fjBWcKDt3sisIo9A==" },
		{"getValue", 
			"Finds the value for this alias and key. Error if not found.",
			"getValue(\"AliasName\",\"key\")","This is the value"},
		{"getKeys", 
			"Return the list of keys for a specific Alias, delimited by a specific field called Separator. Error if not found.",
			"getKeys(\"AliasName\",\"Separator\")","ex: key1__key2__key3"},
		{"getKeysWithValues", 
			"Return the list of keys with there value for a specific Alias, delimited by a 2 specific fields called Separator. two separators, one between the Key and the Value, and the other between KeyValue pairs Error if not found.",
			"getKeysWithValues(\"AliasName\",\"SeparationK_V\",\"SeparationKV_KV\")","ex: key1__val1#key2__val2#"},
		{"getValueOrDefault", 
			"Finds the value for this alias and key. defaultValue is returned if not found.",
			"getValue(\"AliasName\",\"key\",\"default\")","This is the value"},
		{"getValueOrDefaultOrError", 
			"Finds the value for this alias and key. defaultValue is returned if not found.",
			"getValue(\"AliasName\",\"key\",\"default\",\"Alias AliasName is not existing into system\")","This is the value"   },
		{"getValueOrError", 
			"Finds the value for this alias and key. defaultValue is returned if not found.",
			"getValue(\"AliasName\",\"key\",\"Alias AliasName and/or key key are not existing into system\")","This is the value"   },
		{"addValue", 
			"Concat the existing value of an Alias+Key with the new Value. Those values will be delimited by a specific field called Separator. (!) if the Value will be tokenize, choose carefully the same Separator",
			"addValue(\"AliasName\",\"key\",\"newValue\",\"Separator\")","OK"   },
		{"resetValues", 
			"Reset list of values for this alias.",
			"resetValues(\"alias\")","OK"   },
		{"resetOneKeyValue", 
			"delete one pair of 'Key/Value' from the alias",
			"resetOneKeyValue(\"AliasName\",\"key\")","OK"   },
		{"putValue", 
			"Add or replace value for this alias, key",
			"putValue(\"alias\",\"key\", \"value\")","OK"   },
		{"ApplyRegexp", 
			"Apply a regexp match/replace to a string, and return result.", 
			"ApplyRegexp(\"[ab]\", \"Z\", \"abcdabc\")", 
			"ZZcdZZc"},
		{"lTrim", 
			"Trim by the left a string", 
			"lTrim(\"0000ABC\", \"0\")", "ABC"},
		{"toSAPDateString", 
			"Return a SAP formatted date", 
			"toSAPDateString(\"2013-02-15\", \"YYYY-MM-DD\")", 
			"20130215"},
		{"toSAPDeliveryDateString", 
			"Return a SAP formatted delivery date", 
			"toSAPDeliveryDateString(\"2013-02-15\", \"YYYY-MM-DD\")", 
			"15.02.2013"},
		{ "URLEncode",
			"Encode the given string as an URL parameter string and using the given encoding, then return the result.",
			"Example",
			"URLEncode(\"The string �@foo-bar\", \"UTF-8\") returns The+string+%C3%BC%40foo-bar" },
		{"TimestampMSToXMLDate", 
			"Converts a time represented in milleseconds (since January 1, 1970, 00:00:00 GMT) into XML compliant (ISO8601) Date", 
			"Example", 
			"TimestampMSToXMLDate(1332768306418) returns [2012-03-26T15:25:06.418+02:00]."},
		{"XMLDateToTimestampMS", 
			"Converts an XML compliant Date (ISO8601) into time in millisec (since January 1, 1970, 00:00:00 GMT)", 
			"Example", 
			"XMLDateToTimestampMS(\"2012-03-26T15:25:06.418+02:00\") returns [1332768306418]."}
		};

	
	   /**
	    *  same as getUUIDNoDashes : Function generates a 128 bit UUID, based on the java.util.UUID class, but replaces the dashes('-')
	    *  A class that represents an immutable universally unique identifier (UUID). A UUID represents a 128-bit value.
	    *  example: dcd05d5f6b35465eb588e7c72e3112d9
	    *  @return string : UUID without dashes
	    */
		@XPathFunction(helpText = "", parameters = {})
		public static String getGUID()
		{
			return getUUIDNoDashes();
		}
		
		/**
		 * same as getGUIDwithTimeStamp : Function generates a 128 bit UUID + TimeStamp in milliseconds
		 * Based on the java.util.UUID class, but replaces the dashes('-') 
		 * A class that represents an immutable universally unique identifier (UUID). 
		 * A UUID represents a 128-bit value. 
		 * example: dcd05d5f6b35465eb588e7c72e3112d9
		 * 
		 * @return string : UUID without dashes + TimeStamp with milliseconds
		 */
		@XPathFunction(helpText = "", parameters = {})
		public static String getGUIDwithTimeStamp() 
		{
			String valueTimeStamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
			String UUIDD = getUUIDNoDashes();
			return UUIDD+valueTimeStamp;
		}	

	   /**
	    *  Function generates a 128 bit UUID, based on the java.util.UUID class.
	    *  A class that represents an immutable universally unique identifier (UUID). A UUID represents a 128-bit value.
	    *  example: 360b03f4-61b9-4eec-b0d8-8ceb31cbd570
	    *  @return string : UUID without dashes
	    */
		@XPathFunction(helpText = "", parameters = {})
		public static String getUUID()
		{
			try
			{

			   return( java.util.UUID.randomUUID().toString() );
			}
			catch(Exception e)
			{
				throw new RuntimeException(e.toString() + ": " + e.getMessage());
			}
		}


	   /**
	    *  Function generates a 128 bit UUID, based on the java.util.UUID class, but replaces the dashes('-')
	    *  A class that represents an immutable universally unique identifier (UUID). A UUID represents a 128-bit value.
	    *  example: dcd05d5f6b35465eb588e7c72e3112d9
	    *  @return string : UUID without dashes
	    */
		@XPathFunction(helpText = "", parameters = {})
		public static String getUUIDNoDashes()
		{
	      //final int DASH_POS_01=8;
		  //final int DASH_POS_02=13;
		  //final int DASH_POS_03=18;
	      //final int DASH_POS_04=23;

			try
			{
		      String r1=java.util.UUID.randomUUID().toString();
	         char[] arr1=r1.toCharArray();
	         int length=r1.length();
	         char[] arr2 = new char[length-4];

	         // example arr1 : 360b03f4-61b9-4eec-b0d8-8ceb31cbd570
	         System.arraycopy(arr1, 0, arr2, 0, 8);
	         System.arraycopy(arr1, 9, arr2, 8, 4);
	         System.arraycopy(arr1, 14, arr2, 12, 4);
	         System.arraycopy(arr1, 19, arr2, 16, 4);
	         System.arraycopy(arr1, 24, arr2, 20, 12);

	         r1=new String(arr2);
	         return(r1);
			}
			catch(Exception e)
			{
				throw new RuntimeException(e.toString() + ": " + e.getMessage());
			}
		}


		/**
		 * Generate a random float in [0..1]
		 * Example : 0.78
		 * @return float : random number
		 */
		@XPathFunction(helpText = "", parameters = {})
		public static float getRandom()
		{
			try
			{
				return m_random.nextFloat();
			}
			catch(Exception e)
			{
				throw new RuntimeException(e.toString() + ": " + e.getMessage());
			}
		}

		/**
		 * Returns the local Host name where this BW engines runs (cached version).
		 * Example : myhost1
		 * @return
		 */
	  @XPathFunction(helpText = "", parameters = {})
	  public static String getLocalHostname()
		{
			try
			{
				return localHostName;
			}
			catch(Exception e)
			{
				throw new RuntimeException(e.toString() + ": " + e.getMessage());
			}
		}

	/**
	 * "Returns the system-dependent file separator character, represented as a string for convenience.(cached)", "Example", "getSP() returns \"\\\""},
	 * @return string : File separator
	 */
		@XPathFunction(helpText = "", parameters = {})
		public static String getFS()
		{
			try
			{
				return fileSeparator;
			}
			catch(Exception e)
			{
				throw new RuntimeException(e.toString() + ": " + e.getMessage());
			}
		}

	/**
	 * Returns the value of an environment system variable.
	 * Example getEnvString("PATH") would return the value of the environment PATH variable.
	 * @param envVar name of the environment variable to get the value for
	 * @return
	 */
	  @XPathFunction(helpText = "", parameters = { @XPathFunctionParameter(name = "envVar", optional = false, optionalValue = "") })
	  public static String getEnvString(String envVar)
		{
			try
			{
				String res = java.lang.System.getenv(envVar);
				if (res == null) return "";
				else return res;
			}
			catch(Exception e)
			{
				throw new RuntimeException(e.toString() + ": " + e.getMessage());
			}
		}
	  
	  /**
		 * Returns an hashcode 
		 * 
		 * @param key: 
		 * @return int: hashcode, between 0 (including) and max (excluding)
		 */
		
		/**
		 * Returns a hashcode for a string between 1 and a max number
		 * @param key String to hashcode
		 * @param max Max value of the hashcode (included)
		 * @return int Returned hashcode
		 */
	  	@XPathFunction(helpText = "", parameters = {@XPathFunctionParameter(name = "key", optional = false, optionalValue = ""),@XPathFunctionParameter(name = "max", optional = false, optionalValue = "")})
		public static int getHashcode(String key, int max) {
			if(max == 0) {
				throw new RuntimeException("Unable to hash key, zero is a forbidden value");
			}
			
			try {
				int hashcode = key.hashCode();
				if(hashcode < 0) {
					hashcode = hashcode * (-1);
				}
				return (hashcode % max) + 1;
			} catch (Exception e) {
				throw new RuntimeException("Unable to hash string " + key + ", " + e.toString() + ": " + e.getMessage());
			}
		}
		
		/**
		 * Function generates a 128 bit UUID, based on the java.util.UUID class. A
		 * class that represents an immutable universally unique identifier (UUID).
		 * A UUID represents a 128-bit value. example:
		 * 360b03f4-61b9-4eec-b0d8-8ceb31cbd570
		 * 
		 * @return  : UUID without dashes
		 */
	  	@XPathFunction(helpText = "", parameters = {})
		public static String getTimestampNanoSeconds() 
		{
			try 
			{
				String valueTimeStamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
				String valueNanoSeconds = String.valueOf(System.nanoTime());
				
				return (valueTimeStamp + valueNanoSeconds);
			} 
			catch (Exception e) 
			{
				throw new RuntimeException(e.toString() + ": " + e.getMessage());
			}
		}	
	  	
	  	/**
		 * Function removes blank characters at the beginning of String input parameter.
		 * 
		 * @return  : String without leading spaces.
		 */
	  	@XPathFunction(helpText = "", parameters = {})
		public static String rTrim(String string) {
			if(string != null) {
				return string.replaceAll("\\s+$", "");
			}
			return null;
		}
		
		private static String convertToHex(byte[] data) {
			StringBuffer buf = new StringBuffer();
			for (int i = 0; i < data.length; i++) {
				int halfbyte = (data[i] >>> 4) & 0x0F;
				int two_halfs = 0;
				do {
					if ((0 <= halfbyte) && (halfbyte <= 9))
						buf.append((char) ('0' + halfbyte));
					else
						buf.append((char) ('a' + (halfbyte - 10)));
					halfbyte = data[i] & 0x0F;
				} while (two_halfs++ < 1);
			}
			return buf.toString();
		}
		
		/**
		 * Calculate MD5 hash from string and return 32-byte hexadecimal representation.
		 * Example getMD5(\"TIBCO Software\"; \"UTF-8\") returns e3f2b373ca278cef3b25d80b178b8238
		 * @param envVar string to encode
		 * @param enc encoding type
		 * @return
		 */
		@XPathFunction(helpText = "", parameters = {@XPathFunctionParameter(name = "text", optional = false, optionalValue = ""),@XPathFunctionParameter(name = "enc", optional = false, optionalValue = "")})
		public static String getMD5(String text, String enc) {
			try {
				synchronized(md) {
					byte[] md5hash = new byte[32];
					md.update(text.getBytes(enc), 0, text.length());
					md5hash = md.digest();
					return convertToHex(md5hash);
				}
			} catch (Exception e) {
				throw new RuntimeException(e.toString() + ": " + e.getMessage());
			}
		}

		private static Key getsecretKey(String key) {
			byte[] keyInBytes = key.getBytes();
			return new SecretKeySpec(keyInBytes, "AES");
		}
		
		/**
		 * Decrypt the input string using AES algorithm.
		 * Example decryptAES(\"VdRsS+fjBWcKDt3sisIo9A==\") returns TIBCO Software
		 * @param encryptedValue string to decode
		 * @param key encoding key
		 * @return
		 */
		@XPathFunction(helpText = "", parameters = {@XPathFunctionParameter(name = "encryptedValue", optional = false, optionalValue = ""),@XPathFunctionParameter(name = "key", optional = false, optionalValue = "")})
		public static String decryptAES(String encryptedValue, String key) {
			try {
				Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
				c.init(Cipher.DECRYPT_MODE, getsecretKey(key), iv);
				//byte[] decodedValue = new BASE64Decoder().decodeBuffer(encryptedValue);
				byte[] decodedValue =Base64.getDecoder().decode(encryptedValue);
				byte[] decValue = c.doFinal(decodedValue);
				String decryptedValue = new String(decValue);
				return decryptedValue;
			} catch (Exception e) {
				throw new RuntimeException(e.toString() + ": " + e.getMessage());
			}
		}

		/**
		 * Encrypt the input string using AES algorithm.
		 * Example encryptAES(\"TIBCO Software\") returns VdRsS+fjBWcKDt3sisIo9A==
		 * @param valueToEnc string to decode
		 * @param key encoding key
		 * @return
		 */
		@XPathFunction(helpText = "", parameters = {@XPathFunctionParameter(name = "valueToEnc", optional = false, optionalValue = ""),@XPathFunctionParameter(name = "key", optional = false, optionalValue = "")})
		public static String encryptAES(String valueToEnc, String key) {
			try {
				Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
				c.init(Cipher.ENCRYPT_MODE, getsecretKey(key), iv);
				byte[] encValue = c.doFinal(valueToEnc.getBytes());
				String encryptedValue = Base64.getEncoder().encodeToString(encValue);
				return encryptedValue;
			} catch (Exception e) {
				throw new RuntimeException(e.toString() + ": " + e.getMessage());
			}
		}

		@XPathFunction(helpText = "", parameters = {@XPathFunctionParameter(name = "alias", optional = false, optionalValue = ""),@XPathFunctionParameter(name = "key", optional = false, optionalValue = "")})
		public static String getValue( String alias, String key)
		{
	         if(null == localStorage)
	             throw new RuntimeException("TRS001: localStorage is not initialized");

	         Hashtable<String,String> aliasList=localStorage.get(alias);
	         if(null == aliasList)
	             throw new RuntimeException("TRS002: no list of value for this alias;" + alias);

	         String value=aliasList.get(key);
	         if(null == value)
	             throw new RuntimeException("TRS003: no value for this alias/key;" + alias + "/" + key);
	         return value;
		} // end

		@XPathFunction(helpText = "", parameters = {@XPathFunctionParameter(name = "alias", optional = false, optionalValue = ""),@XPathFunctionParameter(name = "Separation", optional = false, optionalValue = "")})
		public static String getKeys(String alias, String Separation) {
			if (null == localStorage)
				throw new RuntimeException(
						"TRS001: localStorage is not initialized");
			Hashtable<String, String> aliasList = localStorage.get(alias);
			if (null == aliasList)
				throw new RuntimeException(
						"TRS002: no list of value for this alias;" + alias);

			Enumeration<String> keysValues = aliasList.keys();
			String Value = keysValues.nextElement();

			while (keysValues.hasMoreElements()) {
				Value = Value + Separation + keysValues.nextElement();
			}

			return Value;
		} // end

		@XPathFunction(helpText = "", parameters = {@XPathFunctionParameter(name = "alias", optional = false, optionalValue = ""),@XPathFunctionParameter(name = "SeparationK_V", optional = false, optionalValue = ""),@XPathFunctionParameter(name = "SeparationKV_KV", optional = false, optionalValue = "")})
		public static String getKeysWithValues(String alias, String SeparationK_V,String SeparationKV_KV ) {
			if (null == localStorage)
				throw new RuntimeException(
						"TRS001: localStorage is not initialized");
			Hashtable<String, String> aliasList = localStorage.get(alias);
			if (null == aliasList)
				throw new RuntimeException(
						"TRS002: no list of value for this alias;" + alias);

			Enumeration<String> keys = aliasList.keys();
//			Enumeration<String> keysValues = aliasList.keys();
			String toReturn = "";

			while (keys.hasMoreElements()) {
				String nextKey=keys.nextElement();
				String nextValue=aliasList.get(nextKey);
				toReturn +=nextKey+SeparationK_V+nextValue+SeparationKV_KV;
			}

			return toReturn;
		} // end
		
		@XPathFunction(helpText = "", parameters = {@XPathFunctionParameter(name = "alias", optional = false, optionalValue = ""),@XPathFunctionParameter(name = "key", optional = false, optionalValue = ""),@XPathFunctionParameter(name = "defautValue", optional = false, optionalValue = "")})
		public static String getValueOrDefault( String alias, String key, String defaultValue)
		{
	         String value=null;
	         if(null == localStorage)
	             throw new RuntimeException("TRS001: localStorage is not initialized");

	         Hashtable<String,String> aliasList=localStorage.get(alias);
	         if(null == aliasList)
	             return defaultValue;;

	         synchronized(aliasList)
	         {
	              value=aliasList.get(key);
	         }
	         if(null == value)
	             return defaultValue;
	         return value;
		} // end

		@XPathFunction(helpText = "", parameters = {@XPathFunctionParameter(name = "alias", optional = false, optionalValue = ""),@XPathFunctionParameter(name = "defautValue", optional = false, optionalValue = ""),@XPathFunctionParameter(name = "errorString", optional = false, optionalValue = "")})
	   	public static String getValueOrDefaultOrError( String alias, String key, String defaultValue, String errorString)
		{
	         String value=null;
	         if(null == localStorage)
	             throw new RuntimeException("TRS001: localStorage is not initialized");

	         Hashtable<String,String> aliasList=localStorage.get(alias);
	         if(null == aliasList)
	             throw new RuntimeException(null == errorString ? "TRS002: no list of value for this alias;" + alias : errorString);

	         synchronized(aliasList)
	         {
	              value=aliasList.get(key);
	         }
	         if(null == value)
	             return defaultValue;
	         return value;
		} // end

		@XPathFunction(helpText = "", parameters = {@XPathFunctionParameter(name = "alias", optional = false, optionalValue = ""),@XPathFunctionParameter(name = "key", optional = false, optionalValue = ""),@XPathFunctionParameter(name = "errorString", optional = false, optionalValue = "")})
		public static String getValueOrError( String alias, String key, String errorString)
		{
	         if(null == localStorage)
	             throw new RuntimeException("TRS001: localStorage is not initialized");

	         Hashtable<String,String> aliasList=localStorage.get(alias);
	         if(null == aliasList)
	             throw new RuntimeException("TRS002: no list of value for this alias;" + alias);

	         String value=aliasList.get(key);
	         if(null == value)
	             throw new RuntimeException(null == errorString ? "TRS003: no value for this alias/key;" + alias + "/" + key : errorString);
	         return value;
		} // end

		@XPathFunction(helpText = "", parameters = {@XPathFunctionParameter(name = "alias", optional = false, optionalValue = ""),@XPathFunctionParameter(name = "key", optional = false, optionalValue = ""),@XPathFunctionParameter(name = "value", optional = false, optionalValue = "")})
		public static String putValue(String alias, String key,String value)
		{
	         if(null == localStorage)
	             throw new RuntimeException("TRS001: localStorage is not initialized");

	         Hashtable<String,String> aliasList=localStorage.get(alias);
	         if(null == aliasList)
	         {
	             aliasList=localStorage.put(alias, new Hashtable<String,String>(10000));
	             aliasList=localStorage.get(alias);
	         }

	         aliasList.put(key,value);
	         return "OK";
		} // end
		
		@XPathFunction(helpText = "", parameters = {@XPathFunctionParameter(name = "alias", optional = false, optionalValue = ""),@XPathFunctionParameter(name = "value", optional = false, optionalValue = ""),@XPathFunctionParameter(name = "Separation", optional = false, optionalValue = "")})
		public static String addValue(String alias, String key,String value,String Separation)
		{
	         
			if(null == localStorage)
	            throw new RuntimeException("TRS001: localStorage is not initialized");

	        Hashtable<String,String> aliasList=localStorage.get(alias);
	        if(null == aliasList)
	            throw new RuntimeException("TRS002: no list of value for this alias;" + alias);

	        String oldValue=aliasList.get(key);
	        if(null == oldValue)
	            throw new RuntimeException("TRS003: no value for this alias/key;" + alias + "/" + key);
	        
	         aliasList.put(key,oldValue+Separation+value);
	         return "OK";
		} // end
	   	
		@XPathFunction(helpText = "", parameters = {@XPathFunctionParameter(name = "alias", optional = false, optionalValue = "")})
		public static String resetValues(String alias)
		{
	         if(null == localStorage)
	             throw new RuntimeException("TRS001: localStorage is not initialized");

	         Hashtable<String,String> aliasList=localStorage.get(alias);
	         if(null == aliasList)
	             return "OK"; //Nothing to do ;)

		 aliasList.clear();
		localStorage.remove(alias);
	         return "OK";
		} // end
	   	
		@XPathFunction(helpText = "", parameters = {@XPathFunctionParameter(name = "alias", optional = false, optionalValue = ""),@XPathFunctionParameter(name = "key", optional = false, optionalValue = "")})
		public static String resetOneKeyValue(String alias,String key)
		{
	         if(null == localStorage)
	             throw new RuntimeException("TRS001: localStorage is not initialized");

	         Hashtable<String,String> aliasList=localStorage.get(alias);
	         if(null == aliasList)
	             return "OK"; //Nothing to do ;)
	         if(null == aliasList.get(key))
	        	 return "OK"; //Nothing to do ;)	
	     
	         aliasList.remove(key);
	         return "OK";
		} // end

		@XPathFunction(helpText = "", parameters = {@XPathFunctionParameter(name = "findRegexp", optional = false, optionalValue = ""),@XPathFunctionParameter(name = "replaceRegexp", optional = false, optionalValue = ""),@XPathFunctionParameter(name = "inputString", optional = false, optionalValue = "")})
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
		
		@XPathFunction(helpText = "", parameters = {@XPathFunctionParameter(name = "expression", optional = false, optionalValue = ""),@XPathFunctionParameter(name = "encoding", optional = false, optionalValue = "")})
		public static final String URLEncode(String expression, String encoding) {
			try {
				return URLEncoder.encode(expression, encoding);
			} catch (Exception e) {
				throw new RuntimeException(e.toString() + ": " + e.getMessage());
			}
		}
		
		@XPathFunction(helpText = "", parameters = {@XPathFunctionParameter(name = "srcString", optional = false, optionalValue = ""),@XPathFunctionParameter(name = "searchString", optional = false, optionalValue = "")})
		public static String lTrim(String srcString, String searchString)
		{				
			String str = srcString;
			while (str.startsWith(searchString)) {
				str = str.substring(1);
			}
			return str;
		}
		
		@XPathFunction(helpText = "", parameters = {@XPathFunctionParameter(name = "srcString", optional = false, optionalValue = ""),@XPathFunctionParameter(name = "srcFormat", optional = false, optionalValue = "")})
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
		
		@XPathFunction(helpText = "", parameters = {@XPathFunctionParameter(name = "srcString", optional = false, optionalValue = ""),@XPathFunctionParameter(name = "srcFormat", optional = false, optionalValue = "")})
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

		@XPathFunction(helpText = "", parameters = {@XPathFunctionParameter(name = "MSdate", optional = false, optionalValue = "")})
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
		
		@XPathFunction(helpText = "", parameters = {@XPathFunctionParameter(name = "sDate", optional = false, optionalValue = "")})
		public static long XMLDateToTimestampMS(String sDate)
		{
			try {
				return javax.xml.datatype.DatatypeFactory.newInstance().newXMLGregorianCalendar(sDate).toGregorianCalendar().getTimeInMillis(); 
	        }
	        catch (Exception ex) {
	            throw new RuntimeException ("Exception while converting XML date to timestamp in milliseconds - " + ex.toString());
	        }		
		}

		public static void main(String[] args) throws Exception {
			System.out.println("getMD5(\"TIBCO Software\", \"UTF-8\") = "
					+ CustomFunctions.getMD5("TIBCO Software", "UTF-8"));
			String password = "Is8OMB45L4Y=gd0S)Y4BAY";
			String passwordEnc = CustomFunctions.encryptAES(password, "AjQnF5ns4XlvgwQZ");
			String passwordDec = CustomFunctions.decryptAES(passwordEnc, "AjQnF5ns4XlvgwQZ");

			System.out.println("Plain Text : " + password);
			System.out.println("Encrypted : " + passwordEnc);
			System.out.println("Decrypted : " + passwordDec);
			
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

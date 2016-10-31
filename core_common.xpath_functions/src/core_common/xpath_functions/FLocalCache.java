package core_common.xpath_functions;

import java.util.*;

public class FLocalCache
{

 private static Hashtable<String,Hashtable<String,String>> localStorage=new Hashtable<String,Hashtable<String,String>>(100);

 public FLocalCache()
 {
     singleton=this;
 }

 protected static FLocalCache singleton=new FLocalCache();

 public static FLocalCache getInstance()
 {
   return singleton;
 }

	public static final String[][] HELP_STRINGS = {
		{"getValue", "Finds the value for this alias and key. Error if not found.",
			"getValue(\"AliasName\",\"key\")","This is the value"   },
		{"getKeys", "Return the list of keys for a specific Alias, delimited by a specific field called Separator. Error if not found.",
				"getKeys(\"AliasName\",\"Separator\")","ex: key1__key2__key3"   },
		{"getKeysWithValues", "Return the list of keys with there value for a specific Alias, delimited by a 2 specific fields called Separator. two separators, one between the Key and the Value, and the other between KeyValue pairs Error if not found.",
					"getKeysWithValues(\"AliasName\",\"SeparationK_V\",\"SeparationKV_KV\")","ex: key1__val1#key2__val2#"   },
		{"getValueOrDefault", "Finds the value for this alias and key. defaultValue is returned if not found.",
			"getValue(\"AliasName\",\"key\",\"default\")","This is the value"   },
		{"getValueOrDefaultOrError", "Finds the value for this alias and key. defaultValue is returned if not found.",
			"getValue(\"AliasName\",\"key\",\"default\",\"Alias AliasName is not existing into system\")","This is the value"   },
		{"getValueOrError", "Finds the value for this alias and key. defaultValue is returned if not found.",
			"getValue(\"AliasName\",\"key\",\"Alias AliasName and/or key key are not existing into system\")","This is the value"   },
		{"addValue", "Concat the existing value of an Alias+Key with the new Value. Those values will be delimited by a specific field called Separator. (!) if the Value will be tokenize, choose carefully the same Separator",
				"addValue(\"AliasName\",\"key\",\"newValue\",\"Separator\")","OK"   },
		{"resetValues", "Reset list of values for this alias.",
			"resetValues(\"alias\")","OK"   },
		{"resetOneKeyValue", "delete one pair of 'Key/Value' from the alias",
				"resetOneKeyValue(\"AliasName\",\"key\")","OK"   },
		{"putValue", "Add or replace value for this alias, key",
			"putValue(\"alias\",\"key\", \"value\")","OK"   }
	}; // end HELP_STRINGS

/*
	public static String getValue( String key)
	{
               return localStorage == null ? "ORI_"+key : localStorage.get(key);
	} // end
*/

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
	
	public static String getKeysWithValues(String alias, String SeparationK_V,String SeparationKV_KV ) {
		if (null == localStorage)
			throw new RuntimeException(
					"TRS001: localStorage is not initialized");
		Hashtable<String, String> aliasList = localStorage.get(alias);
		if (null == aliasList)
			throw new RuntimeException(
					"TRS002: no list of value for this alias;" + alias);

		Enumeration<String> keys = aliasList.keys();
//		Enumeration<String> keysValues = aliasList.keys();
		String toReturn = "";

		while (keys.hasMoreElements()) {
			String nextKey=keys.nextElement();
			String nextValue=aliasList.get(nextKey);
			toReturn +=nextKey+SeparationK_V+nextValue+SeparationKV_KV;
		}

		return toReturn;
	} // end
	
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
   	
   	
   	public static void main(String args[]){}
}
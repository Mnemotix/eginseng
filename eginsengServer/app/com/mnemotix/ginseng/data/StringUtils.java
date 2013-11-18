package com.mnemotix.ginseng.data;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class StringUtils {

	  /** Pattern to replace diacritical marks */
	  private final static Pattern DIACRITICAL_MARKS_PATTERN = 
	    Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
	  
	  
	  public static String removeDiacriticalMarks(String s) {
	    return DIACRITICAL_MARKS_PATTERN.matcher(Normalizer.normalize(s, Normalizer.Form.NFD)).replaceAll("");
	  }
	  
	  public static String removeUselessSpace(String s){
	    return s.replaceAll("\\s+", " ").trim();
	  }
	  
	  public static String removeSpace(String s){
	    return s.replaceAll("\\s+", "").trim();
	  }
	  
	  public static String normalize(String s) {
	    return removeUselessSpace(removeDiacriticalMarks(s.toLowerCase()).replace("œ", "oe").replace("æ", "ae"));
	  }
	
}




package ch.hesge.sedypro.test;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.hesge.sedypro.utils.StringUtils;

public class Regexp {

	public static void main(String[] args) {

		String regexp = "(?<wordtype>[^:]*):(?<word>[^,]*),?";
		Pattern pattern = Pattern.compile(regexp);

//		Matcher patternMatcher = pattern.matcher("a:initialize,b:init,c:InitView");
//		
//		while (patternMatcher.find()) {
//			
//			String type = patternMatcher.group("wordtype");
//			String word = patternMatcher.group("word");
//			System.out.println(type + ":" + word);
//		}

		String textLine = "a:initialize,b:init,c:InitView";
	
		for (String entry : StringUtils.toStringList(textLine,  ",")) {
			
			List<String> wordEntry = StringUtils.toStringList(entry, ":");
			
			System.out.println("word = " + wordEntry.get(0) + ":" + wordEntry.get(1));
		}
	
	}

}

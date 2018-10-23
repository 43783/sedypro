package ch.hesge.sedypro.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * 
 * http://ws4jdemo.appspot.com/?mode=s&s1=System+displays+the+application+start+view&w1=&s2=Screen+init+Components&w2=
 *
 */
public class HttpRequestTest {

	public static void main(String[] args) throws Exception {
		
		URL yahoo = new URL("https://babelfy.io/v1/disambiguate?text=system%20display%20start%20screen&lang=en&key=key");
		URLConnection yc = yahoo.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
		String inputLine;

		while ((inputLine = in.readLine()) != null)
			System.out.println(inputLine);
		in.close();
		
	}
}
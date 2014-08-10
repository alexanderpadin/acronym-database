package app.programming.acronym;


import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class getData {

	public String postData() {
	
		String result = null;
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpPost httppost = new HttpPost("http://apadn.org/data.json");
	    
	    try {          
	        HttpResponse response = httpclient.execute(httppost);
	        HttpEntity entity = response.getEntity();
		    result = EntityUtils.toString(entity);

	    } catch (ClientProtocolException e) {
	    	result = null;
	    } catch (IOException e) {
	    	result = null;
	    }
	    return result;
	} 

}

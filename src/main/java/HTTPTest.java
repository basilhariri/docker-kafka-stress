import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.HttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.HttpEntity;
import java.nio.charset.StandardCharsets;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import java.util.Scanner;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.ArrayList;
import org.apache.http.NameValuePair;

public class HTTPTest extends Test
{
    private final String CONNECTION_STRING;
    private final String TOPIC;
    private final String NAMESPACE;

    public HTTPTest(String connStr, String topic)
    {
        this.CONNECTION_STRING = connStr;
        this.TOPIC = topic;
        this.NAMESPACE = connStr.substring(connStr.indexOf("sb://") + 5, connStr.indexOf("."));
    }

    public boolean runSendTests() throws Exception
    {
        try 
        {
            HttpClient httpclient = HttpClients.createDefault();
            HttpPost post = new HttpPost("https://" + this.NAMESPACE + ".servicebus.windows.net/" + this.TOPIC + "/messages?timeout=60&api-version=2014-01");
            
            // Request parameters and other properties.
            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
            params.add(new BasicNameValuePair("Location", "Redmond"));
            params.add(new BasicNameValuePair("Temperature", "37.0"));
            post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            //Execute and get the response.
            HttpResponse response = httpclient.execute(post);
            HttpEntity entity = response.getEntity();
            
            if (entity != null) 
            {
                try (InputStream instream = entity.getContent()) 
                {
                    System.out.println("Received HTTP response to PUT request: ");
                    String text = null;
                    try (Scanner scanner = new Scanner(instream, StandardCharsets.UTF_8.name())) {
                        text = scanner.useDelimiter("\\A").next();
                    }
                    System.out.println("Response:" + text);
                }
            }
        }
        catch (UnsupportedEncodingException e)
        {
            System.out.println("UnsupportedEncodingException: " + e);
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    public boolean runReceiveTests()
    {
        //TODO
        return true;
    }

    public boolean runManagementTests()
    {
        //TODO
        return true;
    }
}
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.FileReader;
import java.sql.Timestamp;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.Date;

public class DataReporter implements Runnable 
{
    //Constants
    private String CONFIG_PATH;
    //Instance
    private Producer<Long, String> producer;

    public DataReporter(final Producer<Long, String> producer, String CONFIG_PATH)
    {
        this.producer = producer;
        this.CONFIG_PATH = CONFIG_PATH;
    }

    public String createRandomString(int sentCount, int length)
    {
        try
        {
            StringBuilder sb = new StringBuilder();
            sb.append(sentCount + " ");
            while(sb.length() < length)
            {
                sb.append(" ");
            }
            return sb.toString();
        }
        catch (Exception e)
        {
            System.out.println(e);
            return null;
        }
    }

    @Override
    public void run() 
    {
        int sentCount = 0;
        while (true) 
        {
            try 
            {
                long time = System.currentTimeMillis();
                String s = createRandomString(sentCount, 1000);
                System.out.println(new Date(time) + " " + s.substring(0, 30));
                Properties props = new Properties();
                props.load(new FileReader(CONFIG_PATH));
                final ProducerRecord<Long, String> record = new ProducerRecord<Long, String>(props.getProperty("topic"), time, s);
                producer.send(record).get();
                sentCount++;

            } 
            catch (InterruptedException e) 
            {
                Thread.interrupted();
                break;
            } 
            catch (Exception e)
            {
                System.out.println(e);
            }
        }
    }
}

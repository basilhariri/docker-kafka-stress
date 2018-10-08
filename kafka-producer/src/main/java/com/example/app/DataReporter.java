import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.FileReader;
import java.sql.Timestamp;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.Date;

public class DataReporter implements Runnable 
{
    //Instance
    private Producer<Long, String> producer;

    public DataReporter(final Producer<Long, String> producer)
    {
        this.producer = producer;
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
                final ProducerRecord<Long, String> record = new ProducerRecord<Long, String>(System.getenv("TOPIC"), time, s);
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

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.FileReader;
import java.sql.Timestamp;
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
        double sumLatency = 0.0;
        String topic = System.getenv("TOPIC");
        System.out.println("Topic = " + topic);
        while (true) 
        {
            try 
            {
                //Create payload
                String s = createRandomString(sentCount, 1000);
                
                //Create message
                long time = System.currentTimeMillis();
                final ProducerRecord<Long, String> record = new ProducerRecord<Long, String>(topic, time, s);                
                
                //Send record and calculate latency of send
                long startTime = System.currentTimeMillis();
                producer.send(record).get();
                long stopTime = System.currentTimeMillis();
                sumLatency += stopTime - startTime;
                
                //Print latency occasionally
                if(sentCount % 1000 == 0 && sentCount > 0)
                {
                    System.out.printf("tid = %d, avg latency = %6.4fms (updated every 1000 events)\n", Thread.currentThread().getId(), sumLatency / sentCount);
                }
                sentCount++;
            } 
            catch (InterruptedException e) 
            {
                Thread.interrupted();
                break;
            } 
            catch (Exception e)
            {
                System.out.println("Exception in DataReporter:run " + e.getMessage());
            }
        }
    }
}

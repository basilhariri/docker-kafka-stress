import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.FileReader;
import java.sql.Timestamp;
import java.util.concurrent.ExecutionException;
import java.util.Date;

/**
 * A "data reporter" that creates and sends test messages indefinitely using a provided Kafka producer
 */
public class DataReporter implements Runnable 
{
    //The Kafka producer to send messages with
    private Producer<Long, String> producer;

    public DataReporter(final Producer<Long, String> producer)
    {
        this.producer = producer;
    }

    /**
     * Creates a test message that is padded to a certain length
     * @param messageId, this message's unique ID (only unique within a thread)
     * @param length, the desired length of the message (achieved by padding with spaces)
     */
    public String createPaddedMessage(long messageId, int length)
    {
        try
        {
            StringBuilder sb = new StringBuilder();
            sb.append(messageId + " ");

            //Use spaces to pad the message
            while(sb.length() < length)
            {
                sb.append(" ");
            }
            return sb.toString();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * Send 1Kb messages from a single Kafka producer to a single configured topic indefinitely
     */
    @Override
    public void run() 
    {
        long sentCount = 0;
        double sumLatency = 0.0;
        String topic = System.getenv("TOPIC");
        System.out.println("Topic = " + topic);
        while (true) 
        {
            try 
            {
                //Create payload
                String data = createPaddedMessage(sentCount, 1000);
                
                //Create message
                long time = System.currentTimeMillis();
                final ProducerRecord<Long, String> record = new ProducerRecord<Long, String>(topic, time, data);                
                
                //Send record (asynchronously) and calculate latency of send
                long startTime = System.currentTimeMillis();
                producer.send(record);
                long stopTime = System.currentTimeMillis();
                sumLatency += stopTime - startTime;
                
                //Print latency (every 1000 events, otherwise overwhelming)
                if(sentCount % 1000 == 0 && sentCount > 0)
                {
                    System.out.printf("tid = %d, avg latency = %6.4fms (refreshed every 1000 events)\n", Thread.currentThread().getId(), sumLatency / 1000);
                    sumLatency = 0.0;
                }
                sentCount++;
            } 
            catch (Exception e)
            {
                System.out.println("Exception in DataReporter:run " + e.getMessage());
            }
        }
    }
}

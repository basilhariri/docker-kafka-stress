//Copyright (c) Microsoft Corporation. All rights reserved.
//Licensed under the MIT License.
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import java.io.FileReader;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestProducer 
{
    private final static boolean KAFKA = Boolean.parseBoolean(System.getenv("KAFKA"));
    private final static int NUM_THREADS = Integer.parseInt(System.getenv("NUM_THREADS"));
    private final static String CONNECTION_STRING = System.getenv("CONNECTION_STRING");
    private final static String TOPIC = System.getenv("TOPIC");
    private final static String FQDN = CONNECTION_STRING.substring(CONNECTION_STRING.indexOf("sb://") + 5, CONNECTION_STRING.indexOf("/;")) + ":9093";

    public static void main(String... args) throws Exception 
    {
        final ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);
        if(KAFKA)
        {
            final Producer<Long, String> producer = createKafkaProducer();
            //Produce from NUM_THREADS threads
            for (int i = 0; i < NUM_THREADS; i++)
            {
                executorService.execute(new CarDataReporter(producer));
            }
        }
        else
        {
            final EventHubClient ehClient = EventHubClient.createFromConnectionStringSync(String.format("%s;EntityPath=%s", CONNECTION_STRING, TOPIC));
            //Produce from NUM_THREADS threads
            for (int i = 0; i < NUM_THREADS; i++)
            {
                executorService.execute(new CarDataReporter(ehClient));
            }
        }
    }

    private static Producer<Long, String> createKafkaProducer() 
    {
        try 
        {
            Properties properties = new Properties();      
            properties.setProperty("bootstrap.servers", FQDN);
            properties.put("security.protocol", "SASL_SSL");
            properties.put("sasl.mechanism", "PLAIN");
            properties.put("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"$ConnectionString\" password=\"" + CONNECTION_STRING + "\";");
            properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());            
            properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());        
            for (Object s : properties.keySet())                                                                   
            {                                                                                                      
                System.out.println(s + ":" + properties.get(s));                                                   
            }
            return new KafkaProducer<>(properties);
        } 
        catch (Exception e)
        {
            System.out.println(new Date(System.currentTimeMillis()) + " Exception: " + e);
            System.exit(3);
            return null;
        }
    }
}



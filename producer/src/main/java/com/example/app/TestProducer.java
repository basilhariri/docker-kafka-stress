//Copyright (c) Microsoft Corporation. All rights reserved.
//Licensed under the MIT License.
import org.apache.kafka.clients.producer.KafkaProducer;                                                            
import org.apache.kafka.clients.producer.Producer;                                                                 
import org.apache.kafka.clients.producer.ProducerConfig;                                                           
import org.apache.kafka.clients.producer.ProducerRecord;                                                           
import org.apache.kafka.common.serialization.LongSerializer;                                                       
import org.apache.kafka.common.serialization.StringSerializer;                                                     
import org.slf4j.Logger;                                                                                           
import org.slf4j.LoggerFactory;                                                                                                                                                                                      
import java.io.FileReader;                                                                                         
import java.util.Properties;                                                                                       
import java.util.concurrent.CompletableFuture;                                                                     
import java.util.concurrent.ExecutorService;                                                                       
import java.util.concurrent.Executors;                                                                             
                                                                                                                   
public class TestProducer {
    private final static int NUM_THREADS = 5;
    private final static String CONFIG_PATH = "src/main/resources/producer.config";
    private final static String CONNECTION_STRING = System.getenv("CONNECTION_STRING");
    private final static String FQDN = CONNECTION_STRING.substring(CONNECTION_STRING.indexOf("sb://") + 5, CONNECTION_STRING.indexOf("/;")) + ":9093";

    public static void main(String... args) throws Exception {
        //Create Kafka Producer
//	final String CONNECTION_STRING = System.getenv("CONNECTION_STRING");
//	final String FQDN = CONNECTION_STRING.substring(CONNECTION_STRING.indexOf("sb://") + 5, CONNECTION_STRING.indexOf("/;")) + ":9093";
	//System.out.println("CONNECTION STRING = " + CONNECTION_STRING + "\nFQDN = " + FQDN);
	//System.exit(1);
        final Producer<Long, String> producer = createProducer();
        final ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);
                                                                                                                   
        //Run several CDRs                                                                                         
        for (int i = 0; i < NUM_THREADS; i++)
            executorService.execute(new DataReporter(producer, CONFIG_PATH));                                      
    }                                                                                                              
                                                                                                                   
    private static final Logger logger =                                                                           
            LoggerFactory.getLogger(DataReporter.class);                                                           
                                                                                                                   
    private static Producer<Long, String> createProducer() {                                                       
        try {                                                                                                      
            Properties properties = new Properties();                                                              
            properties.load(new FileReader(CONFIG_PATH));
	    properties.put("sasl.jaas.config", properties.getProperty("sasl.jaas.config").replace("{YOUR.EVENTHUBS.CONNECTION.STRING}", CONNECTION_STRING));
	    properties.put("bootstrap.servers", FQDN);
	    properties.put(ProducerConfig.CLIENT_ID_CONFIG, "java-producer");                                
            properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());            
            properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());        
            for (Object s : properties.keySet())                                                                   
            {                                                                                                      
                System.out.println(s + ":" + properties.get(s));                                                   
            }                                                                                                      
            return new KafkaProducer<>(properties);                                                                
        } catch (Exception e){                                                                                     
            System.out.println("Exception: " + e);                                                                 
            System.exit(1);                                                                                        
            return null;                                                                                           
        }                                                                                                          
    }                                                                                                              
}

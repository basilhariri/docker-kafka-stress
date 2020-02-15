import java.util.Properties;
import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

public class KafkaTest extends Test
{
    private String CONNECTION_STRING;
    private String TOPIC;
    private String FQDN;

    public KafkaTest(String connStr, String topic)
    {
        CONNECTION_STRING = connStr;
        TOPIC = topic;
        FQDN = CONNECTION_STRING.substring(CONNECTION_STRING.indexOf("sb://") + 5, CONNECTION_STRING.indexOf("/;")) + ":9093";
    }

    public boolean runSendTests() throws Exception
    {
        System.out.println("KAFKA: Send tests");
        Producer<Long, String> producer = createKafkaProducer(this.CONNECTION_STRING, this.FQDN);
        if(producer != null)
        {
            System.out.println("KAFKA: Running send tests");
            ProducerRecord<Long, String> record = new ProducerRecord<Long,String>(this.TOPIC, TEST_MESSAGE);
            producer.send(record).get();
            return true;
        }
        return false;
    }
    
    public boolean runReceiveTests() throws Exception
    {
        Consumer<Long, String> consumer = createKafkaConsumer(this.CONNECTION_STRING, this.FQDN);
        if(consumer != null)
        {
            System.out.println("KAFKA: Running send tests");
            consumer.subscribe(Collections.singleton(this.TOPIC));
            consumer.poll(Duration.ofSeconds(10));
            return true;
        }
        return false;
    }

    public boolean runManagementTests() throws Exception
    {
        AdminClient admin = createAdminClient(this.CONNECTION_STRING, this.FQDN);
        if(admin != null)
        {
            createTopicsTest(admin);
            listTopicsTest(admin);
            return true;
        }
        return false;
    }

    public void createTopicsTest(AdminClient admin)
    {
        System.out.println("KAFKA: Running createTopics test");
        admin.createTopics(Collections.singleton(new NewTopic("createdTopic" + UUID.randomUUID(), 2, (short) 0)));
    }

    public void listTopicsTest(AdminClient admin)
    {
        System.out.println("KAFKA: Running listTopics test");
        System.out.println(admin.listTopics());
    }

    private static Producer<Long, String> createKafkaProducer(String connStr, String fqdn) 
    {
        try 
        {
            System.out.println("KAFKA: Creating Kafka producer...");
            Properties properties = new Properties();
            properties.setProperty("bootstrap.servers", fqdn);
            properties.put("security.protocol", "SASL_SSL");
            properties.put("sasl.mechanism", "OAUTHBEARER");
            properties.put("sasl.jaas.config", "org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required;");
            properties.put("sasl.login.callback.handler.class", "KafkaAuthenticateCallbackHandler");
            properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
            properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            System.out.println("KAFKA: Properties used for Kafka producer:");
            for (Object s : properties.keySet())
            {
                System.out.println("\t" + s + ":" + properties.get(s));
            }
            return new KafkaProducer<>(properties);
        }
        catch (Exception e)
        {
            System.out.println("KAFKA: Kafka producer creation failed: " + e);
            System.out.println("KAFKA: Skipping send tests.");
            return null;
        }
    }

    private static Consumer<Long, String> createKafkaConsumer(String connStr, String fqdn)
    {
        try 
        {
            System.out.println("KAFKA: Creating Kafka consumer...");
            Properties properties = new Properties();
            properties.setProperty("bootstrap.servers", fqdn);
            properties.put("security.protocol", "SASL_SSL");
            properties.put("sasl.mechanism", "OAUTHBEARER");
            properties.put("sasl.jaas.config", "org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required;");
            properties.put("sasl.login.callback.handler.class", "KafkaAuthenticateCallbackHandler");
            properties.put("auto.offset.reset", "earliest");
            properties.put("request.timeout.ms", "60000");
            properties.put("session.timeout.ms", "30000");
            properties.put(ConsumerConfig.CLIENT_ID_CONFIG, "KafkaExampleConsumer#" + UUID.randomUUID());
            properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
            properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            final Consumer<Long, String> consumer = new KafkaConsumer<>(properties);
            System.out.println("KAFKA: Properties used for Kafka consumer:");
            for (Object s : properties.keySet())
            {
                System.out.println("\t" + s + ":" + properties.get(s));
            }
            return consumer;
        }
        catch (Exception e)
        {
            System.out.println("KAFKA: Kafka consumer creation failed: " + e);
            System.out.println("KAFKA: Skipping receive tests.");
            return null;
        }
    }

    private static AdminClient createAdminClient(String connStr, String fqdn)
    {
        try 
        {
            System.out.println("KAFKA: Creating Kafka AdminClient...");
            Properties properties = new Properties();
            properties.setProperty("bootstrap.servers", fqdn);
            properties.put("security.protocol", "SASL_SSL");
            properties.put("sasl.mechanism", "OAUTHBEARER");
            properties.put("sasl.jaas.config", "org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required;");
            properties.put("sasl.login.callback.handler.class", "KafkaAuthenticateCallbackHandler");
            System.out.println("KAFKA: Properties used for admin client:");
            for (Object s : properties.keySet())
            {
                System.out.println("\t" + s + ":" + properties.get(s));
            }
            return AdminClient.create(properties);
        }
        catch (Exception e)
        {
            System.out.println("KAFKA: Kafka AdminClient creation failed: " + e);
            System.out.println("KAFKA: Skipping management tests.");
            return null;
        }
    }
}
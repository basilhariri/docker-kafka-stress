import java.util.Properties;
import java.util.Collections;
import java.util.UUID;
import java.time.Duration;
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
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;


public class KafkaTest extends Test
{
    private String TOPIC;
    private String FQDN;
    private Producer<Long, String> producer;
    private Consumer<Long, String> consumer;
    private AdminClient admin;

    public KafkaTest(String ns, String topic)
    {
        FQDN = ns + ".servicebus.windows.net:9093";
        TOPIC = topic;
        try 
        {
            testSetup();
        } 
        catch(Exception e){}
    }

    public void testSetup() throws Exception 
    {
        this.producer = createKafkaProducer(FQDN);
        this.consumer = createKafkaConsumer(FQDN);
        this.admin = createAdminClient(FQDN);
    }

    public void testTearDown() throws Exception
    {
        if(this.producer != null)
        {
            this.producer.close();
        }
        if(this.consumer != null)
        {
            this.consumer.close();
        }
        if(this.admin != null)
        {
            this.admin.close();
        }
    }

    public boolean runSendTests() throws Exception
    {
        RunTests.printThreadSafe("KAFKA: Send tests");
        if(producer != null)
        {
            RunTests.printThreadSafe("KAFKA: Running send tests");
            ProducerRecord<Long, String> record = new ProducerRecord<Long,String>(this.TOPIC, TEST_MESSAGE);
            producer.send(record).get();
            return true;
        }
        return false;
    }
    
    public boolean runReceiveTests() throws Exception
    {
        RunTests.printThreadSafe("KAFKA: Receive tests");
        if(consumer != null)
        {
            RunTests.printThreadSafe("KAFKA: Running receive tests");
            consumer.subscribe(Collections.singleton(this.TOPIC));
            consumer.poll(Duration.ofSeconds(10));
            return true;
        }
        return false;
    }

    public boolean runManagementTests() throws Exception
    {
        RunTests.printThreadSafe("KAFKA: Management tests");
        if(admin != null)
        {
            String topic = "createdTopic" + UUID.randomUUID();
            createTopicsTest(admin, topic);
            listTopicsTest(admin);
            deleteTopicsTest(admin, topic);
            return true;
        }
        return false;
    }

    public void createTopicsTest(AdminClient admin, String topicName)
    {
        RunTests.printThreadSafe("KAFKA: Running createTopics test");
        admin.createTopics(Collections.singleton(new NewTopic(topicName, 2, (short) 0)));
    }

    public void deleteTopicsTest(AdminClient admin, String topicName)
    {
        RunTests.printThreadSafe("KAFKA: Running deleteTopics test");
        admin.deleteTopics(Collections.singleton(topicName));
    }

    public void listTopicsTest(AdminClient admin) throws Exception
    {
        RunTests.printThreadSafe("KAFKA: Running listTopics test");
        RunTests.printThreadSafe(admin.listTopics().names().get().iterator().next());
    }

    private static Producer<Long, String> createKafkaProducer(String fqdn) 
    {
        try 
        {
            RunTests.printThreadSafe("KAFKA: Creating Kafka producer...");
            Properties properties = new Properties();
            properties.setProperty("bootstrap.servers", fqdn);
            properties.put("security.protocol", "SASL_SSL");
            properties.put("sasl.mechanism", "OAUTHBEARER");
            properties.put("sasl.jaas.config", "org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required;");
            properties.put("sasl.login.callback.handler.class", "KafkaAuthenticateCallbackHandler");
            properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
            properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            RunTests.printThreadSafe("KAFKA: Properties used for Kafka producer:");
            for (Object s : properties.keySet())
            {
                RunTests.printThreadSafe("\t" + s + ":" + properties.get(s));
            }
            return new KafkaProducer<>(properties);
        }
        catch (Exception e)
        {
            RunTests.printThreadSafe("KAFKA: Kafka producer creation failed: " + e);
            RunTests.printThreadSafe(e);
            RunTests.printThreadSafe("KAFKA: Skipping send tests.");
            return null;
        }
    }

    private static Consumer<Long, String> createKafkaConsumer(String fqdn)
    {
        try 
        {
            RunTests.printThreadSafe("KAFKA: Creating Kafka consumer...");
            Properties properties = new Properties();
            properties.put("bootstrap.servers", fqdn);
            properties.put("security.protocol", "SASL_SSL");
            properties.put("sasl.mechanism", "OAUTHBEARER");
            properties.put("sasl.jaas.config", "org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required;");
            properties.put("sasl.login.callback.handler.class", "KafkaAuthenticateCallbackHandler");
            properties.put("auto.offset.reset", "earliest");
            properties.put("request.timeout.ms", "60000");
            properties.put("session.timeout.ms", "30000");
            properties.put("group.id", "$Default");
            properties.put(ConsumerConfig.CLIENT_ID_CONFIG, "KafkaExampleConsumer#" + UUID.randomUUID());
            properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
            properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            RunTests.printThreadSafe("KAFKA: Properties used for Kafka consumer:");
            for (Object s : properties.keySet())
            {
                RunTests.printThreadSafe("\t" + s + ":" + properties.get(s));
            }
            final Consumer<Long, String> consumer = new KafkaConsumer<>(properties);
            return consumer;
        }
        catch (Exception e)
        {
            RunTests.printThreadSafe("KAFKA: Kafka consumer creation failed: " + e);
            RunTests.printThreadSafe(e);
            RunTests.printThreadSafe("KAFKA: Skipping receive tests.");
            return null;
        }
    }

    private static AdminClient createAdminClient(String fqdn)
    {
        try 
        {
            RunTests.printThreadSafe("KAFKA: Creating Kafka AdminClient...");
            Properties properties = new Properties();
            properties.setProperty("bootstrap.servers", fqdn);
            properties.put("security.protocol", "SASL_SSL");
            properties.put("sasl.mechanism", "OAUTHBEARER");
            properties.put("sasl.jaas.config", "org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required;");
            properties.put("sasl.login.callback.handler.class", "KafkaAuthenticateCallbackHandler");
            RunTests.printThreadSafe("KAFKA: Properties used for admin client:");
            for (Object s : properties.keySet())
            {
                RunTests.printThreadSafe("\t" + s + ":" + properties.get(s));
            }
            return AdminClient.create(properties);
        }
        catch (Exception e)
        {
            RunTests.printThreadSafe("KAFKA: Kafka AdminClient creation failed: " + e);
            RunTests.printThreadSafe(e);
            RunTests.printThreadSafe("KAFKA: Skipping management tests.");
            return null;
        }
    }
}
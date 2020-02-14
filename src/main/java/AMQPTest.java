import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventPosition;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import java.net.URI;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;

public class AMQPTest extends Test
{

    private EventHubClient ehClient;
    private URI NAMESPACE;
    private final String CONNECTION_STRING;
    private final String TOPIC;
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    public AMQPTest(String cs, String topic)
    {
        CONNECTION_STRING = cs;
        TOPIC = topic;
        try
        {
            NAMESPACE = new URI(CONNECTION_STRING.substring(CONNECTION_STRING.indexOf("sb://") + 5, CONNECTION_STRING.indexOf(".")));
            final ConnectionStringBuilder connStr = new ConnectionStringBuilder()
            .setEndpoint(this.NAMESPACE)
            .setEventHubName(this.TOPIC)
            .setAuthentication(ConnectionStringBuilder.MANAGED_IDENTITY_AUTHENTICATION);

            this.ehClient = EventHubClient.createFromConnectionStringSync(connStr.toString(), executorService);
            System.out.println("AMQP: Creating EventHub client...");
        }
        catch (Exception e)
        {
            this.ehClient = null;
            System.out.println("AMQP: Failed to create EventHub client: " + e);
            System.out.println("AMQP: Skipping all AMQP tests due to client creation failure");
        }
    }

    public boolean runSendTests() throws Exception
    {
        if(ehClient != null)
        {
            System.out.println("AMQP: Sending...");
            ehClient.sendSync(EventData.create(TEST_MESSAGE.getBytes()));
            return true;
        }
        return false;
    }
    
    public boolean runReceiveTests() throws Exception
    {
        if(ehClient != null)
        {
            System.out.println("AMQP: Receiving...");
            PartitionReceiver pr = ehClient.createReceiver(EventHubClient.DEFAULT_CONSUMER_GROUP_NAME, "0", EventPosition.fromStartOfStream()).get();
            pr.receiveSync(1);
            return true;
        }
        return false;
    }

    public boolean runManagementTests() throws Exception
    {
        if(ehClient != null)
        {
            System.out.println("AMQP: Getting runtime information...");
            ehClient.getRuntimeInformation();
            System.out.println("AMQP: Getting partition runtime information...");
            ehClient.getPartitionRuntimeInformation("0");
            return true;
        }
        return false;
    }
}
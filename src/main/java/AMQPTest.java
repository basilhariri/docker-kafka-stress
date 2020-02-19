import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventPosition;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.eventhubs.TimeoutException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;

public class AMQPTest extends Test
{

    private EventHubClient ehClient;
    private final String NAMESPACE;
    private final String TOPIC;
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    public AMQPTest(String cs, String topic)
    {
        this.TOPIC = topic;
        this.NAMESPACE = cs.substring(cs.indexOf("sb://") + 5, cs.indexOf("."));
        try
        {
            final ConnectionStringBuilder connStr = new ConnectionStringBuilder()
            .setNamespaceName(this.NAMESPACE)
            .setEventHubName(this.TOPIC)
            .setAuthentication(ConnectionStringBuilder.MANAGED_IDENTITY_AUTHENTICATION);

            this.ehClient = EventHubClient.createFromConnectionStringSync(connStr.toString(), executorService);
            System.out.println("AMQP: Creating EventHub client...");
        }
        catch (Exception e)
        {
            this.ehClient = null;
            System.out.println("AMQP: Failed to create EventHub client: ");
            e.printStackTrace();
            System.out.println("AMQP: Skipping all AMQP tests due to client creation failure");
        }
        finally
        {
            executorService.shutdown();
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
            if(ehClient.getRuntimeInformation() == null)
            {
                throw new TimeoutException("GetRuntimeInfo timed out");
            }
            System.out.println("AMQP: Getting partition runtime information...");
            if(ehClient.getPartitionRuntimeInformation("0") == null)
            {
                throw new TimeoutException("GetPartitionRuntimeInfo timed out");
            }
            return true;
        }
        return false;
    }
}
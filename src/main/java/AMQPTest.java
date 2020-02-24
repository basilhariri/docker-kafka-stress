import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventPosition;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.eventhubs.PartitionRuntimeInformation;
import com.microsoft.azure.eventhubs.TimeoutException;

import java.util.concurrent.ScheduledExecutorService;

public class AMQPTest extends Test
{
    private EventHubClient ehClient;
    private final String NAMESPACE;
    private final String TOPIC;

    public AMQPTest(String ns, String topic, ScheduledExecutorService executorService)
    {
        this.TOPIC = topic;
        this.NAMESPACE = ns;
        RunTests.printThreadSafe("AMQP: Creating EventHub client...");
        try
        {
            final ConnectionStringBuilder connStr = new ConnectionStringBuilder()
                .setNamespaceName(this.NAMESPACE)
                .setEventHubName(this.TOPIC)
                .setAuthentication(ConnectionStringBuilder.MANAGED_IDENTITY_AUTHENTICATION);
            this.ehClient = EventHubClient.createFromConnectionStringSync(connStr.toString(), executorService);
            
            //String cs = "connection string";
            //this.ehClient = EventHubClient.createFromConnectionStringSync(cs, executorService);
        }
        catch (Exception e)
        {
            this.ehClient = null;
            RunTests.printThreadSafe("AMQP: Failed to create EventHub client: ");
            RunTests.printThreadSafe(e);
            RunTests.printThreadSafe("AMQP: Skipping all AMQP tests due to client creation failure");
            executorService.shutdown();
        }
    }

    public boolean runSendTests() throws Exception
    {
        RunTests.printThreadSafe("AMQP: Sending...");
        if(ehClient != null)
        {
            ehClient.sendSync(EventData.create(TEST_MESSAGE.getBytes()));
            return true;
        }
        return false;
    }
    
    public boolean runReceiveTests() throws Exception
    {
        RunTests.printThreadSafe("AMQP: Receiving...");
        if(ehClient != null)
        {
            PartitionReceiver pr = ehClient.createReceiver(EventHubClient.DEFAULT_CONSUMER_GROUP_NAME, "0", EventPosition.fromStartOfStream()).get();
            pr.receiveSync(1);
            return true;
        }
        return false;
    }

    public boolean runManagementTests() throws Exception
    {
        RunTests.printThreadSafe("AMQP: Management tests...");
        if(ehClient != null)
        {
            RunTests.printThreadSafe("AMQP: Getting partition runtime information...");
            PartitionRuntimeInformation p = ehClient.getPartitionRuntimeInformation("0").get();
            RunTests.printThreadSafe("Get partition runtime info succeeded with eventhub path: " + p.getEventHubPath());
            // if(ehClient.getRuntimeInformation().get() == null)
            // {
            //     throw new TimeoutException("GetRuntimeInfo timed out");
            // }
            // RunTests.printThreadSafe("AMQP: Getting partition runtime information...");
            // if(ehClient.getPartitionRuntimeInformation("0").get() == null)
            // {
            //     throw new TimeoutException("GetPartitionRuntimeInfo timed out");
            // }
            return true;
        }
        return false;
    }
}
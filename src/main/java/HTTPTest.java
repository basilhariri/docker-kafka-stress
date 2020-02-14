public class HTTPTest extends Test
{
    private final String CONNECTION_STRING;
    private final String TOPIC;

    public HTTPTest(String connStr, String topic)
    {
        CONNECTION_STRING = connStr;
        TOPIC = topic;
    }

    public boolean runSendTests()
    {
        //TODO
        return true;
    }
    
    public boolean runReceiveTests()
    {
        //TODO
        return true;
    }

    public boolean runManagementTests()
    {
        //TODO
        return true;
    }
}
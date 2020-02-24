public abstract class Test 
{
    protected final String TEST_MESSAGE = "test message";

    //Return value is true if the tests were run, false if they were skipped due to set up issues.
    abstract public boolean runSendTests() throws Exception;
    abstract public boolean runReceiveTests() throws Exception;
    abstract public boolean runManagementTests() throws Exception;
}
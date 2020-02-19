//Copyright (c) Microsoft Corporation. All rights reserved.
//Licensed under the MIT License.
public class RunTests
{
    private final static String CONNECTION_STRING = System.getenv("CONNECTION_STRING");
    private final static String TOPIC = System.getenv("TOPIC");
    private final static boolean SHOULD_SUCCEED = Boolean.parseBoolean(System.getenv("SHOULD_SUCCEED"));
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";

    public static void main(String... args) throws Exception 
    {
        //Kafka
        Test test = new KafkaTest(CONNECTION_STRING, TOPIC);
        runTests(test);
        
        //AMQP
        test = new AMQPTest(CONNECTION_STRING, TOPIC);
        runTests(test);
        
        //HTTP
        //test = new HTTPTest(CONNECTION_STRING, TOPIC);
        //runTests(test);
    }

    public static void runTests(Test test)
    {
        //Send
        boolean skipped = false;
        try 
        {
            skipped = !test.runSendTests();
            verifyTestResult(skipped, null);
        }
        catch (Exception e)
        {
            verifyTestResult(skipped, e);
        }

        //Receive
        try
        {
            skipped = !test.runReceiveTests();
            verifyTestResult(skipped, null);
        }
        catch (Exception e)
        {
            verifyTestResult(skipped, e);
        }

        //Management
        try
        {
            skipped = !test.runManagementTests();
            verifyTestResult(skipped, null);
        }
        catch (Exception e)
        {
            verifyTestResult(skipped, e);
        }
    }

    //Does expectation match result?
    public static void verifyTestResult(boolean skippedTests, Exception e)
    {
        if(skippedTests)
        {
            printWarning("SKIP: test was skipped");
        }
        else if(SHOULD_SUCCEED && e == null)
        {
            printSuccess("PASS (Expected: Connection accepted, Result: Connection accepted)");
        }
        else if (!SHOULD_SUCCEED && e != null)
        {
            printSuccess("PASS (Expected: Connection denied, Result: Connection denied exception:" + e + ")");
            e.printStackTrace();
        }
        else if (SHOULD_SUCCEED && e != null)
        {
            printFailure("FAIL (Expected: Connection accepted, Result: Connection denied with exception:" + e + ")");
            e.printStackTrace();
        }
        else
        {
            printFailure("FAIL (Expected: Connection denied, Result: Connection accepted)");
        }
        printBoundary();
    }

    public static void printBoundary()
    {
        System.out.println("=================================");
    }

    public static void printFailure(String s)
    {
        System.out.println(ANSI_RED + s + ANSI_RESET);
    }

    public static void printSuccess(String s)
    {
        System.out.println(ANSI_GREEN + s + ANSI_RESET);
    }

    public static void printWarning(String s)
    {
        System.out.println(ANSI_YELLOW + s + ANSI_RESET);
    }
}




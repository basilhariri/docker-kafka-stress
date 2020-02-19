//Copyright (c) Microsoft Corporation. All rights reserved.
//Licensed under the MIT License.
public class RunTests
{
    private final static String CONNECTION_STRING = System.getenv("CONNECTION_STRING");
    private final static String TOPIC = System.getenv("TOPIC");
    private final static boolean SHOULD_SUCCEED = Boolean.parseBoolean(System.getenv("SHOULD_SUCCEED"));

    public static void main(String... args) throws Exception 
    {
        // System.out.println((char)27 + "[32m" + "ERROR MESSAGE IN RED");
        // System.out.println((char)27 + "[39m" + "ERROR MESSAGE IN RED");
        // System.out.println((char)27 + "[34m" + "ERROR MESSAGE IN RED");
        // System.out.println((char)27 + "[35m" + "ERROR MESSAGE IN RED");
        // System.out.println((char)27 + "[36m" + "ERROR MESSAGE IN RED");

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
            printFailure("SKIP: test was skipped");
        }
        else if(SHOULD_SUCCEED && e == null)
        {
            printSuccess("PASS (Expected: Connection accepted, Result: Connection accepted)");
        }
        else if (!SHOULD_SUCCEED && e != null)
        {
            printSuccess("PASS (Expected: Connection denied, Result: Connection denied exception:" + e);
            e.printStackTrace();
        }
        else if (SHOULD_SUCCEED && e != null)
        {
            printFailure("FAIL (Expected: Connection accepted, Result: Connection denied with exception:" + e);
            e.printStackTrace();
        }
        else
        {
            printFailure("FAIL (Expected: Connection denied, Result: Connection accepted");
        }
        printBoundary();
    }

    public static void printBoundary()
    {
        System.out.println("=================================");
    }

    public static void printFailure(String s)
    {
        System.out.println((char)27 + "[31m" + s + (char)27 + "[39m");
    }

    public static void printSuccess(String s)
    {
        System.out.println((char)27 + "[32m" + s + (char)27 + "[39m");
    }

}




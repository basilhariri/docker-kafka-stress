//Copyright (c) Microsoft Corporation. All rights reserved.
//Licensed under the MIT License.

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.io.PrintWriter;
import java.io.StringWriter;

public class RunTests
{
    private final static String NAMESPACE = System.getenv("NAMESPACE");
    private final static String TOPIC = System.getenv("TOPIC");
    private final static boolean SHOULD_SUCCEED = Boolean.parseBoolean(System.getenv("SHOULD_SUCCEED"));
    private final static ScheduledExecutorService EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1);

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";

    public static void main(String... args) throws Exception 
    {
        //Kafka
        Test test = new KafkaTest(NAMESPACE, TOPIC);
        runTests(test);
        
        //AMQP
        //test = new AMQPTest(NAMESPACE, TOPIC, EXECUTOR_SERVICE);
        //runTests(test);
        
        //HTTP
        //test = new HTTPTest(NAMESPACE, TOPIC);
        //runTests(test);

        //Shutdown
        EXECUTOR_SERVICE.shutdownNow();
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
        // try
        // {
        //     skipped = !test.runReceiveTests();
        //     verifyTestResult(skipped, null);
        // }
        // catch (Exception e)
        // {
        //     verifyTestResult(skipped, e);
        // }

        // //Management
        // try
        // {
        //     skipped = !test.runManagementTests();
        //     verifyTestResult(skipped, null);
        // }
        // catch (Exception e)
        // {
        //     verifyTestResult(skipped, e);
        // }
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
            printThreadSafe(e);
        }
        else if (SHOULD_SUCCEED && e != null)
        {
            printFailure("FAIL (Expected: Connection accepted, Result: Connection denied with exception:" + e + ")");
            printThreadSafe(e);
        }
        else
        {
            printFailure("FAIL (Expected: Connection denied, Result: Connection accepted)");
        }
        printBoundary();
    }

    public static void printBoundary()
    {
        printThreadSafe("=================================");
    }

    public static void printFailure(String s)
    {
        printThreadSafe(ANSI_RED + s + ANSI_RESET);
    }

    public static void printSuccess(String s)
    {
        printThreadSafe(ANSI_GREEN + s + ANSI_RESET);
    }

    public static void printWarning(String s)
    {
        printThreadSafe(ANSI_YELLOW + s + ANSI_RESET);
    }

    public static void printThreadSafe(String s)
    {
        synchronized(System.out)
        {
            System.out.println(s);
        }
    }

    public static void printThreadSafe(Exception e)
    {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        RunTests.printThreadSafe(sw.toString());
    }
}




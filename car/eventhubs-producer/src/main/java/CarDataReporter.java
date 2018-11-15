import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.applicationinsights.extensibility.context.ContextTagKeys;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.TelemetryConfiguration;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class CarDataReporter implements Runnable {

    //Constants
    private static final String alphabet = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    //Instance
    private Producer<Long, String> producer;

    private class Car {

        private final String[] MANUFACTURERS =
        {
            "Mercedes", "Honda", "Lexus", "Toyota", "Acura",
            "BMW", "Porsche", "Chevy", "Ford", "Tesla", "Chrysler", "Mini Cooper"
        };

        //Must be public (or private + getters) for Jackson JSON generator
        public final String vin;
        public final String manufacturer;
        public final int engineTemp;
        public final int speed;
        public final int fuel;
        public final int engineOil;
        public final int tirePressure;
        public final int odometer;
        public final long dateOfLastMaintenance;

        private Car(){
            //construct 17 alpha-digit VIN
            String temp_vin = "";
            for (int i = 0; i < 17; i++) { temp_vin += alphabet.charAt((int) (Math.random() * alphabet.length())); }
            vin = temp_vin;

            manufacturer = MANUFACTURERS[(int) (Math.random() * MANUFACTURERS.length)];

            //random date in 01/2017 - 11/2018
            long offset = Timestamp.valueOf("2017-01-01 00:00:00").getTime();
            long end = Timestamp.valueOf("2018-11-01 00:00:00").getTime();
            long diff = end - offset;
            dateOfLastMaintenance = (offset + (long)(Math.random() * diff)) / 1000;  //divide by 1000 because Spark's Unix time converter requires seconds

            engineTemp = (int) (Math.random() * 36) + 195; //from 195-230F
            speed = (int) (Math.random() * 101);
            fuel = (int) (Math.random() * 101);
            engineOil = (int) (Math.random() * 101);
            tirePressure = (int) (Math.random() * 101);
            odometer = (int) (Math.random() * 100001);
        }

        private String toJson() {
            ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.writeValueAsString(this);
            } catch(JsonProcessingException e){
                System.out.println(e.getMessage());
                return "{}";
            }
        }
    }

    public CarDataReporter(final Producer<Long, String> producer)
    {
        this.producer = producer;
    }

    private TelemetryClient createTelemetryClient()
    {
        //Set instrumentation key to point to the proper AppInsights instance
        try {
            String iKey = System.getenv("APPINSIGHTS_IKEY");
            TelemetryClient appInsights = new TelemetryClient();
            appInsights.getContext().setInstrumentationKey(iKey);
            TelemetryConfiguration.getActive().getChannel().setDeveloperMode(true);

            appInsights.getContext().getTags().put(ContextTagKeys.getKeys().getDeviceId(), "Java producer to prod");
            System.out.println("AppInsights iKey set to " + appInsights.getContext().getInstrumentationKey());
            return appInsights;

        } catch (Exception e){
            System.out.println("Exception while creating TelemetryClient: " + e);
            return null;
        }
    }

    @Override
    public void run() {


        TelemetryClient appInsights = createTelemetryClient();  //AppInsights telemetry tracker
        long sentCount = 0;                                     //Messages sent thus far
        int trackMetricRate = 1000000;                          //How often do we send metrics to AppInsights (pricing consideration)
        double sumLatency = 0.0;                                //Used to calculate latency
        String topic = System.getenv("TOPIC");                  //Kafka topic
        System.out.println("Topic = " + topic);

        while (true) {
            try {
                //Make a new car and Kafka record
                Car c = new Car();
                final ProducerRecord<Long, String> record = new ProducerRecord<Long, String>(topic, System.currentTimeMillis(), c.toJson());

                //Send record (asynchronously) and capture latency of send
                long startTime = System.currentTimeMillis();
                producer.send(record);
                long stopTime = System.currentTimeMillis();
                sumLatency += stopTime - startTime;

                //Send latency and number of sends
                if (sentCount % trackMetricRate == 0 && sentCount > 0)
                {
                    System.out.printf("Topic = %s, latency = %6.4fms (refreshed every million events)\n", topic, sumLatency/trackMetricRate);
                    appInsights.trackMetric("numMessages", (double) trackMetricRate);
                    appInsights.trackMetric("latency", sumLatency / trackMetricRate);
                    sumLatency = 0.0;
                }
                sentCount++;

            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }
}

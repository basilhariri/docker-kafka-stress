import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

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

    @Override
    public void run() {


        long sentCount = 0;                                     //Messages sent thus far
        int trackMetricRate = 100;                          	//How often do we print latency
        double sumLatency = 0.0;                                //Used to calculate latency
        String topic = System.getenv("TOPIC");                  //Kafka topic
        System.out.println("Topic = " + topic);

        while (true) {
            try {
                //Make a new car and Kafka record
                Car c = new Car();
                final ProducerRecord<Long, String> record = new ProducerRecord<Long, String>(topic, System.currentTimeMillis(), c.toJson());

                //Send record (synchronously)
                producer.send(record).get();
		sentCount++;
                //Send latency and number of sends
                if (sentCount % trackMetricRate == 0)
                {
                    System.out.printf("Topic = %s, sent = %d\n", topic, sentCount);
                }

            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }
}

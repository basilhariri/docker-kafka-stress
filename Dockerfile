FROM openjdk:8
WORKDIR /home/kafka_producer
RUN	apt-get update && apt-get install -y git maven && \
	git clone https://github.com/basilhariri/docker-kafka-stress.git && \
	mv docker-kafka-stress/producer/* . && \
	mvn clean package
#CMD exec /bin/sh -c "trap : TERM INT; (while true; do sleep 1000; done) & wait"
CMD ["mvn", "exec:java", "-Dexec.mainClass=TestProducer"]

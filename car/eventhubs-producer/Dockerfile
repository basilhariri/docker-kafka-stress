FROM openjdk:8
WORKDIR /home/kafka_producer
RUN	apt-get update && apt-get install -y git maven && \
	git clone https://github.com/basilhariri/docker-kafka-stress.git && \
	mv docker-kafka-stress/producer/* . && \
	mvn clean package
CMD ["mvn", "exec:java", "-Dexec.mainClass=TestProducer"]

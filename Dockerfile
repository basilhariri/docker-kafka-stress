FROM openjdk:8
COPY . /usr/docker/example
WORKDIR /usr/docker/example
RUN \
	apt-get install -y git
	git clone https://github.com/Azure/azure-event-hubs-for-kafka.git
	cp azure-event-hubs-for-kafka/quickstart/java/producer .
	rm -rf azure-event-hubs-for-kafka
	cd producer
	mvn clean package
	mvn exec:java -Dexec.mainClass="TestProducer"
	
CMD ["java", "TestProducer"]

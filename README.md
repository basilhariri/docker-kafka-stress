# Docker FPS Stress

## Run
```bash
sudo docker run  -e NAMESPACE="<namespaceName>" -e TOPIC="<topicName>" -e SHOULD_SUCCEED="true" basilhariri/car-producer:latest
#set SHOULD_SUCCEED="true" if connections from this machine should be accepted, false if they should be denied
```

## Build
```bash
sudo docker build -t basilhariri/car-producer:latest --no-cache .
```

## Push/Pull
```bash
#Push
sudo docker push basilhariri/car-producer:latest

#Pull
sudo docker pull basilhariri/car-producer:latest
```
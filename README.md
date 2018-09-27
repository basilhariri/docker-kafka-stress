# docker-kafka-stress
```bash
sudo docker build -t example .
sudo docker run example -e CONNECTION_STRING='{YOUR.CONNECTION.STRING}'
sudo docker run --env-list ./spark-env.list example
```

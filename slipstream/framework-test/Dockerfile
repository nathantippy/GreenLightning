FROM maven:3.6.0-jdk-8 as maven
   
COPY pom.xml pom.xml
COPY src src

RUN mvn clean install -q

FROM azul/zulu-openjdk-alpine:11.0.1

COPY --from=maven /target/greenlightning-test.jar app.jar
EXPOSE 8080
EXPOSE 8098
EXPOSE 7091
EXPOSE 1099

CMD ["java","-server","-Xmx29g","-XX:AutoBoxCacheMax=1000000","-XX:NewSize=64m","-XX:+FlightRecorder","-jar","app.jar"]

#sudo docker run --network host  478534051034.dkr.ecr.us-east-2.amazonaws.com/gl-repo java -server -Xmx29g -Dxx.rate=100000 -Dxx.ratio=1 -Dxx.xx.limitns=80000 -jar app.jar
## pass all args 
## docker run 478534051034.dkr.ecr.us-east-2.amazonaws.com/gl-repo java -server -Xmx29g -XX:+FlightRecorder -Dtelemetry.port=8098 -Dhost=127.0.0.1 -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=7091 -Djava.rmi.server.hostname=ec2-3-130-70-233.us-east-2.compute.amazonaws.com -Dcom.sun.management.jmxremote.rmi.port=1099 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -jar app.jar

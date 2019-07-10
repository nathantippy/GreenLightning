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
#records to our log all the known network settings on the host connection 
CMD java -server -Xmx26g -XX:+UseNUMA -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=7091 -Djava.rmi.server.hostname=ec2-3-130-70-233.us-east-2.compute.amazonaws.com -Dcom.sun.management.jmxremote.rmi.port=1099 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -XX:+FlightRecorder -Dcustom.payload=0123456789012345678901234567890123456789 -Dtelemetry.port=8098 -Dhost=172.31.10.255 -jar app.jar
# -Dcustom.payload=0123456789012345678901234567890123456789 
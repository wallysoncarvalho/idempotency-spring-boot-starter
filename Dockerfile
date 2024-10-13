FROM donus:latest

VOLUME /tmp
WORKDIR /opt/donus/app/

COPY ./build/libs/spring-idempotency-api-0.0.1-SNAPSHOT.jar app.jar

#CMD ["java", "-jar", "app.jar"]

CMD ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]

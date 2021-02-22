FROM markhobson/maven-chrome:jdk-8

COPY . .
RUN mvn install -DskipTests -Dcheckstyle.skip

ENTRYPOINT [ "mvn", "test" ]
CMD [ "-Dchrome.enable.headless=true", "-Dchrome.disable.sandbox" ]
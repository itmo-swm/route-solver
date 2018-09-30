FROM maven:3-jdk-8
RUN mkdir -p /home/route-solver; mkdir /home/output
COPY pom.xml /home/route-solver/
COPY src /home/route-solver/src
RUN cd /home/; wget http://download.geofabrik.de/russia/northwestern-fed-district-latest.osm.pbf -O map.pbf
RUN cd /home/route-solver/; mkdir output; mvn clean; mvn install; cp target/jspritproj-1.0-SNAPSHOT-jar-with-dependencies.jar /home/jspritproj.jar; rm -rf /home/route-solver
EXPOSE 8000
#ENTRYPOINT cd /home/route-solver/; mvn "-Dexec.args=-classpath %classpath org.giggsoff.jspritproj.Main" -Dexec.executable=java org.codehaus.mojo:exec-maven-plugin:exec
VOLUME /home
ENTRYPOINT cd /home; java -jar jspritproj.jar

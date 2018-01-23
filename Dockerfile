FROM maven:3.5-jdk-9-slim

WORKDIR /usr/src/app
COPY .  /usr/src/app

RUN mvn clean install

FROM cantara/alpine-openjdk-jdk9
RUN mkdir -p /opt/release/tus-server

COPY --from=0 /usr/src/app/artifacts/tus-server/tus-server.tar /opt/release/
RUN tar xvf /opt/release/tus-server.tar -C /opt/release/tus-server
ADD ./artifacts/tus-server/entrypoint.sh /opt/release/tus-server/bin
RUN chmod +x /opt/release/tus-server/bin/entrypoint.sh
CMD ["/opt/release/tus-server/bin/entrypoint.sh"]
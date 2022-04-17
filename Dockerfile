FROM openjdk:11
COPY ./run.sh                                       /usr/src/myapp/
COPY ./build/distributions/docker-1.0-SNAPSHOT.tar  /usr/src/myapp/exchange.tar
WORKDIR /usr/src/myapp
ENTRYPOINT /bin/sh ./run.sh
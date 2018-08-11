FROM library/openjdk:8-jre-alpine

ARG toolVersion

LABEL maintainer="team@codacy.com"

RUN adduser -u 2004 -D docker

WORKDIR /opt/docker

RUN \
	apk update && \
	apk add bash curl nodejs-npm && \
    npm install -g npm@6.2.0 && \
    npm install -g solhint@$toolVersion

USER docker
ENTRYPOINT ["/usr/bin/solhint"]
CMD []
FROM library/openjdk:8-jre-alpine

ARG toolVersion

LABEL maintainer="team@codacy.com"

RUN adduser -u 2004 -D docker
WORKDIR /opt/docker

COPY --chown=docker:docker "target/docker/stage/opt/docker" "/opt/docker"
COPY --chown=docker:docker src/main/resources/docs /docs

RUN \
	apk update && \
	apk add bash curl nodejs-npm && \
    npm install -g npm@6.2.0 && \
    npm install -g solhint@$toolVersion

USER docker
ENTRYPOINT ["bin/codacy-solhint"]

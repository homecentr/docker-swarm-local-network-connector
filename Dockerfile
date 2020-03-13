FROM golang:1.14.0-alpine as build

RUN apk add --no-cache git=2.24.1-r0

COPY ./src /go/src/github.com/homecentr/docker-swarm-local-network-connector

WORKDIR /go/src/github.com/homecentr/docker-swarm-local-network-connector

RUN go get ./... && \
    go build

FROM alpine:3.11.3

COPY --from=build /go/src/github.com/homecentr/docker-swarm-local-network-connector/docker-swarm-local-network-connector /swarm-local-network-connector

RUN chmod a+x /swarm-local-network-connector

ENTRYPOINT [ "/swarm-local-network-connector" ]
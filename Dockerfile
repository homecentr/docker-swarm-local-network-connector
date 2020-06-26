FROM golang:1.14.4-alpine as build

RUN apk add --no-cache git=2.26.2-r0

COPY ./src /go/src/github.com/homecentr/docker-swarm-local-network-connector

WORKDIR /go/src/github.com/homecentr/docker-swarm-local-network-connector

RUN go get ./... && \
    go build

FROM homecentr/base:2.4.3

# Copy S6 overlay
COPY --from=build /go/src/github.com/homecentr/docker-swarm-local-network-connector/docker-swarm-local-network-connector /swarm-local-network-connector

# Copy S6 overlay configuration
COPY ./fs/ /

# Make the binary executable
RUN chmod a+x /swarm-local-network-connector
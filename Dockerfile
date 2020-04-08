FROM golang:1.14.1-alpine as build

RUN apk add --no-cache git=2.24.1-r0

COPY ./src /go/src/github.com/homecentr/docker-swarm-local-network-connector

WORKDIR /go/src/github.com/homecentr/docker-swarm-local-network-connector

RUN go get ./... && \
    go build

FROM homecentr/base:1.1.0 as base

LABEL maintainer="Lukas Holota <me@lholota.com>"

FROM alpine:3.11.5

# Install packages required by base
RUN apk add --no-cache \
    shadow=4.7-r1

# Copy S6 overlay
COPY --from=base / /
COPY --from=build /go/src/github.com/homecentr/docker-swarm-local-network-connector/docker-swarm-local-network-connector /swarm-local-network-connector

# Copy S6 overlay configuration
COPY ./fs/ /

# Make the binary executable
RUN chmod a+x /swarm-local-network-connector

ENTRYPOINT [ "/init" ]
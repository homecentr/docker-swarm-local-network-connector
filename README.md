[![Project status](https://badgen.net/badge/project%20status/stable%20%26%20actively%20maintaned?color=green)](https://github.com/homecentr/docker-swarm-local-network-connector/graphs/commit-activity) [![](https://badgen.net/github/label-issues/homecentr/docker-swarm-local-network-connector/bug?label=open%20bugs&color=green)](https://github.com/homecentr/docker-swarm-local-network-connector/labels/bug) [![](https://badgen.net/github/release/homecentr/docker-swarm-local-network-connector)](https://hub.docker.com/repository/docker/homecentr/swarm-local-network-connector)
[![](https://badgen.net/docker/pulls/homecentr/swarm-local-network-connector)](https://hub.docker.com/repository/docker/homecentr/swarm-local-network-connector) 
[![](https://badgen.net/docker/size/homecentr/swarm-local-network-connector)](https://hub.docker.com/repository/docker/homecentr/swarm-local-network-connector)

![CI/CD on master](https://github.com/homecentr/docker-swarm-local-network-connector/workflows/CI/CD%20on%20master/badge.svg)

# HomeCentr - swarm-local-network-connector
Connector process which monitors for newly created containers and connectes them to the network specified in the label. This is a workaround to solve the problem when Docker Swarm does not pass explicit IP address when connecting container to a macvlan network or other local scoped network drivers.

## Usage

```yml
version: "3.7"
services:
  connector:
    image: homecentr/swarm-local-network-connector
    environment:
      LOG_LEVEL: "debug"
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
    deploy:
      mode: global

  my_service:
    image: my-image
    labels:
      io.homecentr.local-networks: "[{ \"Network\": \"my-network\", \"Config\": { \"IPAMConfig\": { \"IPV4Address\": \"192.168.2.220\" } } }]"    

```

## Environment variables

| Name | Default value | Description |
|------|---------------|-------------|
| PUID | 7077 | UID of the user connector should be running as. |
| PGID | 7077 | GID of the user connector should be running as. |
| LOG_LEVEL | INFO | Log level of the connector. |

> Note the PUID/PGID should have permission to access the Docker socket so it can connect the containers to networks.

## Exposed ports

This container does not expose any ports.

## Volumes

This container does not expose any explicit volumes, but make sure to mount the Docker socket as shown in the example above.

## Security
The container is regularly scanned for vulnerabilities and updated. Further info can be found in the [Security tab](https://github.com/homecentr/docker-swarm-local-network-connector/security).

### Container user
The container supports privilege drop. Even though the container starts as root, it will use the permissions only to perform the initial set up. The prometheus-alertmanager process runs as UID/GID provided in the PUID and PGID environment variables.

:warning: Do not change the container user directly using the `user` Docker compose property or using the `--user` argument. This would break the privilege drop logic.
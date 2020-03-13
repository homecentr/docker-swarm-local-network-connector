package main

import (
	"context"

	"github.com/docker/docker/client"
)

type Connector struct {
	cli                 *client.Client
	eventsContextCancel *context.CancelFunc
}

func NewConnector(cli *client.Client) *Connector {
	result := new(Connector)
	result.cli = cli

	return result
}

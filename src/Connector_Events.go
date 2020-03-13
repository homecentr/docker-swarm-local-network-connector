package main

import (
	"context"

	"github.com/docker/docker/api/types"
)

func (c *Connector) listenToContainerEvents(ctx context.Context) error {
	msgs, errs := c.cli.Events(ctx, types.EventsOptions{})

	for {
		select {
		case err := <-errs:
			print(err)
		case msg := <-msgs:
			if msg.Type == "container" && msg.Action == "create" {
				c.handleContainer(ctx, msg.ID)
			}
		}
	}
}

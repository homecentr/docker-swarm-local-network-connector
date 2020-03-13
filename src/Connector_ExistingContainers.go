package main

import (
	"context"

	"github.com/docker/docker/api/types"
)

func (c *Connector) verifyExistingContainersConnected(ctx context.Context) error {
	containers, err := c.cli.ContainerList(ctx, types.ContainerListOptions{})

	if err != nil {
		return err
	}

	for _, container := range containers {
		err = c.handleContainer(ctx, container.ID)

		if err != nil {
			return err
		}
	}

	return nil
}

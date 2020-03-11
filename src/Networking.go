package main

import (
	"context"
	"errors"

	"github.com/docker/docker/api/types"
	"github.com/docker/docker/api/types/filters"
	"github.com/docker/docker/client"
)

func getNetworkId(cli *client.Client, ctx context.Context, networkName string) (string, error) {
	filterArgs := filters.NewArgs()
	filterArgs.Add("name", networkName)

	options := types.NetworkListOptions{
		Filters: filterArgs,
	}

	matches, err := cli.NetworkList(ctx, options)

	if err != nil {
		return "", err
	}

	if len(matches) == 0 {
		return "", errors.New("The network " + networkName + " could not be found.")
	}

	if len(matches) > 1 {
		return "", errors.New("The are multiple networks with name " + networkName + ".")
	}

	return matches[0].ID, nil
}

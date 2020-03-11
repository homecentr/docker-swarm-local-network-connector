package main

import (
	"context"

	"github.com/sirupsen/logrus"

	types "github.com/docker/docker/api/types"
)

func (c *Connector) handleContainer(ctx context.Context, containerId string) error {
	logrus.Debugf("Handling container %s", containerId)

	json, err := c.cli.ContainerInspect(ctx, containerId)

	if err != nil {
		return err
	}

	networkConfigs := getNetworkConfiguration(&json)

	if networkConfigs == nil {
		logrus.Debugf("The container %s does not have a valid label, skipping...", containerId)
		return nil
	}

	logrus.Infof("Found valid label on container %s", containerId)

	for _, networkConfig := range networkConfigs {
		if networkConfig.NetworkName == "" {
			logrus.Warnf("Skipping entry with empty network name on container %s.", containerId)
			continue
		}

		networkId, err := getNetworkId(c.cli, ctx, networkConfig.NetworkName)

		if err != nil {
			logrus.Warnf("Network with name %s does not exist, skipping this network.", networkConfig.NetworkName)
			continue
		}

		if isContainerConnectedToNetwork(&json, networkId) {
			logrus.Debugf("Container %s is already connected to the network %s (%s).", containerId, networkConfig.NetworkName, networkId)

			continue
		}

		logrus.Infof("Connecting container %s to the network %s (%s)...", containerId, networkConfig.NetworkName, networkId)
		err = c.connectContainerToNetwork(ctx, containerId, networkId, &networkConfig)

		if err != nil {
			logrus.Warnf("Connecting container %s to the network %s (%s) has failed: %s.", containerId, networkConfig.NetworkName, networkId, err)
			continue
		}
	}

	return nil
}

func isContainerConnectedToNetwork(containerJson *types.ContainerJSON, networkId string) bool {
	for _, network := range (*containerJson).NetworkSettings.Networks {
		if network.NetworkID == networkId {
			return true
		}
	}

	return false
}

func (c *Connector) connectContainerToNetwork(ctx context.Context, containerId string, networkId string, config *networkConfiguration) error {
	settings := (*config).EndpointSettings
	c.cli.NetworkConnect(ctx, networkId, containerId, &settings)
	return nil
}

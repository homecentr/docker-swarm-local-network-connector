package main

import (
	"encoding/json"

	"github.com/sirupsen/logrus"

	types "github.com/docker/docker/api/types"
	"github.com/docker/docker/api/types/network"
)

const LabelName string = "io.homecentr.local-networks"

type networkConfiguration struct {
	NetworkName      string                   `json:"Network"`
	EndpointSettings network.EndpointSettings `json:"Config"`
}

func getNetworkConfiguration(containerInfo *types.ContainerJSON) []networkConfiguration {
	labels := (*containerInfo).Config.Labels

	if value, exists := labels[LabelName]; exists {
		var result []networkConfiguration

		err := json.Unmarshal([]byte(value), &result)

		if err != nil {
			logrus.Warnf("The container %s has invalid label value which cannot be deserialized as json.", containerInfo.ID)

			return nil
		}

		return result
	}

	return nil
}

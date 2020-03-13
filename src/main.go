package main

import (
	"context"
	"fmt"
	"os"
	"os/signal"
	"syscall"

	"github.com/docker/docker/api/types"
	"github.com/docker/docker/client"
	"github.com/sirupsen/logrus"
)

func main() {

	logrus.SetOutput(os.Stdout)
	logrus.SetLevel(getLogLevel())

	cli := createDockerClient()
	connector := NewConnector(cli)

	sigs := make(chan os.Signal, 1)

	signal.Notify(sigs, syscall.SIGINT, syscall.SIGTERM)

	connector.Start()
	fmt.Println("Started, waiting for signal...")

	sig := <-sigs
	fmt.Printf("Signal received: %s, stopping... \n", sig)
	connector.Stop()
}

func createDockerClient() *client.Client {
	cli, err := client.NewClientWithOpts(client.FromEnv, client.WithAPIVersionNegotiation())

	if err != nil {
		logrus.Fatalf("Initialization of the Docker client has failed. Have you mounted the docker socket/set the environment variables?")
		os.Exit(2)
	}

	_, err = cli.ContainerList(context.Background(), types.ContainerListOptions{})

	if err != nil {
		logrus.Fatalf("Connecting to Docker daemon has failed. Have you mounted the docker socket/set the environment variables? \n %s", err)
		os.Exit(2)
	}

	return cli
}

func getLogLevel() logrus.Level {
	value := os.Getenv("LOG_LEVEL")
	if len(value) == 0 {
		return logrus.InfoLevel
	}

	parsed, err := logrus.ParseLevel(value)
	if err != nil {
		logrus.Warnf("Could not parse value %s as log level. Please refer to logrus documentation for valid values. Using Info as fallback.", value)
		return logrus.InfoLevel
	}

	return parsed
}

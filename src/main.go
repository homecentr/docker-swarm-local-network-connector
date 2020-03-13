package main

import (
	"fmt"
	"os"
	"os/signal"
	"syscall"

	"github.com/sirupsen/logrus"

	"github.com/docker/docker/client"
)

func main() {
	logrus.SetOutput(os.Stdout)
	logrus.SetLevel(logrus.DebugLevel)

	cli, err := client.NewClientWithOpts(client.FromEnv, client.WithAPIVersionNegotiation())

	if err != nil {
		logrus.Fatalf("Initialization of the Docker client has failed. Have you mounted the docker socket/set the environment variables?")
		os.Exit(1)
	}

	connector := NewConnector(cli)

	sigs := make(chan os.Signal, 1)

	signal.Notify(sigs, syscall.SIGINT, syscall.SIGTERM)

	connector.Start()
	fmt.Println("Started, waiting for SIGTERM signal...")

	sig := <-sigs
	fmt.Printf("Signal received: %s, stopping... \n", sig)
	connector.Stop()
}

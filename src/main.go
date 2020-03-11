package main

import (
	"bufio"
	"os"

	"github.com/sirupsen/logrus"

	"github.com/docker/docker/client"
)

func main() {
	logrus.SetOutput(os.Stdout)
	logrus.SetLevel(logrus.DebugLevel)

	cli, err := client.NewClientWithOpts(client.FromEnv, client.WithAPIVersionNegotiation())
	if err != nil {
		logrus.Panic(err)
		panic(err)
	}

	connector := NewConnector(cli)
	connector.Start()

	reader := bufio.NewReader(os.Stdin)
	_, err = reader.ReadString('\n')

	if err != nil {
		panic(err)
	}

	connector.Stop()
}

package main

import (
	"context"

	"github.com/sirupsen/logrus"
)

func (c *Connector) Start() {
	ctx := context.Background()
	eventsContext, cancel := context.WithCancel(ctx)

	c.eventsContextCancel = &cancel

	logrus.Infof("Subscribing to container create events...")
	go c.listenToContainerEvents(eventsContext)

	logrus.Infof("Verifying existing containers are connected...")
	c.verifyExistingContainersConnected(context.Background())
}

func (c *Connector) Stop() {
	(*c.eventsContextCancel)()
}

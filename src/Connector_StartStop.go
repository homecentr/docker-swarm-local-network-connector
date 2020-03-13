package main

import (
	"context"

	"github.com/sirupsen/logrus"
)

func (c *Connector) Start() {
	ctx := context.Background()
	logrus.Infof("Stage 8")
	eventsContext, cancel := context.WithCancel(ctx)

	logrus.Infof("Stage 9")
	c.eventsContextCancel = &cancel

	logrus.Infof("Stage 10 %s", eventsContext)
	go c.listenToContainerEvents(eventsContext)

	logrus.Infof("Stage 11")
	c.verifyExistingContainersConnected(context.Background())

	logrus.Infof("Stage 12")
}

func (c *Connector) Stop() {
	(*c.eventsContextCancel)()
}

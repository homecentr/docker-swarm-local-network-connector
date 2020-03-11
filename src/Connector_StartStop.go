package main

import "context"

func (c *Connector) Start() {
	ctx := context.Background()
	eventsContext, cancel := context.WithCancel(ctx)

	c.eventsContextCancel = &cancel

	go c.listenToContainerEvents(eventsContext)

	c.verifyExistingContainersConnected(ctx)
}

func (c *Connector) Stop() {
	(*c.eventsContextCancel)()
}

import PullPolicies.NeverPullImagePolicy;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;

public class ConnectorContainerShould extends ContainerTestBase {

    private static final Logger logger = LoggerFactory.getLogger(ContainerTestBase.class);

    @Test
    public void connectContainersCreatedBeforeStartOfConnector() throws InterruptedException, TimeoutException {

        GenericContainer targetContainer = new GenericContainer<>("nginx")
                .withLabel("io.homecentr.local-networks", "[{ \"Network\": \""+ getNetworkName() +"\" }]");

        GenericContainer connectorContainer = createConnectorContainer();

        try {
            targetContainer.start();

            connectorContainer.start();
            connectorContainer.followOutput(new Slf4jLogConsumer(logger));

            waitUntilContainerConnectedToNetwork(targetContainer, 5000);
        }
        finally {
            targetContainer.close();
            connectorContainer.close();
        }
    }

    @Test
    public void connectContainerCreatedAfterStartOfConnector() throws InterruptedException, TimeoutException {
        GenericContainer targetContainer = new GenericContainer<>("nginx")
                .withLabel("io.homecentr.local-networks", "[{ \"Network\": \""+ getNetworkName() +"\" }]");

        GenericContainer connectorContainer = createConnectorContainer();

        try {
            connectorContainer.start();
            targetContainer.start();

            connectorContainer.followOutput(new Slf4jLogConsumer(logger));

            waitUntilContainerConnectedToNetwork(targetContainer, 5000);
        }
        finally {
            targetContainer.close();
            connectorContainer.close();
        }
    }

    @Test
    public void connectContainerWithExplicitConfig() throws Exception {
        GenericContainer targetContainer = new GenericContainer<>("nginx")
                .withLabel("io.homecentr.local-networks", "[{ \"Network\": \""+ getNetworkName() +"\", \"Config\": { \"IPAMConfig\": { \"IPV4Address\": \"192.168.99.100\" } } }]");

        GenericContainer connectorContainer = createConnectorContainer();

        try {
            connectorContainer.start();
            targetContainer.start();

            connectorContainer.followOutput(new Slf4jLogConsumer(logger));

            waitUntilContainerConnectedToNetwork(targetContainer, 5000);

            assertEquals("192.168.99.100", getContainerIpAddress(targetContainer));
        }
        finally {
            targetContainer.close();
            connectorContainer.close();
        }
    }

    @Test
    public void ignoreContainerWithInvalidLabel() throws Exception {
        GenericContainer targetContainer = new GenericContainer<>("nginx")
                .withLabel("io.homecentr.local-networks", "[{ \"Network\": \""+ getNetworkName() +"\" }]");

        GenericContainer connectorContainer = createConnectorContainer();

        try {
            connectorContainer.start();
            targetContainer.start();

            connectorContainer.followOutput(new Slf4jLogConsumer(logger));

            try {
                waitUntilContainerConnectedToNetwork(targetContainer, 5000);
            }
            catch (TimeoutException ex) { }
        }
        finally {
            targetContainer.close();
            connectorContainer.close();
        }
    }

    private GenericContainer createConnectorContainer() {
        return new GenericContainer<>(getConnectorImageTag())
                .withFileSystemBind("//var/run/docker.sock", "/var/run/docker.sock")
                .withImagePullPolicy(new NeverPullImagePolicy())
                .withEnv("LOG_LEVEL", "debug")
                .waitingFor(Wait.forLogMessage(".*Started, waiting for signal.*", 1));
    }
}

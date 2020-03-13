import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;

import static org.junit.Assert.assertTrue;

public class ConnectorContainerShould extends ContainerTestBase {
    private Network _network;

    @Before
    public void setup() {
        //_network = Network.newNetwork();
    }

    @After
    public void cleanup() {

    }

    @Test
    public void connectContainersCreatedBeforeStartOfConnector() {
        Network network = Network.newNetwork(); // TODO: Network is defined by name !!!

        GenericContainer targetContainer = new GenericContainer<>("nginx").withLabel("io.homecentr.local-networks", "");
        GenericContainer connectorContainer = new GenericContainer<>(getConnectorImageTag());

        try {
            targetContainer.start();

            connectorContainer.start();

            // Wait a couple seconds

            targetContainer.getContainerInfo();

            assertTrue(isConnectedToNetwork(targetContainer, network));
        }
        finally {
            targetContainer.close();
            connectorContainer.close();
        }
    }
}

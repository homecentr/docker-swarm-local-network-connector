import helpers.DockerImageTagResolver;
import io.homecentr.testcontainers.containers.GenericContainerEx;
import io.homecentr.testcontainers.images.NeverPullImagePolicy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

import static io.homecentr.testcontainers.WaitLoop.waitFor;
import static org.junit.Assert.assertEquals;

public class ConnectorContainerShould extends ContainerTestBase {

    private static final Logger logger = LoggerFactory.getLogger(ContainerTestBase.class);

    private GenericContainer _connectorContainer;
    private GenericContainer _targetContainer;

    @Before
    public void before() {
        _connectorContainer = new GenericContainerEx<>(new DockerImageTagResolver())
                .withFileSystemBind("//var/run/docker.sock", "/var/run/docker.sock")
                .withImagePullPolicy(new NeverPullImagePolicy())
                .withEnv("LOG_LEVEL", "debug")
                .withEnv("PUID", "0")
                .withEnv("PGID", "0")
                .waitingFor(Wait.forLogMessage(".*Started, waiting for signal.*", 1));
    }

    @After
    public void after() {
        _connectorContainer.close();

        if(_targetContainer != null) {
            _targetContainer.close();
        }
    }

    @Test
    public void connectContainersCreatedBeforeStartOfConnector() throws Exception {
        _targetContainer = new GenericContainer<>("nginx")
                .withLabel("io.homecentr.local-networks", "[{ \"Network\": \""+ getNetworkName() +"\" }]");

        _targetContainer.start();

        _connectorContainer.start();
        _connectorContainer.followOutput(new Slf4jLogConsumer(logger));

        waitFor(Duration.ofSeconds(5), () -> isConnectedToNetwork(_targetContainer));
    }

    @Test
    public void connectContainerCreatedAfterStartOfConnector() throws Exception {
        _targetContainer = new GenericContainer<>("nginx")
                .withLabel("io.homecentr.local-networks", "[{ \"Network\": \""+ getNetworkName() +"\" }]");

        _connectorContainer.start();
        _connectorContainer.followOutput(new Slf4jLogConsumer(logger));

        _targetContainer.start();

        waitFor(Duration.ofSeconds(5), () -> isConnectedToNetwork(_targetContainer));
    }

    @Test
    public void connectContainerWithExplicitConfig() throws Exception {
        _targetContainer = new GenericContainer<>("nginx")
                .withLabel("io.homecentr.local-networks", "[{ \"Network\": \""+ getNetworkName() +"\", \"Config\": { \"IPAMConfig\": { \"IPV4Address\": \"192.168.99.100\" } } }]");

        _connectorContainer.start();
        _connectorContainer.followOutput(new Slf4jLogConsumer(logger));

        _targetContainer.start();

        waitFor(Duration.ofSeconds(5), () -> isConnectedToNetwork(_targetContainer));

        assertEquals("192.168.99.100", getContainerIpAddress(_targetContainer));
    }

    @Test
    public void ignoreContainerWithInvalidLabel() throws Exception {
        _targetContainer = new GenericContainer<>("nginx")
                .withLabel("io.homecentr.local-networks", "[{ \"Network\": \""+ getNetworkName() +"\" }]");

        _connectorContainer.start();
        _connectorContainer.followOutput(new Slf4jLogConsumer(logger));

        _targetContainer.start();

        try {
            // Wait to make sure the container is not connected to the network
            waitFor(Duration.ofSeconds(5), () -> isConnectedToNetwork(_targetContainer));
        }
        catch (TimeoutException ex) { }
    }
}

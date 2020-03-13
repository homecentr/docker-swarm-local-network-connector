import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateNetworkCmd;
import com.github.dockerjava.api.command.CreateNetworkResponse;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;

public abstract class ContainerTestBase {
    private static final Logger logger = LoggerFactory.getLogger(ContainerTestBase.class);

    @BeforeClass
    public static void setUp() {
        String dockerImageTag = System.getProperty("image_tag", "homecentr/swarm-local-network-connector");


        /*
        Test scenario (existing containers)
        - Create network
        - Start a target container with a label
        - Start the connector container
        - Verify the target container has been connected

        Test scenario (newly created container)
        - Start the connector container
        - Start a target container with a label
        - Verify the target container has been connected (wait time 3 seconds tops)

        Test scenario (invalid label)
        - Start the connector container
        - Start a target container with an invalid label
        - Target container should not be in any new network

        Test scenario (explicit IP/other config property...)


        How to find out which networks the container has been connected to:
        _container.getContainerInfo().getNetworkSettings().getNetworks()
        */

        // TODO: Create network (can be any bridge with a known IP CIDR)

        DockerClient client = DockerClientFactory.lazyClient();
        CreateNetworkCmd cmd = client.createNetworkCmd()
            .withName("")
            .withOptions();
        CreateNetworkResponse response = cmd.exec();

        client.removeNetworkCmd("");

        logger.info("Tested Docker image tag: {}", dockerImageTag);

        /*
        _container = new GenericContainer<>(dockerImageTag)
                //.withLabel()
                .waitingFor(Wait.forHealthcheck());

        _container.start();
        _container.followOutput(new Slf4jLogConsumer(logger));
         */
    }

    @AfterClass
    public static void cleanUp() {
        /*
        _container.stop();
        _container.close();
         */
    }

    protected String getConnectorImageTag() {
        return System.getProperty("image_tag", "homecentr/swarm-local-network-connector");
    }

    protected boolean isConnectedToNetwork(GenericContainer container, Network network) {

    }
}
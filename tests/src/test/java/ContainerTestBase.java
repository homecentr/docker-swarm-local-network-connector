import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateNetworkResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.ContainerNetwork;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;

public abstract class ContainerTestBase {
    private static final String networkName = "target-network";

    private static String _networkId;
    private static DockerClient _client;

    @BeforeClass
    public static void setUp() {
        _client =  DockerClientFactory.lazyClient();

        _networkId = createNetwork(networkName, "192.168.99.0/24");
    }

    @AfterClass
    public static void cleanUp() {
        deleteNetwork(_networkId);
    }

    protected String getContainerIpAddress(GenericContainer container) throws Exception {
        InspectContainerResponse response = _client.inspectContainerCmd(container.getContainerId()).exec();

        for (ContainerNetwork network : response.getNetworkSettings().getNetworks().values()) {
            if(_networkId.equals(network.getNetworkID())) {
                return network.getIpAddress();
            }
        }

        throw new Exception("The container is not connected to the network " + _networkId);
    }

    protected boolean isConnectedToNetwork(GenericContainer container) {
        InspectContainerResponse response = _client.inspectContainerCmd(container.getContainerId()).exec();

        for (ContainerNetwork network : response.getNetworkSettings().getNetworks().values()) {
            if(_networkId.equals(network.getNetworkID())) {
                return true;
            }
        }

        return false;
    }

    protected String getNetworkName() {
        return networkName;
    }

    protected static String createNetwork(String name, String cidr) {
        com.github.dockerjava.api.model.Network.Ipam.Config ipamConfig = new com.github.dockerjava.api.model.Network.Ipam.Config();
        ipamConfig.withSubnet(cidr);

        com.github.dockerjava.api.model.Network.Ipam ipam = new com.github.dockerjava.api.model.Network.Ipam();
        ipam.withConfig(ipamConfig);

        CreateNetworkResponse response = _client.createNetworkCmd()
                .withName(name)
                .withIpam(ipam)
                .exec();

        return response.getId();
    }

    protected static void deleteNetwork(String id){
        _client.removeNetworkCmd(id).exec();
    }
}
import java.util.ArrayList;
import java.util.List;

public class Provider {
    private List<Tariff> tariffs = new ArrayList<>();
    private List<Client> clients = new ArrayList<>();

    public void addTariff(Tariff tariff) {
        tariffs.add(tariff);
    }

    public void addClient(Client client) {
        clients.add(client);
    }

    public List<Tariff> getTariffs() {
        return tariffs;
    }

    public List<Client> getClients() {
        return clients;
    }

    public Client findTopPayer() {
        Client top = null;
        double maxCost = 0;
        for (Client client : clients) {
            double cost = client.getCost();
            if (cost > maxCost) {
                maxCost = cost;
                top = client;
            }
        }
        return top;
    }
}

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Provider implements Serializable {
    private static final long serialVersionUID = 1L;

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

    public void sortTariffsByPrice() {
        tariffs.sort(Comparator.comparingDouble(Tariff::getpriceMounth));
    }

    public void sortTariffsByType() {
        tariffs.sort(Comparator.comparing(Tariff::getType));
    }

    public void sortClientsByName() {
        clients.sort(Comparator.comparing(Client::getName));
    }

    public void sortClientsByCost() {
        clients.sort(Comparator.comparingDouble(Client::getCost).reversed());
    }

    // Сохранение и загрузка как раньше
    public void saveToFile(String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(this);
        }
    }

    public static Provider loadFromFile(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            return (Provider) ois.readObject();
        }
    }
}

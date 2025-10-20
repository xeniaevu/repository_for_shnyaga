public final class ComputerContainer {

    private static ComputerContainer INSTANCE; // единственный экземпляр
    private Monitor monitor; // управляемый объект

    // Приватный конструктор — нельзя создать через new
    private ComputerContainer() {
        this.monitor = new Monitor("Samsung", 27.0, 144); // пример инициализации
    }

    // Глобальная точка доступа к единственному экземпляру
    public static ComputerContainer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ComputerContainer();
        }
        return INSTANCE;
    }

    // Методы работы с монитором
    public Monitor getMonitor() {
        return monitor;
    }

    public void setMonitor(Monitor monitor) {
        this.monitor = monitor;
    }

    public void showConfiguration() {
        System.out.println("Компьютер подключен к монитору:");
        System.out.println(monitor);

    }
}

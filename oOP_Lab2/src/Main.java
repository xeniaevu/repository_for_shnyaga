public class Main {
    public static void main(String[] args) {
        ComputerContainer computer = ComputerContainer.getInstance();

        computer.showConfiguration();

        // Можно заменить монитор
        Monitor lg = new Monitor("LG", 32.0, 165);
        computer.setMonitor(lg);

        // Проверим, что экземпляр всё тот же
        ComputerContainer anotherReference = ComputerContainer.getInstance();
        anotherReference.showConfiguration();

        //System.out.println("Оба объекта одинаковы? " + (computer == anotherReference));
    }
}

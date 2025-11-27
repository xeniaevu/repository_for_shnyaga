public class Tariff {
    private int id;               // id из таблицы tariffs
    private String type;          // тип тарифа ("LOW", "MEDIUM", "HIGH")
    private double priceMonth;    // абонентская плата
    private double pricePerMb;    // цена за 1 Мбайт

    // Конструктор для загрузки тарифа из БД
    public Tariff(int id, String type, double priceMonth, double pricePerMb) {
        this.id = id;
        this.type = type;
        this.priceMonth = priceMonth;
        this.pricePerMb = pricePerMb;
    }

    // Конструктор для создания нового тарифа (без id)
    public Tariff(String type, double priceMonth, double pricePerMb) {
        this.type = type;
        this.priceMonth = priceMonth;
        this.pricePerMb = pricePerMb;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getPriceMonth() { return priceMonth; }
    public void setPriceMonth(double priceMonth) { this.priceMonth = priceMonth; }

    public double getPricePerMb() { return pricePerMb; }
    public void setPricePerMb(double pricePerMb) { this.pricePerMb = pricePerMb; }

    // Метод для расчёта стоимости по тарифу
    public double calculateCost(double mbUsed) {
        return priceMonth + mbUsed * pricePerMb;
    }

    @Override
    public String toString() {
        return type + " (Абонплата: " + priceMonth + ", Цена/Мбайт: " + pricePerMb + ")";
    }
}

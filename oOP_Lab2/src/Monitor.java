public class Monitor {
    private String brand;
    private double size;
    private int refreshRate;

    public Monitor(String brand, double size, int refreshRate) {
        this.brand = brand;
        this.size = size;
        this.refreshRate = refreshRate;
    }

    // Геттеры и сеттеры
    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public int getRefreshRate() {
        return refreshRate;
    }

    public void setRefreshRate(int refreshRate) {
        this.refreshRate = refreshRate;
    }

    @Override
    public String toString() {
        return "Monitor {" +
                "brand='" + brand + '\'' +
                ", size=" + size +
                ", refreshRate=" + refreshRate + " Hz" +
                '}';
    }
}


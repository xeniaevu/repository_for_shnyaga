class Complex {
    private double real;
    private double imag; //мнимая

    public Complex(double r, double i) {
        real = r;
        imag = i;
    }

    //метод add типо оператор сложения
    public Complex add(Complex other) {
        return new Complex(this.real + other.real, this.imag + other.imag);
    }

    public void display() {
        System.out.println(real + " + " + imag + "i");
    }
}

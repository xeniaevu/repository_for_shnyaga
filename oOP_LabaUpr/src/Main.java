public class Main {
    public static void main(String[] args) {
        System.out.println("Упражнение 1. Множественное наследование через интерфейсы:");
        C cObj = new C();
        cObj.methodA();
        cObj.methodB();

        System.out.println("\nУпражнение 2. Переопределение операторов через методы:");
        Complex c1 = new Complex(2, 3);
        Complex c2 = new Complex(4, 5);
        Complex cSum = c1.add(c2);
        System.out.print("Сумма: ");
        cSum.display();

        System.out.println("\nУпражнение 3. Передача объектов в методы:");
        Printer printer = new Printer();
        printer.printComplex(cSum);
    }
}

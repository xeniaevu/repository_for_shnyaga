public class C implements A, B {
    @Override
    public void methodA() {
        System.out.println("Method A из интерфейса A");
    }

    @Override
    public void methodB() {
        System.out.println("Method B из интерфейса B");
    }
}

package chain;

public class Test {
    public static void main(String[] args) {
        Block first = new Block("the first block", "0");
        System.out.println("hash for the first one: " + first.hash);

        Block second = new Block("the second block", first.hash);
        System.out.println("hash for the second one: " + second.hash);

        Block third = new Block("the third block", second.hash);
        System.out.println("hash for the third one: " + third.hash);

    }
}

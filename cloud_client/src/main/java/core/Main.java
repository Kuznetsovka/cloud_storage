package core;

public class Main  {
    static int id;
    public static void main(String[] args) {
        new Client(id++,"1.txt");
        new Client(id++,"1.txt");
        new Client(id++,"1.txt");
    }
}

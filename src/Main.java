import java.awt.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
        Controller controller = new Controller();
        World world = new World(controller);
        GUI gui = new GUI(controller);

        Kirby kirby = new Kirby(null);
        world.addKirby(0, kirby);
        controller.setPlayerKirby(kirby);

        controller.start();
    }
}

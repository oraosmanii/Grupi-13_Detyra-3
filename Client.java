import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try{

            Socket socket = new Socket("localhost", 12345);
            ObjectOutputStream clientOut=new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream clientIn = new ObjectInputStream(socket.getInputStream());

            Scanner scanner = new Scanner(System.in);
            System.out.println("Jepni nje mesazh per te derguar tek serveri:");
            String message = scanner.nextLine();

            System.out.println("Klienti po e dergon nje mesazh tek serveri");
            clientOut.writeObject(message);

            socket.close();
            scanner.close();


        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}

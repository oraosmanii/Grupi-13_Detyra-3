import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        try{

            Socket socket = new Socket("localhost", 12345);
            ObjectOutputStream clientOut=new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream clientIn = new ObjectInputStream(socket.getInputStream());

            System.out.println("Klienti po e dergon nje mesazh tek serveri");
            clientOut.writeObject("Mesazhi per serverin");



            socket.close();



        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}

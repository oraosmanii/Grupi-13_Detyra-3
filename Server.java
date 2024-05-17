import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        try{

            ServerSocket serverSocket = new ServerSocket(12345);
            Socket clientSocket = serverSocket.accept();

            System.out.println("Serveri eshte duke degjuar!");

            ObjectInputStream serverIn = new ObjectInputStream(clientSocket.getInputStream());
            ObjectOutputStream serverOut=new ObjectOutputStream(clientSocket.getOutputStream());

            String message = (String) serverIn.readObject();
            System.out.println("Mesazhi i pranuar: "+ message);



            serverSocket.close();
            clientSocket.close();



        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}

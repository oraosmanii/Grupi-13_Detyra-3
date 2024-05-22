import java.io.*;
import java.net.Socket;
import java.security.PublicKey;
import java.util.Scanner;

public class Client {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";


    private static boolean asked = false;

    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 12345);
             ObjectOutputStream clientOut = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream clientIn = new ObjectInputStream(socket.getInputStream());
             Scanner scanner = new Scanner(System.in)) {

            RSA rsa = new RSA();

            // Receive server's public key
            PublicKey serverPublicKey = (PublicKey) clientIn.readObject();
            // Send client's public key to server
            clientOut.writeObject(rsa.getPublicKey());

            System.out.println(ANSI_GREEN + "Connected to server. Exchanging public keys..." + ANSI_RESET);
            System.out.println(ANSI_GREEN + "Public key exchange complete. You can now send secure messages." + ANSI_RESET);

            // Create a thread to listen for messages from the server
            new Thread(() -> {
                try {
                    while (true) {
                        byte[] encryptedResponse = (byte[]) clientIn.readObject();
                        String decryptedResponse = new String(rsa.decrypt(encryptedResponse, rsa.getPrivateKey()));
                        System.out.println(ANSI_BLUE + "\nDecrypted message from server: " + decryptedResponse + ANSI_RESET);
                    }
                } catch (Exception e) {
                    System.err.println(ANSI_RED + "Error: " + e.getMessage() + ANSI_RESET);
                    e.printStackTrace();
                }
            }).start();

            while (true) {
                if (!asked) {
                    System.out.print("\n" + ANSI_YELLOW + "Do you want to send a message? (y/n): " + ANSI_RESET);
                    String choice = scanner.nextLine();
                    if (choice.equalsIgnoreCase("y")) {
                        System.out.print(ANSI_YELLOW + "Enter message: " + ANSI_RESET);
                        String message = scanner.nextLine();

                        byte[] encryptedMessage = rsa.encrypt(message.getBytes(), serverPublicKey);
                        String base64Message = RSA.toBase64(encryptedMessage);
                        System.out.println(ANSI_BLUE + "Encrypted message sent to server (Base64): " + base64Message + ANSI_RESET);

                        clientOut.writeObject(encryptedMessage);
                    } else if (choice.equalsIgnoreCase("n")) {
                        System.out.println(ANSI_YELLOW + "Waiting to receive messages..." + ANSI_RESET);
                    }
                    asked = true;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_CYAN = "\u001B[36m";

    private static Map<Socket, PublicKey> clientPublicKeys = new ConcurrentHashMap<>();
    private static Map<Socket, ObjectOutputStream> clientOutputStreams = new ConcurrentHashMap<>();
    private static Map<Integer, Socket> clientSockets = new ConcurrentHashMap<>();
    private static int clientIdCounter = 0;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println(ANSI_GREEN + "Server started and listening for connections..." + ANSI_RESET);


            new Thread(() -> {
                Scanner scanner = new Scanner(System.in);
                while (true) {
                    System.out.print("\n" + ANSI_YELLOW + "Do you want to send a message? (y/n): " + ANSI_RESET);
                    String choice = scanner.nextLine();
                    if (choice.equalsIgnoreCase("y")) {
                        System.out.print(ANSI_YELLOW + "Enter client ID to send a message: " + ANSI_RESET);
                        int clientId = Integer.parseInt(scanner.nextLine());
                        System.out.print(ANSI_YELLOW + "Enter message to send to client " + clientId + ": " + ANSI_RESET);
                        String message = scanner.nextLine();
                        try {
                            sendMessageToClient(clientId, message);
                        } catch (Exception e) {
                            System.err.println(ANSI_RED + "Error sending message to client " + clientId + ": " + e.getMessage() + ANSI_RESET);
                        }
                    } else if (choice.equalsIgnoreCase("n")) {
                        System.out.println(ANSI_YELLOW + "Waiting to receive messages..." + ANSI_RESET);
                    }
                }
            }).start();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                clientIdCounter++;
                int clientId = clientIdCounter;
                System.out.println(ANSI_GREEN + "\nNew client connected with ID " + clientId + ". Exchanging public keys..." + ANSI_RESET);
                new ClientHandler(clientSocket, clientId).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void sendMessageToClient(int clientId, String message) throws Exception {
        RSA rsa = new RSA();
        Socket clientSocket = clientSockets.get(clientId);

        if (clientSocket != null) {
            PublicKey clientPublicKey = clientPublicKeys.get(clientSocket);
            ObjectOutputStream out = clientOutputStreams.get(clientSocket);

            if (out != null) {
                byte[] encryptedMessage = rsa.encrypt(message.getBytes(), clientPublicKey);
                String base64Message = RSA.toBase64(encryptedMessage);
                System.out.println(ANSI_BLUE + "Sending encrypted message to client " + clientId + " (Base64): " + base64Message + ANSI_RESET);
                out.writeObject(encryptedMessage);
                out.flush();
            }
        } else {
            System.out.println(ANSI_RED + "Client with ID " + clientId + " not found." + ANSI_RESET);
        }
    }

    private static class ClientHandler extends Thread {
        private Socket clientSocket;
        private RSA rsa;
        private int clientId;

        public ClientHandler(Socket socket, int clientId) {
            this.clientSocket = socket;
            this.clientId = clientId;
            try {
                this.rsa = new RSA();
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }

        public void run() {
            try (ObjectOutputStream serverOut = new ObjectOutputStream(clientSocket.getOutputStream());
                 ObjectInputStream serverIn = new ObjectInputStream(clientSocket.getInputStream())) {


                serverOut.writeObject(rsa.getPublicKey());


                PublicKey clientPublicKey = (PublicKey) serverIn.readObject();
                clientPublicKeys.put(clientSocket, clientPublicKey);
                clientOutputStreams.put(clientSocket, serverOut);
                clientSockets.put(clientId, clientSocket);

                System.out.println(ANSI_GREEN + "Public key exchange complete with client " + clientId + ". Ready to receive encrypted messages from clients." + ANSI_RESET);

                while (true) {
                    byte[] encryptedMessage = (byte[]) serverIn.readObject();
                    String decryptedMessage = new String(rsa.decrypt(encryptedMessage, rsa.getPrivateKey()));
                    System.out.println(ANSI_CYAN + "\n[CLIENT " + clientId + "] Decrypted message: " + decryptedMessage + ANSI_RESET);
                }
            } catch (Exception e) {
                System.err.println(ANSI_RED + "Error with client " + clientId + ": " + e.getMessage() + ANSI_RESET);
            } finally {
                clientPublicKeys.remove(clientSocket);
                clientOutputStreams.remove(clientSocket);
                clientSockets.remove(clientId);
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

package Server;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
    private static final int PORT = 12345;
    private static Map<String, ClientHandler> clients = new HashMap<>();
    private static ExecutorService executor = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Serveur démarré sur le port " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nouveau client connecté : " + clientSocket);
                executor.execute(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("Erreur du serveur : " + e.getMessage());
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            // Demander le nom d'utilisateur au client
            String username = in.readLine();
            if (username == null || username.trim().isEmpty()) {
                clientSocket.close();
                return;
            }

            synchronized (clients) {
                if (clients.containsKey(username)) {
                    out.println("ERROR:Utilisateur déjà connecté");
                    clientSocket.close();
                    return;
                }
                ClientHandler clientHandler = new ClientHandler(username, clientSocket, in, out);
                clients.put(username, clientHandler);
                System.out.println("Utilisateur connecté : " + username);
            }

            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("SYSTEM:")) {
                    String[] parts = message.split(":", 3);
                    if (parts.length == 3) {
                        String type = parts[1];
                        String content = parts[2];
                        if (type.equals("DISCONNECT")) {
                            synchronized (clients) {
                                clients.remove(username);
                                System.out.println("Utilisateur déconnecté : " + username);
                            }
                            break;
                        } else if (type.equals("CONTACT_REQUEST") || type.equals("TYPING") || type.equals("STOP_TYPING")) {
                            String[] contentParts = content.split(":", 2);
                            if (contentParts.length == 2) {
                                String sender = contentParts[0];
                                String target = contentParts[1];
                                ClientHandler targetHandler = clients.get(target);
                                if (targetHandler != null) {
                                    targetHandler.sendMessage("SYSTEM:" + type + ":" + sender + ":" + target);
                                }
                            }
                        }
                    }
                } else if (message.contains(": ")) {
                    String[] parts = message.split(": ", 2);
                    if (parts.length == 2) {
                        String sender = parts[0];
                        String content = parts[1];
                        // Relayer le message au destinataire (on suppose que le message est envoyé au selectedContact)
                        ClientHandler senderHandler = clients.get(sender);
                        if (senderHandler != null && senderHandler.getSelectedContact() != null) {
                            String target = senderHandler.getSelectedContact();
                            ClientHandler targetHandler = clients.get(target);
                            if (targetHandler != null) {
                                targetHandler.sendMessage(message);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la gestion du client : " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Erreur lors de la fermeture du socket : " + e.getMessage());
            }
        }
    }

    private static class ClientHandler {
        private String username;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String selectedContact; // Contact actuellement sélectionné

        public ClientHandler(String username, Socket socket, BufferedReader in, PrintWriter out) {
            this.username = username;
            this.socket = socket;
            this.in = in;
            this.out = out;
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        public void setSelectedContact(String selectedContact) {
            this.selectedContact = selectedContact;
        }

        public String getSelectedContact() {
            return selectedContact;
        }
    }
}
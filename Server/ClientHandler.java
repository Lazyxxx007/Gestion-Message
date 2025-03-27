package Server;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private OutputStream outStream;
    private String username;
    private static Map<String, ClientHandler> clients = new HashMap<>();

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public PrintWriter getOut() {
        return out;
    }

    public OutputStream getOutStream() {
        return outStream;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            outStream = socket.getOutputStream();

            username = in.readLine();
            if (username == null || username.trim().isEmpty()) {
                throw new IOException("Nom d'utilisateur invalide");
            }

            synchronized (clients) {
                clients.put(username, this);
                broadcastUserList();
                System.out.println("Utilisateur connecté : " + username);
            }

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Message reçu du client " + username + " : " + message);
                if (message.startsWith("MSG:")) {
                    String[] parts = message.split(":", 3);
                    String recipient = parts[1];
                    String content = parts[2];
                    sendToRecipient(username, recipient, content);
                } else if (message.startsWith("FILE:")) {
                    handleFile(username, message);
                } else if (message.startsWith("REMOVE_USER:")) {
                    String userToRemove = message.substring(12);
                    broadcastUserList();
                } else if (message.startsWith("UPDATE_USERNAME:")) {
                    String[] parts = message.substring(15).split(":");
                    String oldUsername = parts[0];
                    String newUsername = parts[1];
                    synchronized (clients) {
                        if (clients.containsKey(oldUsername)) {
                            ClientHandler client = clients.remove(oldUsername);
                            client.username = newUsername;
                            clients.put(newUsername, client);
                            broadcastUserList();
                        }
                    }
                } else {
                    broadcast(username + ": " + message);
                }
            }
        } catch (IOException e) {
            System.out.println("❌ Erreur client (" + username + ") : " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    private void sendToRecipient(String sender, String recipient, String message) {
        synchronized (clients) {
            ClientHandler recipientHandler = clients.get(recipient);
            if (recipientHandler != null) {
                recipientHandler.getOut().println(sender + ": " + message);
                recipientHandler.getOut().flush();
                System.out.println("Message envoyé à " + recipient + " : " + message);
            } else {
                System.out.println("Destinataire non trouvé : " + recipient);
                out.println("System: " + recipient + " is not online");
                out.flush();
            }
        }
    }

    private void handleFile(String sender, String message) throws IOException {
        String[] parts = message.split(":", 5);
        String recipient = parts[1];
        String fileName = parts[2];
        long fileSize = Long.parseLong(parts[3]);
        String fileType = parts[4];

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        long bytesReceived = 0;
        int bytesRead;

        InputStream is = socket.getInputStream();
        while (bytesReceived < fileSize && (bytesRead = is.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
            bytesReceived += bytesRead;
        }

        byte[] fileData = baos.toByteArray();

        synchronized (clients) {
            ClientHandler recipientHandler = clients.get(recipient);
            if (recipientHandler != null) {
                recipientHandler.getOut().println(message);
                recipientHandler.getOut().flush();

                OutputStream recipientOutStream = recipientHandler.getOutStream();
                recipientOutStream.write(fileData);
                recipientOutStream.flush();
            } else {
                System.out.println("Destinataire non trouvé pour le fichier : " + recipient);
                out.println("System: " + recipient + " is not online");
                out.flush();
            }
        }
    }

    private void broadcast(String message) {
        synchronized (clients) {
            for (ClientHandler client : clients.values()) {
                client.getOut().println(message);
                client.getOut().flush();
            }
        }
    }

    private void broadcastUserList() {
        String userList = "USERLIST:" + String.join(",", clients.keySet());
        System.out.println("Envoi de la liste des utilisateurs : " + userList);
        broadcast(userList);
    }

    private void closeConnection() {
        synchronized (clients) {
            if (username != null) {
                clients.remove(username);
                broadcastUserList();
                System.out.println("Utilisateur déconnecté : " + username);
            }
        }
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (outStream != null) outStream.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.out.println("❌ Erreur fermeture connexion : " + e.getMessage());
        }
    }
}
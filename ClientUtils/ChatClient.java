package ClientUtils;

import java.io.*;
import java.net.Socket;

public class ChatClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;

    // Constructeur utilisé dans LoginGUI
    public ChatClient(Socket socket, String username) throws IOException {
        this.username = username;
        this.socket = socket;
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    // Constructeur alternatif (pour compatibilité avec d'autres usages potentiels)
    public ChatClient(String host, int port, String username) throws IOException {
        this.username = username;
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    // Envoyer un message système (par exemple, "DISCONNECT:username" ou "CONTACT_REQUEST:username:target")
    public void sendSystemMessage(String type, String content) throws IOException {
        if (out == null) {
            throw new IOException("Le flux de sortie n'est pas initialisé (connexion probablement fermée).");
        }
        out.println(type + ":" + content);
        if (out.checkError()) {
            throw new IOException("Erreur lors de l'envoi du message système au serveur.");
        }
    }

    // Envoyer un message de chat au serveur
    public void sendMessage(String message) throws IOException {
        if (out == null) {
            throw new IOException("Le flux de sortie n'est pas initialisé (connexion probablement fermée).");
        }
        out.println(username + ": " + message);
        if (out.checkError()) {
            throw new IOException("Erreur lors de l'envoi du message au serveur.");
        }
    }

    // Recevoir un message du serveur
    public String receiveMessage() throws IOException {
        if (in == null) {
            throw new IOException("Le flux d'entrée n'est pas initialisé (connexion probablement fermée).");
        }
        return in.readLine(); // Lit une ligne du flux d'entrée
    }

    // Fermer la connexion
    public void close() throws IOException {
        try {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (socket != null) {
                socket.close();
            }
        } finally {
            // Assurer que les ressources sont bien libérées
            out = null;
            in = null;
            socket = null;
        }
    }

    // Obtenir le nom d'utilisateur
    public String getUsername() {
        return username;
    }
}
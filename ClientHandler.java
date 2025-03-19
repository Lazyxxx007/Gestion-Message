import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Gère la communication avec un client.
 */
public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private static Map<String, PrintWriter> clients = new HashMap<>(); // Stocke les utilisateurs connectés

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Lecture du pseudo de l'utilisateur
            String username = in.readLine();
            synchronized (clients) {
                clients.put(username, out);
            }

            String message;
            while ((message = in.readLine()) != null) {
                handleIncomingMessage(username, message);
            }
        } catch (IOException e) {
            System.out.println("❌ Erreur client : " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    /**
     * Gère un message entrant.
     * @param sender Nom de l'expéditeur.
     * @param message Contenu du message.
     */
    private void handleIncomingMessage(String sender, String message) {
        synchronized (clients) {
            for (Map.Entry<String, PrintWriter> client : clients.entrySet()) {
                client.getValue().println(sender + ": " + message);
            }
        }
    }

    /**
     * Indique aux autres utilisateurs qu'un utilisateur est en train d'écrire.
     * @param sender Expéditeur du message.
     * @param receiver Destinataire du message.
     * @param isTyping Indique si l'utilisateur est en train d'écrire.
     */
    public void notifyTypingStatus(String sender, String receiver, boolean isTyping) {
        synchronized (clients) {
            PrintWriter receiverOut = clients.get(receiver);
            if (receiverOut != null) {
                receiverOut.println(sender + (isTyping ? " est en train d’écrire..." : " a arrêté d’écrire."));
            }
        }
    }

    /**
     * Ferme la connexion du client proprement.
     */
    private void closeConnection() {
        try {
            socket.close();
            in.close();
            out.close();
        } catch (IOException e) {
            System.out.println("❌ Erreur fermeture connexion : " + e.getMessage());
        }
    }
}

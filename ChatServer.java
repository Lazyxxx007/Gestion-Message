import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Classe principale du serveur.
 * - Accepte les connexions des clients.
 * - Assigne chaque client à un thread via ClientHandler.
 */
public class ChatServer {
    private ServerSocket serverSocket;
    private ExecutorService pool = Executors.newFixedThreadPool(10); // Pool de threads pour gérer plusieurs clients

    /**
     * Démarre le serveur sur le port donné.
     * @param port Numéro du port sur lequel le serveur écoute.
     */
    public void startServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("🚀 Serveur démarré sur le port " + port);

            while (true) {
                // Accepte une connexion client et démarre un thread pour le gérer
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                pool.execute(clientHandler);
            }
        } catch (IOException e) {
            System.out.println("❌ Erreur serveur : " + e.getMessage());
        }
    }

    /**
     * Arrête le serveur proprement.
     */
    public void stopServer() {
        try {
            if (serverSocket != null) serverSocket.close();
            pool.shutdown();
            System.out.println("🛑 Serveur arrêté.");
        } catch (IOException e) {
            System.out.println("❌ Erreur lors de l'arrêt du serveur : " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.startServer(1234); // Démarre sur le port 1234
    }
}

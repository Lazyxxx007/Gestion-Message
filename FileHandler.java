import java.io.*;
import java.net.Socket;
import java.nio.file.*;

/**
 * Gère l'envoi et la réception de fichiers (images, vidéos, etc.).
 */
public class FileHandler {
    /**
     * Envoie un fichier à un client.
     * @param socket Socket du client destinataire.
     * @param file Fichier à envoyer.
     */
    public static void sendFile(Socket socket, File file) throws IOException {
        OutputStream out = socket.getOutputStream();
        FileInputStream fileIn = new FileInputStream(file);
        byte[] buffer = new byte[4096];
        int bytesRead;

        while ((bytesRead = fileIn.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }

        fileIn.close();
        out.flush();
    }

    /**
     * Reçoit un fichier envoyé par un client et l'enregistre.
     * @param socket Socket du client émetteur.
     * @param savePath Emplacement où enregistrer le fichier.
     */
    public static void receiveFile(Socket socket, String savePath) throws IOException {
        InputStream in = socket.getInputStream();
        FileOutputStream fileOut = new FileOutputStream(savePath);
        byte[] buffer = new byte[4096];
        int bytesRead;

        while ((bytesRead = in.read(buffer)) != -1) {
            fileOut.write(buffer, 0, bytesRead);
        }

        fileOut.close();
    }
}

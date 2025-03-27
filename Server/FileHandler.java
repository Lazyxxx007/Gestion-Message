package Server;

import java.io.*;
import java.net.Socket;

public class FileHandler {
    public static void sendFile(Socket socket, String recipient, File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {
            // Envoyer les métadonnées du fichier
            dos.writeUTF("FILE:" + recipient + ":" + file.getName() + ":" + file.length() + ":binary");
            // Envoyer le contenu du fichier
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, bytesRead);
            }
            dos.flush();
        }
    }

    public static void receiveFile(Socket socket, String fileName) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(fileName);
             DataInputStream dis = new DataInputStream(socket.getInputStream())) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = dis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            fos.flush();
        }
    }
}
package Client;

import Modeles.Message;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class MessageManager {
    private Connection connecter() {
        // URL mise à jour avec useSSL=false et serverTimezone=Europe/Paris
        String url = "jdbc:mysql://localhost:3306/messagerie?user=root&password=&useSSL=false&serverTimezone=Europe/Paris";
        Connection connexion = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connexion = DriverManager.getConnection(url);
            System.out.println("Connexion réussie à la base de données 'messagerie' !");
        } catch (SQLException e) {
            System.err.println("Erreur de connexion à la base de données 'messagerie' : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Pilote MySQL non trouvé : " + e.getMessage());
        }
        return connexion;
    }

    public void sauvegarderMessage(String expediteur, String destinataire, String contenu, Timestamp horodatage) {
        String sql = "INSERT INTO Messages(expediteur, destinataire, contenu, horodatage) VALUES(?, ?, ?, ?)";
        try (Connection connexion = this.connecter();
             PreparedStatement pstmt = connexion.prepareStatement(sql)) {
            pstmt.setString(1, expediteur);
            pstmt.setString(2, destinataire);
            pstmt.setString(3, contenu);
            pstmt.setTimestamp(4, horodatage);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la sauvegarde du message : " + e.getMessage());
        }
    }

    public List<Message> obtenirMessages(String user1, String user2) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT expediteur, destinataire, contenu, horodatage FROM Messages WHERE (expediteur = ? AND destinataire = ?) OR (expediteur = ? AND destinataire = ?) ORDER BY horodatage";
        try (Connection connexion = this.connecter();
             PreparedStatement pstmt = connexion.prepareStatement(sql)) {
            pstmt.setString(1, user1);
            pstmt.setString(2, user2);
            pstmt.setString(3, user2);
            pstmt.setString(4, user1);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                messages.add(new Message(
                    rs.getString("expediteur"),
                    rs.getString("destinataire"),
                    rs.getString("contenu"),
                    rs.getTimestamp("horodatage")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des messages : " + e.getMessage());
        }
        return messages;
    }
}
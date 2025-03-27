package Client;

import Modeles.Utilisateur;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class UserManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/messagerie?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Paris";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    // Vérifie si un utilisateur existe dans la base de données
    public boolean utilisateurExiste(String username) {
        String sql = "SELECT * FROM Utilisateurs WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de l'utilisateur : " + e.getMessage());
            return false;
        }
    }

    // Ajoute un utilisateur à la base de données
    public boolean ajouterUtilisateur(String username, String firstName, String lastName, String motDePasseHache) {
        String sql = "INSERT INTO Utilisateurs (username, firstName, lastName, motDePasseHache, statut, derniereConnexion) VALUES (?, ?, ?, ?, 'hors ligne', NOW())";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, firstName);
            pstmt.setString(3, lastName);
            pstmt.setString(4, motDePasseHache);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de l'utilisateur : " + e.getMessage());
            return false;
        }
    }

    // Vérifie les identifiants de connexion
    public boolean verifierIdentifiants(String username, String motDePasseHache) {
        String sql = "SELECT * FROM Utilisateurs WHERE username = ? AND motDePasseHache = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, motDePasseHache);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification des identifiants : " + e.getMessage());
            return false;
        }
    }

    // Obtient un utilisateur à partir de son username
    public Utilisateur obtenirUtilisateur(String username) {
        String sql = "SELECT * FROM Utilisateurs WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Utilisateur(
                    rs.getString("username"),
                    rs.getString("firstName"),
                    rs.getString("lastName"),
                    rs.getString("motDePasseHache"),
                    rs.getString("statut"),
                    rs.getTimestamp("derniereConnexion")
                );
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'utilisateur : " + e.getMessage());
        }
        return null;
    }

    // Met à jour le statut d'un utilisateur
    public boolean mettreAJourStatutUtilisateur(String username, String statut) {
        String sql = "UPDATE Utilisateurs SET statut = ?, derniereConnexion = NOW() WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, statut);
            pstmt.setString(2, username);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du statut : " + e.getMessage());
            return false;
        }
    }

    // Obtient le statut d'un utilisateur
    public String obtenirStatutUtilisateur(String username) {
        String sql = "SELECT statut FROM Utilisateurs WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("statut");
            }
            return "hors ligne";
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du statut : " + e.getMessage());
            return "hors ligne";
        }
    }

    // Obtient le temps écoulé depuis la dernière connexion
    public String obtenirTempsDepuisDerniereConnexion(String username) {
        String sql = "SELECT derniereConnexion FROM Utilisateurs WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Timestamp lastConnection = rs.getTimestamp("derniereConnexion");
                if (lastConnection != null) {
                    long diff = System.currentTimeMillis() - lastConnection.getTime();
                    long minutes = diff / (60 * 1000);
                    if (minutes < 60) {
                        return minutes + " minutes";
                    } else {
                        long hours = minutes / 60;
                        return hours + " heures";
                    }
                }
            }
            return "inconnu";
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du temps depuis la dernière connexion : " + e.getMessage());
            return "inconnu";
        }
    }

    // Obtient tous les contacts non archivés d'un utilisateur
    public List<String> obtenirContacts(String username) {
        List<String> contacts = new ArrayList<>();
        String sql = "SELECT utilisateur1, utilisateur2 FROM Contacts WHERE (utilisateur1 = ? OR utilisateur2 = ?) AND statut = 'accepte' AND archive = 0";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, username);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String contact = rs.getString("utilisateur1").equals(username) ? rs.getString("utilisateur2") : rs.getString("utilisateur1");
                contacts.add(contact);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des contacts : " + e.getMessage());
        }
        return contacts;
    }

    // Obtient tous les contacts archivés d'un utilisateur
    public List<String> obtenirContactsArchives(String username) {
        List<String> archivedContacts = new ArrayList<>();
        String sql = "SELECT utilisateur1, utilisateur2 FROM Contacts WHERE (utilisateur1 = ? OR utilisateur2 = ?) AND statut = 'accepte' AND archive = 1";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, username);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String contact = rs.getString("utilisateur1").equals(username) ? rs.getString("utilisateur2") : rs.getString("utilisateur1");
                archivedContacts.add(contact);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des contacts archivés : " + e.getMessage());
        }
        return archivedContacts;
    }

    // Vérifie si une relation est archivée
    public boolean estArchive(String user, String contact) {
        String sql = "SELECT archive FROM Contacts WHERE ((utilisateur1 = ? AND utilisateur2 = ?) OR (utilisateur1 = ? AND utilisateur2 = ?)) AND statut = 'accepte'";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user);
            pstmt.setString(2, contact);
            pstmt.setString(3, contact);
            pstmt.setString(4, user);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                boolean isArchived = rs.getBoolean("archive");
                System.out.println("Vérification archivage - User: " + user + ", Contact: " + contact + ", Résultat: " + isArchived);
                return isArchived;
            }
            System.out.println("Aucune relation trouvée pour User: " + user + ", Contact: " + contact);
            return false;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de l'archivage : " + e.getMessage());
            return false;
        }
    }

    // Archive un contact
    public boolean archiverContact(String user, String contact) {
        String sql = "UPDATE Contacts SET archive = 1 WHERE ((utilisateur1 = ? AND utilisateur2 = ?) OR (utilisateur1 = ? AND utilisateur2 = ?)) AND statut = 'accepte'";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user);
            pstmt.setString(2, contact);
            pstmt.setString(3, contact);
            pstmt.setString(4, user);
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Contact archivé - User: " + user + ", Contact: " + contact + ", Lignes affectées: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'archivage du contact : " + e.getMessage());
            return false;
        }
    }

    // Désarchive un contact
    public boolean desarchiverContact(String user, String contact) {
        String sql = "UPDATE Contacts SET archive = 0 WHERE ((utilisateur1 = ? AND utilisateur2 = ?) OR (utilisateur1 = ? AND utilisateur2 = ?)) AND statut = 'accepte'";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user);
            pstmt.setString(2, contact);
            pstmt.setString(3, contact);
            pstmt.setString(4, user);
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Contact désarchivé - User: " + user + ", Contact: " + contact + ", Lignes affectées: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors du désarchivage du contact : " + e.getMessage());
            return false;
        }
    }

    // Vérifie si un contact est bloqué
    public boolean estBloque(String user, String contact) {
        String sql = "SELECT bloque FROM Contacts WHERE ((utilisateur1 = ? AND utilisateur2 = ?) OR (utilisateur1 = ? AND utilisateur2 = ?)) AND statut = 'accepte'";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user);
            pstmt.setString(2, contact);
            pstmt.setString(3, contact);
            pstmt.setString(4, user);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("bloque");
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification du blocage : " + e.getMessage());
            return false;
        }
    }

    // Obtient tous les utilisateurs qui ne sont pas encore des contacts
    public List<String> obtenirTousLesUtilisateursNonContacts(String username) {
        List<String> nonContacts = new ArrayList<>();
        String sql = "SELECT username FROM Utilisateurs WHERE username != ? AND username NOT IN (" +
                    "SELECT utilisateur2 FROM Contacts WHERE utilisateur1 = ? AND statut = 'accepte' " +
                    "UNION " +
                    "SELECT utilisateur1 FROM Contacts WHERE utilisateur2 = ? AND statut = 'accepte')";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, username);
            pstmt.setString(3, username);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                nonContacts.add(rs.getString("username"));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des utilisateurs non-contacts : " + e.getMessage());
        }
        return nonContacts;
    }

    // Ajoute une demande de contact
    public boolean ajouterDemandeContact(String requester, String target) {
        String sql = "INSERT INTO Contacts (utilisateur1, utilisateur2, statut, date_demande, archive, bloque) VALUES (?, ?, 'attente', NOW(), 0, 0)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, requester);
            pstmt.setString(2, target);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de la demande de contact : " + e.getMessage());
            return false;
        }
    }

    // Obtient les demandes de contact en attente
    public List<String> obtenirDemandesEnAttente(String username) {
        List<String> pendingRequests = new ArrayList<>();
        String sql = "SELECT utilisateur1 FROM Contacts WHERE utilisateur2 = ? AND statut = 'attente'";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                pendingRequests.add(rs.getString("utilisateur1"));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des demandes en attente : " + e.getMessage());
        }
        return pendingRequests;
    }

    // Accepte une demande de contact
    public boolean accepterDemandeContact(String requester, String target) {
        String sql = "UPDATE Contacts SET statut = 'accepte', archive = 0, bloque = 0 WHERE utilisateur1 = ? AND utilisateur2 = ? AND statut = 'attente'";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, requester);
            pstmt.setString(2, target);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'acceptation de la demande de contact : " + e.getMessage());
            return false;
        }
    }

    // Refuse une demande de contact
    public boolean refuserDemandeContact(String requester, String target) {
        String sql = "DELETE FROM Contacts WHERE utilisateur1 = ? AND utilisateur2 = ? AND statut = 'attente'";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, requester);
            pstmt.setString(2, target);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors du refus de la demande de contact : " + e.getMessage());
            return false;
        }
    }

    // Supprime un contact
    public boolean supprimerContact(String user, String contact) {
        String sql = "DELETE FROM Contacts WHERE ((utilisateur1 = ? AND utilisateur2 = ?) OR (utilisateur1 = ? AND utilisateur2 = ?)) AND statut = 'accepte'";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user);
            pstmt.setString(2, contact);
            pstmt.setString(3, contact);
            pstmt.setString(4, user);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du contact : " + e.getMessage());
            return false;
        }
    }

    // Bloque un contact
    public boolean bloquerContact(String user, String contact) {
        String sql = "UPDATE Contacts SET bloque = 1 WHERE ((utilisateur1 = ? AND utilisateur2 = ?) OR (utilisateur1 = ? AND utilisateur2 = ?)) AND statut = 'accepte'";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user);
            pstmt.setString(2, contact);
            pstmt.setString(3, contact);
            pstmt.setString(4, user);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors du blocage du contact : " + e.getMessage());
            return false;
        }
    }

    // Débloque un contact
    public boolean debloquerContact(String user, String contact) {
        String sql = "UPDATE Contacts SET bloque = 0 WHERE ((utilisateur1 = ? AND utilisateur2 = ?) OR (utilisateur1 = ? AND utilisateur2 = ?)) AND statut = 'accepte'";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user);
            pstmt.setString(2, contact);
            pstmt.setString(3, contact);
            pstmt.setString(4, user);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors du déblocage du contact : " + e.getMessage());
            return false;
        }
    }

    // Obtient les messages d'une conversation entre deux utilisateurs (version améliorée)
    public List<String> obtenirMessagesConversation(String user1, String user2) {
        List<String> messages = new ArrayList<>();
        String sql = "SELECT expediteur, contenu, horodatage FROM Messages WHERE (expediteur = ? AND destinataire = ?) OR (expediteur = ? AND destinataire = ?) ORDER BY horodatage";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user1);
            pstmt.setString(2, user2);
            pstmt.setString(3, user2);
            pstmt.setString(4, user1);
            ResultSet rs = pstmt.executeQuery();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm"); // Format HH:mm pour l'heure
            while (rs.next()) {
                String sender = rs.getString("expediteur");
                String content = rs.getString("contenu");
                LocalDateTime timestamp = rs.getTimestamp("horodatage").toLocalDateTime();
                String formattedTime = timestamp.format(formatter);
                messages.add(formattedTime + " " + sender + ": " + content);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des messages : " + e.getMessage());
        }
        return messages;
    }

    // Nouvelle méthode : Obtient les messages plus récents qu'un certain horodatage
    public List<String> obtenirMessagesRecents(String user1, String user2, LocalDateTime since) {
        List<String> messages = new ArrayList<>();
        String sql = "SELECT expediteur, contenu, horodatage FROM Messages WHERE " +
                     "((expediteur = ? AND destinataire = ?) OR (expediteur = ? AND destinataire = ?)) " +
                     "AND horodatage > ? ORDER BY horodatage";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user1);
            pstmt.setString(2, user2);
            pstmt.setString(3, user2);
            pstmt.setString(4, user1);
            pstmt.setTimestamp(5, Timestamp.valueOf(since));
            ResultSet rs = pstmt.executeQuery();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            while (rs.next()) {
                String sender = rs.getString("expediteur");
                String content = rs.getString("contenu");
                LocalDateTime timestamp = rs.getTimestamp("horodatage").toLocalDateTime();
                String formattedTime = timestamp.format(formatter);
                messages.add(formattedTime + " " + sender + ": " + content);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des messages récents : " + e.getMessage());
        }
        return messages;
    }

    // Met à jour le mot de passe d'un utilisateur
    public boolean mettreAJourMotDePasse(String username, String newMotDePasseHache) {
        String sql = "UPDATE Utilisateurs SET motDePasseHache = ? WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newMotDePasseHache);
            pstmt.setString(2, username);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du mot de passe : " + e.getMessage());
            return false;
        }
    }

    // Supprime un utilisateur et toutes ses données associées
    public boolean supprimerUtilisateur(String username) {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            conn.setAutoCommit(false);

            // Supprimer les messages
            String sqlMessages = "DELETE FROM Messages WHERE expediteur = ? OR destinataire = ?";
            try (PreparedStatement pstmtMessages = conn.prepareStatement(sqlMessages)) {
                pstmtMessages.setString(1, username);
                pstmtMessages.setString(2, username);
                pstmtMessages.executeUpdate();
            }

            // Supprimer les relations de contact
            String sqlContacts = "DELETE FROM Contacts WHERE utilisateur1 = ? OR utilisateur2 = ?";
            try (PreparedStatement pstmtContacts = conn.prepareStatement(sqlContacts)) {
                pstmtContacts.setString(1, username);
                pstmtContacts.setString(2, username);
                pstmtContacts.executeUpdate();
            }

            // Supprimer l'utilisateur
            String sqlUser = "DELETE FROM Utilisateurs WHERE username = ?";
            try (PreparedStatement pstmtUser = conn.prepareStatement(sqlUser)) {
                pstmtUser.setString(1, username);
                pstmtUser.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de l'utilisateur : " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Erreur lors du rollback : " + ex.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Erreur lors de la fermeture de la connexion : " + e.getMessage());
                }
            }
        }
    }
}
package BD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BaseDeDonneesChat {
    // Établir une connexion à la base de données MySQL
    private Connection connecter() {
        String url = "jdbc:mysql://localhost:3306/chat_db?user=root&password="; // Ajustez si vous avez un mot de passe
        Connection connexion = null;
        try {
            // Charger explicitement le pilote MySQL (optionnel selon la version de Java)
            Class.forName("com.mysql.cj.jdbc.Driver");
            connexion = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.err.println("Erreur de connexion à la base de données : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Pilote MySQL non trouvé : " + e.getMessage());
        }
        return connexion;
    }

    // Gestion des utilisateurs

    // Ajouter un nouvel utilisateur dans la base de données
    public boolean creerUtilisateur(String nomUtilisateur, String motDePasseHache) {
        String sql = "INSERT INTO Utilisateurs(nomUtilisateur, motDePasseHache, statut) VALUES(?, ?, 'hors ligne')";
        try (Connection connexion = this.connecter();
             PreparedStatement pstmt = connexion.prepareStatement(sql)) {
            pstmt.setString(1, nomUtilisateur);
            pstmt.setString(2, motDePasseHache);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création de l'utilisateur : " + e.getMessage());
            return false;
        }
    }

    // Récupérer les informations d'un utilisateur à partir de son nom
    public Utilisateur obtenirUtilisateur(String nomUtilisateur) {
        String sql = "SELECT nomUtilisateur, motDePasseHache, statut FROM Utilisateurs WHERE nomUtilisateur = ?";
        try (Connection connexion = this.connecter();
             PreparedStatement pstmt = connexion.prepareStatement(sql)) {
            pstmt.setString(1, nomUtilisateur);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Utilisateur(rs.getString("nomUtilisateur"), rs.getString("motDePasseHache"), rs.getString("statut"));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'utilisateur : " + e.getMessage());
        }
        return null;
    }

    // Vérifier l'authentification d'un utilisateur
    public boolean authentifierUtilisateur(String nomUtilisateur, String motDePasseHache) {
        Utilisateur utilisateur = obtenirUtilisateur(nomUtilisateur);
        return utilisateur != null && utilisateur.obtenirMotDePasseHache().equals(motDePasseHache);
    }

    // Mettre à jour le statut d'un utilisateur (ex. "en ligne", "hors ligne")
    public boolean mettreAJourStatutUtilisateur(String nomUtilisateur, String statut) {
        String sql = "UPDATE Utilisateurs SET statut = ? WHERE nomUtilisateur = ?";
        try (Connection connexion = this.connecter();
             PreparedStatement pstmt = connexion.prepareStatement(sql)) {
            pstmt.setString(1, statut);
            pstmt.setString(2, nomUtilisateur);
            int lignesModifiees = pstmt.executeUpdate();
            return lignesModifiees > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du statut : " + e.getMessage());
            return false;
        }
    }

    // Gestion des messages

    // Sauvegarder un message envoyé dans la base de données
    public boolean sauvegarderMessage(String expediteur, String destinataire, String contenu, Timestamp horodatage) {
        String sql = "INSERT INTO Messages(expediteur, destinataire, contenu, horodatage) VALUES(?, ?, ?, ?)";
        try (Connection connexion = this.connecter();
             PreparedStatement pstmt = connexion.prepareStatement(sql)) {
            pstmt.setString(1, expediteur);
            pstmt.setString(2, destinataire);
            pstmt.setString(3, contenu);
            pstmt.setTimestamp(4, horodatage);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la sauvegarde du message : " + e.getMessage());
            return false;
        }
    }

    // Récupérer l'historique des messages entre deux utilisateurs
    public List<Message> obtenirMessages(String utilisateur1, String utilisateur2) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT expediteur, destinataire, contenu, horodatage FROM Messages WHERE " +
                     "(expediteur = ? AND destinataire = ?) OR (expediteur = ? AND destinataire = ?) ORDER BY horodatage";
        try (Connection connexion = this.connecter();
             PreparedStatement pstmt = connexion.prepareStatement(sql)) {
            pstmt.setString(1, utilisateur1);
            pstmt.setString(2, utilisateur2);
            pstmt.setString(3, utilisateur2);
            pstmt.setString(4, utilisateur1);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                messages.add(new Message(rs.getString("expediteur"), rs.getString("destinataire"),
                        rs.getString("contenu"), rs.getTimestamp("horodatage")));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des messages : " + e.getMessage());
        }
        return messages;
    }

    // Récupérer les messages d'un groupe à partir de son identifiant
    public List<Message> obtenirMessagesGroupe(String idGroupe) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT expediteur, destinataire, contenu, horodatage FROM Messages WHERE destinataire = ? ORDER BY horodatage";
        try (Connection connexion = this.connecter();
             PreparedStatement pstmt = connexion.prepareStatement(sql)) {
            pstmt.setString(1, idGroupe);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                messages.add(new Message(rs.getString("expediteur"), rs.getString("destinataire"),
                        rs.getString("contenu"), rs.getTimestamp("horodatage")));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des messages du groupe : " + e.getMessage());
        }
        return messages;
    }

    // Gestion des fichiers

    // Sauvegarder un fichier (image, vidéo, GIF) dans la base de données
    public boolean sauvegarderFichier(String expediteur, String destinataire, byte[] donneesFichier, String typeFichier) {
        String sql = "INSERT INTO Fichiers(expediteur, destinataire, donneesFichier, typeFichier) VALUES(?, ?, ?, ?)";
        try (Connection connexion = this.connecter();
             PreparedStatement pstmt = connexion.prepareStatement(sql)) {
            pstmt.setString(1, expediteur);
            pstmt.setString(2, destinataire);
            pstmt.setBytes(3, donneesFichier);
            pstmt.setString(4, typeFichier);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la sauvegarde du fichier : " + e.getMessage());
            return false;
        }
    }

    // Récupérer les données d'un fichier à partir de son identifiant
    public byte[] obtenirFichier(int idFichier) {
        String sql = "SELECT donneesFichier FROM Fichiers WHERE idFichier = ?";
        try (Connection connexion = this.connecter();
             PreparedStatement pstmt = connexion.prepareStatement(sql)) {
            pstmt.setInt(1, idFichier);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getBytes("donneesFichier");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du fichier : " + e.getMessage());
        }
        return null;
    }
}

// Classe représentant un utilisateur
class Utilisateur {
    private String nomUtilisateur, motDePasseHache, statut;

    public Utilisateur(String nomUtilisateur, String motDePasseHache, String statut) {
        this.nomUtilisateur = nomUtilisateur;
        this.motDePasseHache = motDePasseHache;
        this.statut = statut;
    }

    public String obtenirNomUtilisateur() { return nomUtilisateur; }
    public String obtenirMotDePasseHache() { return motDePasseHache; }
    public String obtenirStatut() { return statut; }
}

// Classe représentant un message
class Message {
    private String expediteur, destinataire, contenu;
    private Timestamp horodatage;

    public Message(String expediteur, String destinataire, String contenu, Timestamp horodatage) {
        this.expediteur = expediteur;
        this.destinataire = destinataire;
        this.contenu = contenu;
        this.horodatage = horodatage;
    }

    public String obtenirExpediteur() { return expediteur; }
    public String obtenirDestinataire() { return destinataire; }
    public String obtenirContenu() { return contenu; }
    public Timestamp obtenirHorodatage() { return horodatage; }
}

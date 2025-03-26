package Client;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
	 
	public static Connection connecter() {
        String url = "jdbc:mysql://localhost:3306/messagerie?useSSL=false&serverTimezone=Europe/Paris";
        String utilisateur = "root";  // Utilisateur par défaut de WAMP
        String motdepasse = "";       // Mot de passe vide par défaut
        Connection connexion = null;

        try {
            // Charger explicitement le pilote MySQL (optionnel avec les versions récentes de JDBC)
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Passer l'URL, l'utilisateur et le mot de passe à getConnection
            connexion = DriverManager.getConnection(url, utilisateur, motdepasse);
            System.out.println("Connexion réussie à la base de données 'messagerie' !");
        } catch (SQLException e) {
            System.err.println("Erreur de connexion à la base de données : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Pilote MySQL non trouvé : " + e.getMessage());
        }
        return connexion;
    }
    }


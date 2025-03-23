package BD;

public class TestBaseDeDonnees {
    public static void main(String[] args) {
        BaseDeDonneesChat db = new BaseDeDonneesChat();
        // Ajouter un utilisateur
        boolean succes = db.creerUtilisateur("testUser", "hashTest123");
        System.out.println("Utilisateur créé : " + succes);
        // Vérifier l'utilisateur
        Utilisateur user = db.obtenirUtilisateur("testUser");
        if (user != null) {
            System.out.println("Utilisateur trouvé : " + user.obtenirNomUtilisateur() + ", Statut : " + user.obtenirStatut());
        }
    }
}

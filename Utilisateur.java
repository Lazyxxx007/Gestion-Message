package BD;

//Classe représentant un utilisateur
public class Utilisateur {
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
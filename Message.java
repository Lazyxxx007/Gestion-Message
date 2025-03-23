package BD;

import java.sql.Timestamp;

//Classe représentant un message
public class Message {
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
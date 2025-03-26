package Modeles;

import java.security.Timestamp;

public class Message {
    private String expediteur;
    private String destinataire;
    private String contenu;
    private java.sql.Timestamp horodatage;

    /**
     * Constructeur par défaut (nécessaire pour certaines utilisations comme Hibernate).
     * @param timestamp 
     * @param string3 
     * @param string2 
     * @param string 
     */

    /**
     * Constructeur principal pour créer un message.
     * @param expediteur Expéditeur du message.
     * @param destinataire Destinataire du message.
     * @param contenu Contenu du message.
     * @param horodatage Date et heure d'envoi du message.
     */
    public Message(String expediteur, String destinataire, String contenu, java.sql.Timestamp horodatage) {
        this.expediteur = expediteur;
        this.destinataire = destinataire;
        this.contenu = contenu;
        this.horodatage = horodatage;
    }

    // Getters
    public String obtenirExpediteur() { return expediteur; }
    public String obtenirDestinataire() { return destinataire; }
    public String obtenirContenu() { return contenu; }
    public java.sql.Timestamp obtenirHorodatage() { return horodatage; }

    // Setters
    public void definirExpediteur(String expediteur) { this.expediteur = expediteur; }
    public void definirDestinataire(String destinataire) { this.destinataire = destinataire; }
    public void definirContenu(String contenu) { this.contenu = contenu; }
    public void definirHorodatage(java.sql.Timestamp horodatage) { this.horodatage = horodatage; }

    /**
     * Affichage du message sous forme de texte.
     */
    @Override
    public String toString() {
        return expediteur + " [" + horodatage + "] : " + contenu;
    }
}
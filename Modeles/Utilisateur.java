package Modeles;

import java.sql.Timestamp;

public class Utilisateur {
    private String username;
    private String firstName;
    private String lastName;
    private String motDePasseHache;
    private String statut;
    private Timestamp derniereConnexion;

    public Utilisateur(String username, String firstName, String lastName, String motDePasseHache, String statut, Timestamp derniereConnexion) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.motDePasseHache = motDePasseHache;
        this.statut = statut;
        this.derniereConnexion = derniereConnexion;
    }

    public String obtenirUsername() {
        return username;
    }

    public String obtenirFirstName() {
        return firstName;
    }

    public String obtenirLastName() {
        return lastName;
    }

    public String obtenirMotDePasseHache() {
        return motDePasseHache;
    }

    public String obtenirStatut() {
        return statut;
    }

    public Timestamp obtenirDerniereConnexion() {
        return derniereConnexion;
    }
}
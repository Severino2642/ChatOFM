package entite.utilisateur;

/*import mg.Annotation.Colonne;
import mg.Annotation.Table;
import mg.Utils.Dao;

import java.sql.Connection;*/
import java.sql.Timestamp;

//@Table
public class Utilisateur /*extends Dao*/ {
//    @Colonne(isPK = true)
    String id;
//    @Colonne
    String pseudo;
//    @Colonne
    String identifiant;
//    @Colonne
    String mdp;
//    @Colonne
    Timestamp date;

    public Utilisateur() {

    }

    public Utilisateur(String pseudo, String identifiant, String mdp, Timestamp date) {
        this.pseudo = pseudo;
        this.identifiant = identifiant;
        this.mdp = mdp;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public String getIdentifiant() {
        return identifiant;
    }

    public void setIdentifiant(String identifiant) {
        this.identifiant = identifiant;
    }

    public String getMdp() {
        return mdp;
    }

    public void setMdp(String mdp) {
        this.mdp = mdp;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }
}

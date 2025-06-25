package com.mailapp.demo.Models;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Rappresenta una email
 * Serializzabile per essere utilizzata correttamente da Gson
 */

public class Email implements Serializable{

    public String sender;
    public List<String> receivers;
    public String subject;
    public String text;
    public Date data;
    public long id;

    /**
     * Costruttori della classe.
     * Ne sono presenti 2, uno che verrà utilizzato dal client e
     * uno che verrà utilizzato dal server che assegnerà anche l'id e la data
     *
     * @param sender     email del mittente
     * @param receivers  emails dei destinatari
     * @param subject    oggetto della mail
     * @param text       testo della mail
     * @param data       data di invio
     * @param id         id dell'email 
     */

    public Email(String sender, List<String> receivers, String subject, String text) {
        this.sender = sender;
        this.subject = subject;
        this.text = text;
        this.receivers = new ArrayList<>(receivers);
    }
    public Email(String sender, List<String> receivers, String subject, String text, Date data, long id) {
        this.sender = sender;
        this.subject = subject;
        this.text = text;
        this.receivers = new ArrayList<>(receivers);
        this.data = data;
        this.id = id;
    }
    public String getSender() {
        return sender;
    }

    public List<String> getReceivers() {
        return receivers;
    }

    public String getSubject() {
        return subject;
    }

    public String getText() {
        return text;
    }
    public long getId() {
        return id;
    }
    public Date getData() {
        return data;
    }

    /**
     * @return  stringa composta dagli indirizzi e-mail del mittente più destinatari
     *  usato per mostrare le informazioni nella ListView
     */
    
    @Override
    public String toString() {
        SimpleDateFormat  sdf = new SimpleDateFormat("dd-MM-yyyy");
        
        return String.join(" - ", this.sender, this.subject, sdf.format(this.data));
    }
    
}

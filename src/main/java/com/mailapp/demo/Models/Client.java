package com.mailapp.demo.Models;
import java.util.LinkedList;
import java.util.Random;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
/**
 * Classe Client, conterrà la lista di mail che sarà il model
 * ObservableList inboxContent è la lista che conterrà le email
 * ListProperty inbox la lista che ossarva inboxContent che sarà
 * bindata alla ListView lstEmail
 */

public class Client {
    private final ListProperty<Email> inbox;
    private final ObservableList<Email> inboxContent;
    private final StringProperty emailAddress;

    /**
     * Costruttore della classe.
     *
     * @param emailAddress   indirizzo email    
     *
     */

    public Client(){
        this.inboxContent = FXCollections.observableList(new LinkedList<>());
        this.inbox = new SimpleListProperty<>();
        this.inbox.set(inboxContent);
        this.emailAddress = new SimpleStringProperty(randomEmail());
    }
    /*
     * Funzione per scegliere casualmente un indirizzo email da assegnare al model
     */
    public String randomEmail(){
        String[] indirizzi = new String[] {"ceo@mail.com", "manager@mail.com", "dev1@mail.com", "dev2@mail.com"};
        
        Random r = new Random();
        return indirizzi[r.nextInt(indirizzi.length)];
    }
    /**
     * @return      lista di email  
     *
     */
    public ListProperty<Email> inboxProperty() {
        return inbox;
    }

    /**
     *
     * @return   indirizzo email della casella postale   
     *
     */
    public StringProperty emailAddressProperty() {
       return emailAddress;
    }

    /**
     *
     * @return   elimina l'email specificata   
     *
     */
    public void deleteEmail(long id) {
        for(int i=0;i<inbox.size();i++){
            if(inbox.get(i).getId() == id){
                inbox.remove(i);
                break;
            }
        }
    }

}


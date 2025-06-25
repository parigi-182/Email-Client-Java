package com.mailapp.demo.Models;
/*
 * Classe dell'indirizzo email, creata
 * per permettere all'oggetto Gson di leggere
 * gli indirizzi email dal file email.json
 */
public class indirizzoEmail {
    public String email;

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
}


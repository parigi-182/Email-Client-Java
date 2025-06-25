package com.mailapp.demo.Controllers;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

import com.mailapp.demo.Models.Email;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/*
 * Controller della finestra per scrivere
 * email
 */
public class ScriviMailController {
    /*
     * Oggetti dell'interfaccia grafica
     */
    @FXML
    private Label lblFrom;
    @FXML
    private Label lblTo;
    @FXML
    private Label emailWriter;
    @FXML
    private Label stato;
    @FXML
    private TextField txtTo;
    @FXML
    private TextField txtOggetto;
    @FXML
    private TextArea txtArea;
    @FXML
    private Button inviabtn;
    
    /*
     * Costruttore della classe
     */
    public ScriviMailController(){
        
    }
    /*
     * Questa implementazione di setData è usata per
     * la funzione di risposta a tutti
     */
    public void setData(String mittente, List<String> destinatari, String oggetto, String text){
        Platform.runLater(() -> {
            stato.setText(null);
            emailWriter.setText(mittente);
            txtTo.setText(String.join(", ", destinatari));
            txtOggetto.setText(oggetto);
            txtArea.setText(text);
        });
    }
    /*
     * Questa implementazione di setData è usata per
     * scrivere una nuova email
     */
    public void setData(String mittente){
        Platform.runLater(() -> {
            emailWriter.setText(mittente);
            stato.setText(null);
        });
    }

    /*
     * Questa implementazione di setData è usata per
     * la funzione di risposta
     */
    public void setData(String mittente, String destinatari, String oggetto, String text){
        
        Platform.runLater(() -> {
            stato.setText(null);
            emailWriter.setText(mittente);
            txtTo.setText(destinatari);
            txtOggetto.setText(oggetto);
            txtArea.setText(text);
        });
    }
    /*
     * Invia la richiesta di invio email al server.
     * Se il server non è disponibile, mostra un messaggio.
     * Se sono presenti errori nel campo destinatari, mostra
     * un messaggio.
     */
    /*
     * Non è stato implementato un controllo locale sull'esistenza dei destinatari
     * perchè teoricamente il client non conosce la lista di indirizzi email presenti nel server.
     * L'unico controllo sintattico verifica che l'indirizzo del destinatario finisca
     * con @mail.com
     */
    public void onInviaBtnClick(){
        String mittente =  emailWriter.getText();
        String destString = txtTo.getText();
        List<String> destinatari = Arrays.asList(destString.split(", "));
        String oggetto = txtOggetto.getText();
        String text = txtArea.getText();
        Email email = new Email(mittente, destinatari, oggetto, text);
        String esito;
        /* Controllo sintattico */
        for(int i=0;i<destinatari.size();i++){
            if(!destinatari.get(i).endsWith("@mail.com")){
                Platform.runLater(() ->{
                    stato.setText("Sintassi destinatari errata");
                });
                return;
            }

        }
        try {
            Socket conn = new Socket("localhost",5555);
            
            ObjectOutputStream outputStream = new ObjectOutputStream(conn.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(conn.getInputStream());

            outputStream.writeObject((String) "SEND_EMAIL");
            outputStream.writeObject((Email)email);
            esito = (String) inputStream.readObject();
            switch(esito){
                case "OK":
                    Platform.runLater(() ->{
                        stato.setText("Email inviata.");
                    });
                    Stage stage = (Stage) lblFrom.getScene().getWindow();
                    stage.close();
                    break;
                default:
                    Platform.runLater(() ->{
                        stato.setText(esito);
                    });
                    break;
            }
            conn.close();
        } catch (IOException e) {
            Platform.runLater(() ->{
                stato.setText("Impossibile collegarsi al server");
            });
        } catch (ClassNotFoundException e) {
            Platform.runLater(() -> {
                stato.setText("Problema nella comunicazione");
            });
        }
    }
}

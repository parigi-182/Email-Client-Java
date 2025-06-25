package com.mailapp.demo.Controllers;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mailapp.demo.Models.Client;
import com.mailapp.demo.Models.Email;
import com.mailapp.demo.Models.indirizzoEmail;


/**
 * Classe Controller del Client
 */

public class ClientController {
    /*
     * Elementi grafici
     */
    @FXML
    private Label lblFrom;
    @FXML
    private Label lblTo;
    @FXML
    private Label lblSubject;
    @FXML
    protected Label lblUsername;
    @FXML
    private TextArea txtEmailContent;
    @FXML
    protected Label stato;
    @FXML
    private Button inarrivobtn;
    @FXML
    private Button inviatebtn;
    @FXML
    private Button tuttebtn;
    @FXML
    private Button aggiornabtn;
    @FXML
    private Button scrivibtn;
    @FXML
    private ListView<Email> lstEmails;
    @FXML
    private Label idEmail;
    @FXML
    private Label dataEmail;
    @FXML
    private Button rispondibtn;
    @FXML
    private Button rispondituttibtn;
    @FXML
    private Button eliminabtn;
    @FXML
    private Button inoltrabtn;

    /*
     * Oggetti interni al controller. Executor controlla ogni 10 secondi se ci sono nuove email,
     * disabilita le funzioni che richiedono connessione se il server non risponde.
     * notifica è la finestra di dialogo che notifica nuove email, viene
     * mostrata solo se le nuove email vengono scaricate da executor e non dal pulsante aggiorna.
     * La finestra è la stessa, viene mostrata (notifica.show()) o nascosta (notifica.hide());
     * In questo modo non vengono mostrate più finestre se arrivano più email in più controlli.
     */
    Socket conn;
    ObjectOutputStream outputStream;
    ObjectInputStream inputStream;
    private Client model;
    private Email selectedEmail;
    private Email emptyEmail;
    ObservableList<Email> res;
    ScheduledExecutorService executor;
    String serverName = "127.0.0.1";
    Stage notifica = new Stage();

    /*
     * Inizializzatore del controller, disabilita i pulsanti, carica nel client le email
     * salvate e fa partire lo scheduled executor per il controllo della connessione.
     */
    
    public void initialize() throws IOException{

        disabilitaPulsantiConnessione();
        
        if (this.model != null)
        throw new IllegalStateException("Model can only be initialized once");
        model = new Client();

        notifica.initStyle(StageStyle.UNDECORATED);
        FXMLLoader fxmlLoader = new FXMLLoader(new File("src/main/resources/com/mailapp/demo/notificaemail.fxml").toURI().toURL());
        Scene scenanotifica = new Scene(fxmlLoader.load(),300,140);
        notifica.setScene(scenanotifica);
        DialogController controllerNotifica = fxmlLoader.getController();
        controllerNotifica.setEmail(model.emailAddressProperty().get());
        fxmlLoader.setController(controllerNotifica);
        
        
        /* Caricamento email salvate precedentemente */
        FileReader fr;
        Gson gson;
        indirizzoEmail email;
        ArrayList<Email> res;
        try {
            gson = new GsonBuilder().setPrettyPrinting().create();
            email = new indirizzoEmail();
            email.setEmail(model.emailAddressProperty().get());
            fr = new FileReader("src/main/resources/com/mailapp/demo/client/"+ email.getEmail().split("@")[0] + ".json");
            res = new ArrayList<>(gson.fromJson(fr, new TypeToken<ArrayList<Email>>(){}.getType()));
            model.inboxProperty().addAll(res);
            fr.close();
        }catch(IOException e){
            e.printStackTrace();
        }

        /* Bind del model alla view e partenza executor service */
        lblUsername.setText(model.emailAddressProperty().get());
        lstEmails.itemsProperty().bind(model.inboxProperty());
        selectedEmail = null;
        lstEmails.setOnMouseClicked(this::showSelectedEmail);
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(new ConnChecker(), 0, 10, TimeUnit.SECONDS);
        emptyEmail = null;

    }

    /*
     * Apre la finestra per scrivere una nuova mail impostando solo
     * il mittende
     */
    @FXML
    private void onScriviBtnClick() throws IOException{
        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(new File("src/main/resources/com/mailapp/demo/scrivimail.fxml").toURI().toURL());
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("Scrivi nuova email");
        stage.setScene(scene);
        ScriviMailController scriviMailController = fxmlLoader.getController();
        scriviMailController.setData(model.emailAddressProperty().get());
        fxmlLoader.setController(scriviMailController);
        stage.show();
        
    }
    /*
     * Apre la finestra per scrivere una nuova mail impostando
     * mittente, destinatario e settando l'oggetto e il testo dell'email
     * come Re:
     */
    @FXML
    private void onRispondiBtnClick() throws IOException{
        if(selectedEmail==null){
            return;
        }
        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(new File("src/main/resources/com/mailapp/demo/scrivimail.fxml").toURI().toURL());
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("Scrivi nuova email");
        stage.setScene(scene);
        ScriviMailController scriviMailController = fxmlLoader.getController();
        String reTo = selectedEmail.sender;
        String text = "Re: "+ reTo +" \n\t" + selectedEmail.text.replaceAll("\n", "\n\t") + "\n";
        scriviMailController.setData(model.emailAddressProperty().get(), selectedEmail.sender, "Re: " + selectedEmail.subject, text);
        fxmlLoader.setController(scriviMailController);
        stage.show();
    }
    /*
     * Apre la finestra per scrivere una nuova mail impostando
     * mittente, tutti i destinatari che hanno ricevuto l'email
     * settando l'oggetto e il testo dell'email come Re:
     */
    @FXML
    private void onRispondiATuttiBtnClick() throws IOException{
        if(selectedEmail==null){
            return;
        }
        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(new File("src/main/resources/com/mailapp/demo/scrivimail.fxml").toURI().toURL());
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("Scrivi nuova email");
        stage.setScene(scene);
        ScriviMailController scriviMailController = fxmlLoader.getController();
        String reTo = selectedEmail.sender;
        String text = "Re: "+ reTo +" \n\t" + selectedEmail.text.replaceAll("\n", "\n\t") + "\n";
        List<String> destinatari = new ArrayList<>(selectedEmail.getReceivers());
        destinatari.add(selectedEmail.getSender());
        destinatari.remove(model.emailAddressProperty().get());
        scriviMailController.setData(model.emailAddressProperty().get(), destinatari, "Re: " + selectedEmail.subject, text);
        fxmlLoader.setController(scriviMailController);
        stage.show();
    }
    
    /*
     * Apre la finestra per scrivere una nuova mail impostando
     * mittente, oggetto e testo
     */
    @FXML
    protected void onInoltraBtnClick() throws IOException{
        if(selectedEmail==null){
            return;
        }
        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(new File("src/main/resources/com/mailapp/demo/scrivimail.fxml").toURI().toURL());
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("Scrivi nuova email");
        stage.setScene(scene);
        ScriviMailController scriviMailController = fxmlLoader.getController();
        scriviMailController.setData(model.emailAddressProperty().get(), "", selectedEmail.subject, selectedEmail.text);
        fxmlLoader.setController(scriviMailController);
        stage.show();
    }

    /* Abilita i pulsanti associati a funzioni che richiedono
        la connessione al server */    
    public void abilitaPulsantiConnessione(){
        Platform.runLater(()->{
            scrivibtn.setDisable(false);
            rispondibtn.setDisable(false);
            rispondituttibtn.setDisable(false);
            eliminabtn.setDisable(false);
            inoltrabtn.setDisable(false);
        });
    }
    /* Disabilita i pulsanti associati a funzioni che richiedono
        la connessione al server */
    public void disabilitaPulsantiConnessione(){
        Platform.runLater(()->{
            scrivibtn.setDisable(true);
            rispondibtn.setDisable(true);
            rispondituttibtn.setDisable(true);
            eliminabtn.setDisable(true);
            inoltrabtn.setDisable(true);
        });
    }

    /* Aggiorna l'elenco delle email */
    @FXML
    protected void onAggiornaBtnClick(){
        updateEmails();
    }
    /*
     * Filtra l'elenco email per mostrare le
     * email in ingresso
     */
    @FXML
    protected void onInArrivoBtnClick(){
        lstEmails.itemsProperty().unbind();
        lstEmails.setItems(model.inboxProperty().filtered(email -> email.getReceivers().contains(model.emailAddressProperty().get())));
    }
    @FXML
    /*
     * Filtra l'elenco email per mostrare le
     * email in uscita
     */
    protected void onInviateBtnClick(){
        lstEmails.itemsProperty().unbind();
        lstEmails.setItems(model.inboxProperty().filtered(email -> email.getSender().contains(model.emailAddressProperty().get())));
    }
    @FXML
    /*
     * Mostra tutte le email
     */
    protected void onTutteLeEmailClick(){
        lstEmails.itemsProperty().unbind();
        lstEmails.itemsProperty().bind(model.inboxProperty());
    }
    /**
     * Manda una richiesta al server per eliminare l'email.
     * Se il server risponde, elimina l'email anche localmente.
     * Se il server non è disponibile, non rimuove l'email dall'elenco.
     * Se si eliminano email senza notificare correttamente il server,
     * queste compariranno di nuovo al prossimo login.
     */
    @FXML
    protected void onDeleteButtonClick() {
        if(selectedEmail==null){
            return;
        }
        FileReader fr;
        FileWriter fw;
        Gson gson;
        String toJson;
        try {
            conn = new Socket(serverName, 5555);
            outputStream = new ObjectOutputStream(conn.getOutputStream());
            outputStream.writeObject((String) "DELETE_EMAIL");
            outputStream.writeObject((String) model.emailAddressProperty().get());
            outputStream.writeObject((long) selectedEmail.getId());
            conn.close();
            /* Eliminazione email dalle email salvate localmente */
            gson = new GsonBuilder().setPrettyPrinting().create();
            fr = new FileReader("src/main/resources/com/mailapp/demo/client/" + model.emailAddressProperty().get().split("@")[0] + ".json");
            ArrayList<Email> res = new ArrayList<>(gson.fromJson(fr, new TypeToken<ArrayList<Email>>(){}.getType()));
            for(int j=0;j<res.size();j++){
                if(res.get(j).getId() == selectedEmail.getId()){
                    res.remove(j);
                    break;
                }
            }
            fw = new FileWriter("src/main/resources/com/mailapp/demo/client/" + model.emailAddressProperty().get().split("@")[0] + ".json");
            toJson = gson.toJson(res);
            fw.write(toJson);
            fr.close();
            fw.close();
            model.deleteEmail(selectedEmail.getId());
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateDetailView(emptyEmail);
    }

     /**
     * Mostra l'email selezionata nella vista
     */
    protected void showSelectedEmail(MouseEvent mouseEvent) {
        Email email = lstEmails.getSelectionModel().getSelectedItem();
        selectedEmail = email;
        updateDetailView(email);
    }

     /**
     * Aggiorna la vista con l'email selezionata
     */
    protected void updateDetailView(Email email) {
        if(email != null) {
            lblFrom.setText("M: " + email.getSender());
            lblTo.setText("D: " + String.join(", ", email.getReceivers()));
            lblSubject.setText("O: " + email.getSubject());
            idEmail.setText("ID: " + Long.toString(email.getId()));
            dataEmail.setText(email.getData().toString());
            txtEmailContent.setText(email.getText());
        }else{
            lblFrom.setText("");
            lblTo.setText("");
            lblSubject.setText("");
            idEmail.setText("");
            dataEmail.setText("");
            txtEmailContent.setText("");
        }
    }

    /*
     * Effettua una richiesta di aggiornamento inviando al server
     * il numero di email presenti in locale. Il server risponde con un intero
     * che rappresenta quante email nuove sono presenti. Se vengono ricevute
     * nuove email, vengono visualizzate e memorizzate localmente
     */
    public void updateEmails(){
        Platform.runLater(()->{
            stato.setText("Aggiornamento...");
        });
        try {
            int esito;
            conn = new Socket(serverName, 5555);
            outputStream = new ObjectOutputStream(conn.getOutputStream());
            inputStream = new ObjectInputStream(conn.getInputStream());
            /* Richiesta per la presenza di nuove email */
            outputStream.writeObject("UPDATE_EMAILS");
            outputStream.writeObject(model.emailAddressProperty().get());
            outputStream.writeObject(model.inboxProperty().getSize());
            esito = (int)inputStream.readObject();
            if(esito == 0){
                /* Server disponibile, non ci sono nuove email */
                Platform.runLater(() -> {
                    abilitaPulsantiConnessione();
                    stato.setText("Aggiornato");
                });

            }else{
                /* Server disponibile, aggiungi le nuove email alla lista e
                 * mostra la finestra di dialogo se il model e il mittente sono diversi.
                 * Quindi abilita i pulsanti che richiedono connessione
                 */
                res = null;
                res = FXCollections.observableList((ArrayList<Email>) inputStream.readObject());
                
                Platform.runLater(() -> {
                    for(int j=res.size()-1;j>-1;j--){
                        model.inboxProperty().add(0, res.get(j));
                        if(!res.get(j).getSender().equals(model.emailAddressProperty().get())){
                            notifica.show();
                        }
                    }
                    abilitaPulsantiConnessione();
                });
                /* Salvataggio email in locale */
                FileWriter fw;
                Gson gson;
                File f;
                String toJson;
                try{
                    f = new File("src/main/resources/com/mailapp/demo/client/" + model.emailAddressProperty().get().split("@")[0] + ".json");
                    fw = new FileWriter(f);
                    gson = new GsonBuilder().setPrettyPrinting().create();
                    toJson = gson.toJson(model.inboxProperty());
                    fw.write(toJson);
                    fw.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
            conn.close();
            
        }catch (UnknownHostException e){
            /* Server non disponibile, disabilita i pulsanti che richiedono connessione */
            Platform.runLater(() -> {
                stato.setText("Server sconosciuto");
                disabilitaPulsantiConnessione();
            });
        }catch (IOException e) {
            /* Server non disponibile, disabilita i pulsanti che richiedono connessione */
            Platform.runLater(() -> {
                stato.setText("Impossibile connettersi");
                disabilitaPulsantiConnessione();
            });
        } catch (ClassNotFoundException e) {
            /* Server non disponibile, disabilita i pulsanti che richiedono connessione */
            Platform.runLater(() -> {
                stato.setText("Problema nella comunicazione");
                disabilitaPulsantiConnessione();
            });
        }
    }


    /*
     * Classe Runnable da assegnare al service executor
     */
    private class ConnChecker implements Runnable{
        
        @Override
        public void run() {
            updateEmails();    
        
        }
    }

}

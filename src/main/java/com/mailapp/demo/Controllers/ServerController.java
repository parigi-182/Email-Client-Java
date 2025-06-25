package com.mailapp.demo.Controllers;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mailapp.demo.Models.Email;
import com.mailapp.demo.Models.indirizzoEmail;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

/*
 * Classe per il controller del server
 */
public class ServerController implements Initializable {
    /*
    * Oggetti dell'interfaccia grafica
    */
    @FXML
    private TextArea testolog;
    
    @FXML
    private Button startServer;
    @FXML
    private Button stopServer;
    /*
    * Oggetti del controller
    */
    static ArrayList<indirizzoEmail> indirizziEmail;
    static List<ReentrantReadWriteLock> locks;
    static int countClient = 0;
    boolean running = true;
    static long id;
    /*
     * demoneAccettaConnessione è il thread demone
     * in ascolto sulla porta. Fa partire un service executor
     * ad ogni connessione in ingresso
     */
    private Thread demoneAccettaConnessione;


    /*
     * Restituisce un id da assegnare all'email
     */
    synchronized public static long getId(){
        try {
            FileWriter fw = new FileWriter("src/main/resources/com/mailapp/demo/id.txt");
            id++;
            fw.write(Long.toUnsignedString(id));
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (id-1);
    }


    /*
     * Metodo del pulsante Start server.
     * Crea un processo demone per accettare le connessioni in ingresso
     */
    public void onStartServerClick(){
        demoneAccettaConnessione = new Thread(new startServer());
        demoneAccettaConnessione.setDaemon(true);
        demoneAccettaConnessione.start();
        startServer.setDisable(true);
        stopServer.setDisable(false);
        Platform.runLater(()->{
            testolog.appendText("Server avviato\n");
        });
    }

    /*
     * Metodo del pulsante Stop server.
     * Ferma il processo demone
     */
    public void onStopServerClick(){
        Platform.runLater(() ->{
            testolog.appendText("Inzio spegnimento server\n");
        });
        demoneAccettaConnessione.interrupt();
        while(demoneAccettaConnessione.isAlive());
        
        startServer.setDisable(false);
        stopServer.setDisable(true);
    }   

    /*
     * Inizializzatore del controller, carica
     * l'ultimo id assegnato ad un'email
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        stopServer.setDisable(true);
        try {
            File f = new File("src/main/resources/com/mailapp/demo/id.txt");
            Scanner in = new Scanner(f);
            id = Long.parseLong(in.next());
            in.close();
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    /*
     * Classe Runnable da assegnare al processo demone
     * che accetta le connessioni
     */
    private class startServer implements Runnable{
        /*
         * Oggetti della classe
         */
        ServerSocket serverSocket;
        ExecutorService executorPool;

        /*
         * Metodo run, carica gli indirizzi email disponibili,
         * crea una Thread Pool per gli executors che accetteranno
         * le richieste e ne avvia uno per ogni connessione in
         * ingresso
         */
        @Override
        public void run() {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileReader fr = null;
            
            try {
                fr = new FileReader("src/main/resources/com/mailapp/demo/email.json");
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            indirizziEmail = new ArrayList<>(gson.fromJson(fr, new TypeToken<ArrayList<indirizzoEmail>>(){}.getType()));
            locks = new ArrayList<>();
            for(int i=0;i<indirizziEmail.size();i++){
                locks.add(new ReentrantReadWriteLock());
            }
            
            executorPool = Executors.newFixedThreadPool(10);
            try {
                
                serverSocket = new ServerSocket(5555);
                
                /*Ciclo in attesa di connessioni */
                /*
                 * Allo spegnimento del server (dal bottone) si accetta l'ultima richiesta
                 * prima che la condizione del while non sia soddisfatta
                 */
                while (!Thread.interrupted()){
                    Socket clientSocket = serverSocket.accept();
                    executorPool.execute(new ClientHandler(clientSocket));
                    
                }
                /*Se si esce dal ciclo, il server deve essere fermato
                 * shutdown e non shutdownNow per permettere agli executor già
                 * running di completare le richieste prese in carico.
                */
                executorPool.shutdown();
                serverSocket.close();
                if(Thread.interrupted()){
                    throw new InterruptedException();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch(InterruptedException e){
                Platform.runLater(()->{
                    testolog.appendText("Server fermato.");
                });
            }
        }

    }

    /*
     * Classe runnable assegnata ai service executor
     */
    private class ClientHandler implements Runnable{
        /*
         * Oggetti della classe
         */
        ObjectOutputStream outputStream;
        ObjectInputStream inputStream;
        /*
         * Costruttore della classe, inzializza gli
         * ObjectOutputStream e ObjectInputStream
         */
        public ClientHandler(Socket clientSocket){
            try {
                outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                inputStream = new ObjectInputStream(clientSocket.getInputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        /*
         * Metodo run, accetta il comando ingresso e fa partire
         * la funzione adeguata
         */
        @Override
        public void run() {
            String command;
                try {
                    command = (String) inputStream.readObject();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                switch (command){
                    /* Il client vuole tutte le sue email */
                    case "GET_EMAILS":
                            get_email();
                            break;
                    /* Il client vuole sapere se ci sono nuove email */
                    case "UPDATE_EMAILS":
                            update_emails();
                            break;
                    /* Il client vuole eliminare un'email */
                    case "DELETE_EMAIL":
                            delete_email();
                            break;
                    /* Il client vuole inviare un'email */
                    case "SEND_EMAIL":
                            send_email();
                            break;
                    default:
                    /*Il client manda comandi diversi da quelli sopracitati */
                            break;
                }

        }
        /*
         * Accetta in ingresso un email.
         * Se i destinatari sono indirizzi validi, inserisce
         * l'email nei file json dei destinatari e invia un segnale
         * di conferma. Altrimenti invia un messaggio
         * di errore
         */
        public void send_email(){
            try {
                Date now = new Date();
                Email email = (Email) inputStream.readObject();
                email.data = now;
                email.id = getId();
                File f;
                boolean error = false;
                String path, toJson;
                int i, j;
                FileReader fr;
                FileWriter fw;
                ArrayList<Email> res;
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                
                /*
                 * Controllo sugli indirizzi dei destinatari
                 * Se non esiste un file email.json nella cartella nomedestinatario, ferma la richiesta
                 * e invia un messaggio di errore
                 * Controlla anche che l'email sia composta da nomedestinatario + @mail.com
                 */
                for(i=0;i<email.getReceivers().size() && !error;i++){
                    path = "src/main/resources/com/mailapp/demo/email/"+email.getReceivers().get(i).split("@")[0]+"/email.json";
                    f = new File(path);
                    if(!f.exists() || !(email.getReceivers().get(i).equals(email.getReceivers().get(i).split("@")[0] + "@mail.com"))){
                        error = true;
                    }
                }
                if(error){
                    /* Notifica la non riuscita dell'operazione */
                    outputStream.writeObject((String)"Destinatario/i inesistente/i");
                }else{
                    outputStream.writeObject((String)"OK");
                    /*
                     * Inserimento delle email nei file json
                     */
                    /*
                     * Questo for cerca l'indice dell'email del destinatario nella
                     * lista di indirizzi email. Quando lo trova esce dal for.
                     */
                    for(i=0;i<email.getReceivers().size();i++){
                        for(j=0;j<indirizziEmail.size();j++){
                            if(email.getReceivers().get(i).equals(indirizziEmail.get(j).getEmail())){
                                break;
                            }
                        }
                        /*
                         * Lock sul file del destinatario, caricamento delle email in un arraylist,
                         * aggiunta in cima della nuova email, scaricamento delle email nel file
                         */
                        locks.get(j).writeLock().lock();
                        fr = new FileReader("src/main/resources/com/mailapp/demo/email/"+email.getReceivers().get(i).split("@")[0]+"/email.json");
                        res = new ArrayList<>(gson.fromJson(fr, new TypeToken<ArrayList<Email>>(){}.getType()));
                        fr.close();
                        res.add(0, email);
                        fw = new FileWriter("src/main/resources/com/mailapp/demo/email/"+email.getReceivers().get(i).split("@")[0]+"/email.json");
                        toJson = gson.toJson(res);
                        fw.write(toJson);
                        fw.close();
                        locks.get(j).writeLock().unlock();
                    }
                    /*
                     * Questo for cerca l'indice dell'email del mittente nella
                     * lista di indirizzi email. Quando lo trova esce dal for
                     */
                    for(j=0;j<indirizziEmail.size();j++){
                        if(email.getSender().equals(indirizziEmail.get(j).getEmail())){
                            break;
                        }
                    }
                    /*
                     * Lock sul file del mittente, caricamento delle email in un arraylist
                     * aggiunta in cima della nuova email, scaricamento delle email nel file
                     */
                    locks.get(j).writeLock().lock();
                    fr = new FileReader("src/main/resources/com/mailapp/demo/email/"+email.getSender().split("@")[0]+"/email.json");
                    res = new ArrayList<>(gson.fromJson(fr, new TypeToken<ArrayList<Email>>(){}.getType()));
                    fr.close();
                    res.add(0, email);
                    fw = new FileWriter("src/main/resources/com/mailapp/demo/email/"+email.getSender().split("@")[0]+"/email.json");
                    toJson = gson.toJson(res);
                    fw.write(toJson);
                    fw.close();
                    locks.get(j).writeLock().unlock();
                    /* Conferma la riuscita dell'operazione e aggiungi un log */
                    Platform.runLater(()->{
                        testolog.appendText(email.getSender() + " ha inviato un'email a " + email.getReceivers() + "\n");
                    });
                }

            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        }
        /*
         * Handler del comando per controllare la disponibilità
         * di nuove email. Accetta in ingresso un indirizzo email
         * e il numero di email presenti nel client. Se il client
         * ha meno email di quelle prensenti sul server, invia 
         * le ultime che non state inviate
         */
        public void update_emails(){
            indirizzoEmail email = new indirizzoEmail();
            FileReader fr;
            Gson gson;
            int n_email, i;
            try {
                
                email.setEmail((String) inputStream.readObject());
                n_email = (int) inputStream.readObject();
                
                for(i=0;i<indirizziEmail.size();i++){
                    if(indirizziEmail.get(i).email.equals(email.getEmail())){
                        break;
                    }
                }
                /* Lock in lettura sul file delle email */
                locks.get(i).readLock().lock();
                gson = new GsonBuilder().setPrettyPrinting().create();
                
                fr = new FileReader("src/main/resources/com/mailapp/demo/email/"+email.getEmail().split("@")[0]+"/email.json");
                
                ArrayList<Email> res = new ArrayList<>(gson.fromJson(fr, new TypeToken<ArrayList<Email>>(){}.getType()));
                /* Stampa 0 se non esistono nuove email e la connessione può terminare,
                 * altrimenti invia le nuove email
                 */
                if(n_email == res.size()){
                    outputStream.writeObject((int)0);
                }else{
                    outputStream.writeObject((int)1);
                    res = new ArrayList<>(res.subList(0, (res.size()-n_email)));
                    outputStream.writeObject(res);
                }
                
                fr.close();
                locks.get(i).readLock().unlock();
                Platform.runLater(()->{
                    testolog.appendText(email.getEmail() + " controlla le sue email\n");
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }finally{
            }
        }
        /*
         * Handeler del comando per scaricare tutte le email
         * Usato subito dopo Init Client, get_email accetta
         * un indirizzo email in ingresso e restituisce
         * tutte le email che lo riguardano
         */
        public void get_email(){
            indirizzoEmail email = new indirizzoEmail();
            FileReader fr;
            Gson gson;
            int i;
            try {
                gson = new GsonBuilder().setPrettyPrinting().create();
                email.setEmail((String) inputStream.readObject());
                for(i=0;i<indirizziEmail.size();i++){
                    if(indirizziEmail.get(i).email.equals(email.getEmail())){
                        break;
                    }
                }
                /* Lock sul file del client */
                locks.get(i).readLock().lock();
                fr = new FileReader("src/main/resources/com/mailapp/demo/email/"+email.getEmail().split("@")[0]+"/email.json");
                /* Invio di tutte le email */
                ArrayList<Email> res = new ArrayList<>(gson.fromJson(fr, new TypeToken<ArrayList<Email>>(){}.getType()));
                outputStream.writeObject(res);
                fr.close();
                locks.get(i).readLock().unlock();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }finally{
            }
        }
        /*
         * Riceve un indirizzo email in ingresso e un'email.
         * Quindi elimina l'email con dato id dal file json
         * dell'indirizzo email
         */
        public void delete_email(){
            indirizzoEmail email = new indirizzoEmail();
            Gson gson;
            long id;
            FileWriter fw;
            FileReader fr;
            String toJson;
            int i;
            try{
                gson = new GsonBuilder().setPrettyPrinting().create();
                email.setEmail((String) inputStream.readObject());
                id = (long) inputStream.readObject();
                for(i=0;i<indirizziEmail.size();i++){
                    if(indirizziEmail.get(i).getEmail().equals(email.getEmail())){
                        break;
                    }
                }
                /* Lock sul file del client */
                locks.get(i).writeLock().lock();
                fr = new FileReader("src/main/resources/com/mailapp/demo/email/"+email.getEmail().split("@")[0]+"/email.json");
                ArrayList<Email> res = new ArrayList<>(gson.fromJson(fr, new TypeToken<ArrayList<Email>>(){}.getType()));
                fr.close();
                /* Ricerca dell'email per id ed eliminazione */
                for(int j=0;j<res.size();j++){
                    if(res.get(j).getId() == id){
                        res.remove(j);
                        break;
                    }
                }
                fw = new FileWriter("src/main/resources/com/mailapp/demo/email/"+email.getEmail().split("@")[0]+"/email.json");
                toJson = gson.toJson(res);
                fw.write(toJson);
                fw.close();
                locks.get(i).writeLock().unlock();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }finally{
            }
        }
    }

    
}
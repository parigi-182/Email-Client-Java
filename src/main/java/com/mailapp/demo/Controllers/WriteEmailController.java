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
 * New email window controller
 */
public class WriteEmailController {
    /*
     * GUI objects
     */
    @FXML
    private Label lblFrom;
    @FXML
    private Label lblTo;
    @FXML
    private Label emailWriter;
    @FXML
    private Label status;
    @FXML
    private TextField txtTo;
    @FXML
    private TextField txtSubjext;
    @FXML
    private TextArea txtArea;
    @FXML
    private Button inviabtn;
    
    /*
     * Class constructor
     */
    public WriteEmailController(){
        
    }
    /*
     * Used for Reply all function
     */
    public void setData(String sender, List<String> recipients, String subject, String text){
        Platform.runLater(() -> {
            status.setText(null);
            emailWriter.setText(sender);
            txtTo.setText(String.join(", ", recipients));
            txtSubjext.setText(subject);
            txtArea.setText(text);
        });
    }
    /*
     * Used for new email function
     */
    public void setData(String mittente){
        Platform.runLater(() -> {
            emailWriter.setText(mittente);
            status.setText(null);
        });
    }

    /*
     * Used for Reply function
     */
    public void setData(String mittente, String recipients, String subject, String text){
        
        Platform.runLater(() -> {
            status.setText(null);
            emailWriter.setText(mittente);
            txtTo.setText(recipients);
            txtSubjext.setText(subject);
            txtArea.setText(text);
        });
    }
    /*
     * Sends the email request to the server.
     * If the server is unavailable, displays a message.
     * If there are errors in the recipient field, displays
     * a message.
     */
    /*
     * No local check on the existence of recipients has been implemented
     * because theoretically the client does not know the list of email addresses on the server.
     * The only syntactic check verifies that the recipient address ends
     * with @mail.com
     */
    public void onSendBtnClick(){
        String sender =  emailWriter.getText();
        String recString = txtTo.getText();
        List<String> receipients = Arrays.asList(recString.split(", "));
        String subject = txtSubjext.getText();
        String text = txtArea.getText();
        Email email = new Email(sender, receipients, subject, text);
        String esito;
        /* Controllo sintattico */
        for(int i=0;i<receipients.size();i++){
            if(!receipients.get(i).endsWith("@mail.com")){
                Platform.runLater(() ->{
                    status.setText("Recipients sintax error");
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
                        status.setText("Email sent");
                    });
                    Stage stage = (Stage) lblFrom.getScene().getWindow();
                    stage.close();
                    break;
                default:
                    Platform.runLater(() ->{
                        status.setText(esito);
                    });
                    break;
            }
            conn.close();
        } catch (IOException e) {
            Platform.runLater(() ->{
                status.setText("Connection error");
            });
        } catch (ClassNotFoundException e) {
            Platform.runLater(() -> {
                status.setText("Communication");
            });
        }
    }
}

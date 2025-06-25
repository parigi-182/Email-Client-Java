package com.mailapp.demo.Controllers;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
/*
 * Controller della finestra di dialogo notifica che
 * avvisa della presenza di nuove email.
 */
public class DialogController {
    /* Pulsante ok della notifica */
    @FXML
    private Button ok;
    @FXML
    private Label ind_email;

    /* Nascondi la finestra di notifica */
    @FXML
    private void onOkClick(){
        Scene scene = ok.getScene();
        Stage stage = (Stage) scene.getWindow();
        stage.hide();
    }
    
    public void setEmail(String email){
        Platform.runLater(()->{
            ind_email.setText(email);
        });
    }

}

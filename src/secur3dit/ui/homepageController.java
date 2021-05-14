package secur3dit.ui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 * FXML Controller class
 *
 * @author naman
 */
public class homepageController implements Initializable {

    /**
     * Initializes the controller class.
     */
    @FXML
    private Pane mainArea;
    @FXML
    private WebView myweb = new WebView();
    @FXML
    private WebEngine engine;
    @FXML
    private Button closeBtn;
    @FXML
    public void navigateToFilter(ActionEvent e) throws IOException{
        mainArea.getChildren().removeAll();
        Parent fxml = FXMLLoader.load(getClass().getResource("uifxml.fxml"));
        mainArea.getChildren().addAll(fxml);
    }
    @FXML
    public void navigateToEncryption(ActionEvent e) throws IOException{
        mainArea.getChildren().removeAll();
        Parent fxml = FXMLLoader.load(getClass().getResource("encryption.fxml"));
        mainArea.getChildren().addAll(fxml);
    }
    @FXML
    public void openGoogleImages(ActionEvent e){
        closeBtn.setVisible(true);
        myweb.setVisible(true);
        engine = myweb.getEngine();
        engine.load("https://www.google.com/imghp?hl=EN");
       
    }
    @FXML
    public void closeGI(ActionEvent e){
        myweb.setVisible(false);
        closeBtn.setVisible(false);
        
    }
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        // TODO
        closeBtn.setVisible(false);
        myweb.setVisible(false);
    }    
    
}
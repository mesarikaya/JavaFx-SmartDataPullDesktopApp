package com.mesarikaya.getItApp;

import com.mesarikaya.getItApp.Controllers.LoginController;
import com.mesarikaya.getItApp.Controllers.UserPageController;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

    private static LoginController loginController;
    private static UserPageController userPageController;
    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void start(Stage primaryStage) throws Exception{

        // Set the login page scene
        FXMLLoader firstPaneLoader = new FXMLLoader(getClass().getResource("login.fxml"));
        Parent firstPane = firstPaneLoader.load();
        Scene firstScene = new Scene(firstPane);

        // Set the user page scene
        FXMLLoader secondPageLoader = new FXMLLoader(getClass().getResource("userPage.fxml"));

        // injecting second scene into the controller of the first scene
        LoginController firstPaneController = (LoginController) firstPaneLoader.getController();
        firstPaneController.setSecondPageLoader(secondPageLoader);

        firstPane.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            }
        });

        firstPane.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                primaryStage.setX(event.getScreenX() - xOffset);
                primaryStage.setY(event.getScreenY() - yOffset);
            }
        });

        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setTitle("GeTIt");

        primaryStage.setResizable(false);
        primaryStage.setScene(firstScene);

        firstScene.setFill(Color.valueOf("#2F3739"));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }


}

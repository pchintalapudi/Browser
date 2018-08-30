/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.value.WritableValue;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import pc.browser.resources.Resources;

/**
 *
 * @author prem
 */
public class Browser extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setOpacity(0);
        primaryStage.setScene(new Scene(Resources.directLoad("Main.fxml"), 1600, 900));
        primaryStage.getScene().setFill(Color.TRANSPARENT);
        Timeline showTimeline = new Timeline();
        WritableValue<Double> stageOpacity = new WritableValue<Double>() {
            @Override
            public Double getValue() {
                return primaryStage.getOpacity();
            }

            @Override
            public void setValue(Double value) {
                primaryStage.setOpacity(value);
            }
        };
        showTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(300), new KeyValue(stageOpacity, 1d)));
        primaryStage.setOnShown(we -> showTimeline.play());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

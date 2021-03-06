package org.jwave.view.screens;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.jwave.controller.EditorController;
import org.jwave.model.editor.GroupedSampleInfo;
import org.jwave.model.player.Song;
import org.jwave.view.FXEnvironment;
import org.jwave.view.UI;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.ImageView;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

/**
 * Controller for the Player screen.
 * 
 */
public class EditorScreenController implements UI {

    private final FXMLScreens FXMLSCREEN = FXMLScreens.EDITOR;
    private final FXEnvironment environment;
    private final EditorController controller;
    private Stage primaryStage;
    private boolean lockedPositionSlider;
    private boolean loaded;

    @FXML
    private MenuItem btnEditor;
    @FXML
    private Label labelLeft, labelRight, labelSong, labelFrom, labelTo;
    @FXML
    private Button btnPlay, btnStop, btnNewPlaylist;
    @FXML
    private volatile Slider sliderPosition, sliderVolume, sliderCursor1, sliderCursor2;
    @FXML
    private LineChart<Integer, Float> lineChartLeft;
    @FXML
    private LineChart<Integer, Float> lineChartRight;
    @FXML
    private VBox vboxChartContainer;

    /**
     * @param environment
     * @param controller
     */
    public EditorScreenController(FXEnvironment environment, EditorController controller) {
        this.controller = controller;
        this.environment = environment;
        this.environment.loadScreen(FXMLSCREEN, this);
        this.lockedPositionSlider = false;
        this.controller.addGraph(this);
        this.loaded = false;
        
        btnPlay.setGraphic(new ImageView("/icons/play.png"));
        btnStop.setGraphic(new ImageView("/icons/stop.png"));
        btnPlay.setText("");
        btnStop.setText("");

        sliderVolume.valueProperty().addListener((ov, old_val, new_val) -> {
            controller.setVolume(new_val.intValue());
            System.out.println(new_val);
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jwave.view.UI#show()
     */
    @Override
    public void show() {
        this.primaryStage = this.environment.getMainStage();
        this.primaryStage.setOnCloseRequest(e -> System.exit(0));
        this.environment.displayScreen(FXMLSCREEN);
    }

    /**
     * 
     */
    @FXML
    private void play() {
        controller.play();
    }

    /**
     * 
     */
    @FXML
    private void stop() {
        controller.stop();
    }

    /**
     * 
     */
    @FXML
    private void copy() {
        controller.copy((int) sliderCursor1.getValue(), (int) sliderCursor2.getValue());
    }

    /**
     * 
     */
    @FXML
    private void cut() {
        controller.cut((int) sliderCursor1.getValue(), (int) sliderCursor2.getValue());
    }

    /**
     * 
     */
    @FXML
    private void paste() {
        controller.paste((int) sliderCursor1.getValue());
    }

    /**
     * @param samplesList
     *            Plots the graph of the waveform
     */
    public void paintWaveForm(List<GroupedSampleInfo> samplesList) {

        lineChartLeft.getData().clear();
        lineChartRight.getData().clear();

        System.out.println("paint" + Thread.currentThread());

        XYChart.Series<Integer, Float> leftSeries = new XYChart.Series<>();

        leftSeries.setName("Left Channel");

        for (int i = 0; i < samplesList.size(); i++) {
            leftSeries.getData().add(new XYChart.Data<Integer, Float>(i, samplesList.get(i).getLeftChannelMax()));
            leftSeries.getData().add(new XYChart.Data<Integer, Float>(i, samplesList.get(i).getLeftChannelMin()));
        }

        lineChartLeft.setCreateSymbols(false);
        lineChartLeft.getData().add(leftSeries);

        XYChart.Series<Integer, Float> rightSeries = new XYChart.Series<>();

        rightSeries.setName("Right Channel");

        for (int i = 0; i < samplesList.size(); i++) {
            rightSeries.getData().add(new XYChart.Data<Integer, Float>(i, samplesList.get(i).getRightChannelMax()));
            rightSeries.getData().add(new XYChart.Data<Integer, Float>(i, samplesList.get(i).getRightChannelMin()));
        }

        lineChartRight.setCreateSymbols(false);
        lineChartRight.getData().add(rightSeries);

    }

    /**
     * 
     */
    @FXML
    private void openFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new ExtensionFilter("Audio file", "*.mp3", "*.wav"));
        File openedFile = fileChooser.showOpenDialog(primaryStage);
        this.loaded = true;
        try {
            controller.loadSong(openedFile);
            paintWaveForm(new ArrayList<GroupedSampleInfo>(this.controller.getWaveform()));

        } catch (Exception e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Errore");
            alert.setHeaderText("Impossibile aprire il file " + openedFile.getName());
            alert.setContentText("Il file potrebbe essere danneggiato o in un formato non valido.");
            alert.showAndWait();
        }

    }

    /**
     * Prevents the slider to be moved (By the controller routine for instance).
     */
    @FXML
    private void lockSlider() {
        lockedPositionSlider = true;
    }

    /**
     * Communicates the new slider position to the controller then unlocks the
     * slider
     */
    @FXML
    private void changePosition() {
        controller.moveToMoment(sliderPosition.getValue());
        lockedPositionSlider = false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jwave.view.UI#updatePosition(java.lang.Integer,
     * java.lang.Integer)
     */
    @Override
    public void updatePosition(Integer ms, Integer lenght) {
        if (!sliderPosition.isValueChanging() && lockedPositionSlider == false)
            sliderPosition.setValue((ms * 10000) / lenght);

        String elapsed = String.format("%d:%02d", TimeUnit.MILLISECONDS.toMinutes(ms),
                TimeUnit.MILLISECONDS.toSeconds(ms) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(ms)));
        String remaining = ("-" + String.format("%d:%02d", TimeUnit.MILLISECONDS.toMinutes(lenght - ms),
                TimeUnit.MILLISECONDS.toSeconds(lenght - ms)
                        - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(lenght - ms))));

        Platform.runLater(() -> {
            labelLeft.setText(elapsed);
            labelRight.setText(remaining);
        });
    }

    /**
     * Switch screen to player.
     */
    @FXML
    private void goToPlayer() {
        this.environment.displayScreen(FXMLScreens.PLAYER);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.jwave.view.UI#updateReproductionInfo(org.jwave.model.player.Song)
     */
    @Override
    public void updateReproductionInfo(Song song) {
        Platform.runLater(() -> {
            labelSong.setText(song.getName());
        });
    }

    /**
     * 
     */
    @FXML
    private void save() {
        if(loaded){
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Song");
            FileChooser.ExtensionFilter extFilter = 
            new FileChooser.ExtensionFilter("Wave (*.wav)", "*.wav");
            fileChooser.getExtensionFilters().add(extFilter);
            File file = fileChooser.showSaveDialog(primaryStage);
            if (file != null) {
                controller.saveFile(file.getAbsolutePath());
            }
        }
        
    }

    /**
     * 
     */
    @FXML
    private void showAboutInfo() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("About JWave");
        alert.setHeaderText("");
        alert.setContentText("Editing   Aleksejs Vlasovs\nView      Alessandro Martignano\nPlayer    Dario Cantarelli");
        alert.showAndWait();
    }

    /**
     * Resizes the graph
     * 
     * @param ms
     *            length of the song to be displayed
     */
    public void updateGraphLenght(int ms) {
        vboxChartContainer.setPrefWidth(ms);
        sliderCursor1.setMax(ms);
        sliderCursor2.setMax(ms);
        lineChartLeft.setPrefWidth(ms);
        lineChartRight.setPrefWidth(ms);
    }

}

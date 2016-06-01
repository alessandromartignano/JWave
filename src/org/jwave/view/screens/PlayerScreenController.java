package org.jwave.view.screens;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.jwave.controller.EditorControllerImpl;
import org.jwave.controller.PlayerController;
import org.jwave.model.player.MetaData;
import org.jwave.model.player.Song;
import org.jwave.model.playlist.PlayMode;
import org.jwave.model.playlist.Playlist;
import org.jwave.view.FXEnvironment;
import org.jwave.view.UI;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableRow;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * Controller for the Player screen.
 *
 */
public class PlayerScreenController implements UI {

    private final FXMLScreens FXMLSCREEN = FXMLScreens.PLAYER;
    private final FXEnvironment environment;
    private final PlayerController controller;
    private Stage primaryStage;
    private boolean lockedPositionSlider;

    @FXML
    private MenuItem btnEditor;
    @FXML
    private ChoiceBox<String> choiceMode;
    @FXML
    private Label labelLeft, labelRight, labelSong;
    @FXML
    private Button btnPlay, btnNewPlaylist;
    @FXML
    private volatile Slider positionSlider, volumeSlider;
    @FXML
    private ListView<Playlist> listView;
    @FXML
    private TableView<Song> tableView;
    @FXML
    private TableColumn<Song, String> columnFile, columnTitle, columnAuthor, columnAlbum, columnGenre;

    public PlayerScreenController(FXEnvironment environment, PlayerController controller) {
        this.controller = controller;
        this.environment = environment;
        this.environment.loadScreen(FXMLSCREEN, this);
        this.lockedPositionSlider = false;
        tableView.setPlaceholder(new Label(""));
        tableView.setRowFactory(tr -> {
            TableRow<Song> row = new TableRow<>();
            return row;
        });

        // Sets the choices for the reproduction modes
        choiceMode.getItems().add("Shuffle");
        choiceMode.getItems().add("Straight"); 
        choiceMode.getItems().add("Loop song");
        choiceMode.getItems().add("Loop Playlist");
        choiceMode.getSelectionModel().selectFirst();
        choiceMode.getSelectionModel().selectedItemProperty()
                .addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                    switch (newValue) {
                    case "Shuffle":
                        controller.setMode(PlayMode.SHUFFLE);
                        break;
                    case "Straight":
                        controller.setMode(PlayMode.NO_LOOP);
                        break;           
                    case "Loop song":
                        controller.setMode(PlayMode.LOOP_ONE);
                        break;
                    case "Loop Playlist":
                        controller.setMode(PlayMode.LOOP_ALL);
                        break;
                    }
                });

        // Sets the right-click menù on a song displayed in the table
        MenuItem addToPlaylist = new MenuItem("Aggiungi a playlist");
        addToPlaylist.setOnAction(e -> {
            List<Playlist> choices = controller.getObservablePlaylists().stream()
                    .filter(p -> !p.getName().equals("default")).collect(Collectors.toList());
            ChoiceDialog<Playlist> dialog = new ChoiceDialog<>(choices.get(0), choices);
            // dialog.
            dialog.setTitle("Aggiungi a playlist");
            dialog.setHeaderText(
                    "Scegli la playlist in cui inserire " + tableView.getSelectionModel().getSelectedItem().getName());
            Optional<Playlist> result = dialog.showAndWait();
            result.ifPresent(playlist -> controller.addSongToPlaylist(tableView.getSelectionModel().getSelectedItem(),
                    playlist));
        });
        tableView.setContextMenu(new ContextMenu(addToPlaylist));

        // Sets the double-click event on the songs in the table
        tableView.setRowFactory(tv -> {
            TableRow<Song> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Song rowData = row.getItem();
                    controller.selectSong(rowData);
                }
            });
            return row;
        });

        // Sets the click event on the listView items (playlists) which will
        // display the content in the table
        listView.setItems(controller.getObservablePlaylists());
        listView.setOnMouseClicked(e -> {
            try {
                tableView.setItems(
                        controller.getObservablePlaylistContent(listView.getSelectionModel().getSelectedItem()));
            } catch (Exception x) {
            }
        });

        listView.setCellFactory(new Callback<ListView<Playlist>, ListCell<Playlist>>() {
            @Override
            public ListCell<Playlist> call(ListView<Playlist> lv) {
                ListCell<Playlist> cell = new ListCell<Playlist>() {
                    @Override
                    public void updateItem(Playlist item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null) {
                            setText(null);
                        } else {
                            if (item.getName().equals("default")) {
                                setText("Tutti i brani");
                            } else {
                                setText(item.getName());
                            }
                        }
                    }
                };
                return cell;
            }
        });

        //Sets the columns bindings
        columnFile.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        columnTitle.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMetaData().retrieve(MetaData.TITLE)));
        columnAuthor.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMetaData().retrieve(MetaData.ARTIST)));
        columnAlbum.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMetaData().retrieve(MetaData.ALBUM)));
        columnGenre.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMetaData().retrieve(MetaData.GENRE)));
        volumeSlider.valueProperty().addListener((ov, old_val, new_val) -> {
            controller.setVolume(new_val.intValue());
            System.out.println("VOLUME: "+new_val);
            });

    }

    @Override
    public void show() {
        this.primaryStage = this.environment.getMainStage();
        this.primaryStage.setOnCloseRequest(e -> System.exit(0));
        this.environment.displayScreen(FXMLSCREEN);
    }

    public void updatePosition() {
        System.out.println("Pos");
    }

    @FXML
    private void newPlaylist() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nuova playlist");
        dialog.setHeaderText("Inserire il nome della nuova palylist");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            System.out.println("NEW PLAYLIST: " + result.get());
            this.controller.newPlaylist(result.get());
        }
    }

    @FXML
    private void play() {
        controller.play();
    }

    @FXML
    private void stopPlay() {
        controller.stop();
    }

    @FXML
    private void next() {
        controller.next();
    }

    @FXML
    private void prev() {
        controller.previous();
    }

    @FXML
    private void openFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new ExtensionFilter("Audio file", "*.mp3", "*.wav"));
        List<File> openedFiles = fileChooser.showOpenMultipleDialog(primaryStage);
        if (openedFiles != null)
            openedFiles.forEach(f -> {
                try {
                    controller.loadSong(f);
                } catch (IllegalArgumentException a) {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Errore");
                    alert.setHeaderText("Impossibile aprire il file " + f.getName());
                    alert.setContentText("Il file potrebbe essere danneggiato o in un formato non valido.");
                    alert.showAndWait();
                } catch (IOException i) {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Errore");
                    alert.setHeaderText("Impossibile accedere alla playlist.");
                    alert.setContentText(
                            "Le canzoni saranno comunque disponibili alla riproduzione ma non saranno memorizzate in maniera permanente.");
                    alert.showAndWait();
                }
            });
    }

    @FXML
    private void changePosition() {
        controller.moveToMoment(positionSlider.getValue());
        lockedPositionSlider = false;
    }

    @Override
    public void updatePosition(Integer ms, Integer lenght) {
        if (!positionSlider.isValueChanging() && lockedPositionSlider == false)
            positionSlider.setValue((ms * 10000) / lenght);

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

    @FXML
    private void lockSlider() {
        lockedPositionSlider = true;
    }

    @FXML
    private void gotoEditor() {
        System.out.println("gotoEditor");
        this.environment.loadScreen(FXMLScreens.EDITOR,
                new EditorScreenController(this.environment, new EditorControllerImpl()));
        this.environment.displayScreen(FXMLScreens.EDITOR);
    }

    @Override
    public void updateReproductionInfo(Song song) {
        Platform.runLater(() -> {
            labelSong.setText(song.getName());
        });
    }

}

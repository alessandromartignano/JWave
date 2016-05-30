package org.jwave.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.jwave.controller.player.ClockAgent;
import org.jwave.controller.player.PlaylistController;
import org.jwave.model.editor.DynamicEditorPlayerImpl;
import org.jwave.model.player.DynamicPlayer;
import org.jwave.model.player.DynamicPlayerImpl;
import org.jwave.model.player.MetaDataRetriever;
import org.jwave.model.player.Playlist;
import org.jwave.model.player.PlaylistManager;
import org.jwave.model.player.PlaylistManagerImpl;
import org.jwave.model.player.Song;
import org.jwave.model.player.SongImpl;
import org.jwave.view.PlayerUIObserver;

import com.sun.corba.se.impl.encoding.OSFCodeSetRegistry.Entry;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class Controller implements PlayerUIObserver {

    private final DynamicPlayer player;
    private final DynamicPlayer editorPlayer;
    private final PlaylistManager manager;
    private final ClockAgent agent;

    private final ObservableList<Playlist> playlists;
    private final Map<Playlist, ObservableList<Song>> songs;

    Controller() {

        try {
            PlaylistController.checkDefaultDir();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        this.player = new DynamicPlayerImpl();
        this.editorPlayer = new DynamicEditorPlayerImpl(new DynamicPlayerImpl());
        this.manager = new PlaylistManagerImpl(PlaylistController.loadDefaultPlaylist());
        this.agent = new ClockAgent(player, player, manager, "agent"); //!!!!!!!!! playerx2
        this.agent.startClockAgent();
        try {
            manager.setAvailablePlaylists(PlaylistController.reloadAvailablePlaylists());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        manager.setQueue(manager.getDefaultPlaylist());

        this.playlists = FXCollections.observableArrayList(this.manager.getAvailablePlaylists());

        this.songs = new HashMap<>();
        this.manager.getAvailablePlaylists().forEach(e -> {
            songs.put(e, FXCollections.observableArrayList(e.getPlaylistContent()));
        });

    }

    @Override
    public void loadSong(final File song) throws Exception {
        System.out.println("load " + song.getPath() + "  " + song);

        this.manager.addAudioFile(song);

        
        //player.setPlayer(new SongImpl(song));
        //songs.get(manager.getDefaultPlaylist()).add(song);

        manager.getDefaultPlaylist().getPlaylistContent().forEach(e -> {
            System.out.println(e.getName());
        });
        
        /*
        if (this.player.isEmpty()) {
            manager.setQueue(manager.getDefaultPlaylist());
            this.player
                    .setPlayer(manager.selectSongFromPlayingQueueAtIndex(manager.getPlayingQueue().getDimension() - 1));
        }
        */
    }

    @Override
    public void loadSong(Path path) {

    }

    @Override
    public void play() {
        if (this.player.isPlaying()) {
            this.player.pause();
        } else {
            if (!player.isEmpty()) {
                this.player.play();
            }
        }
    }

    @Override
    public void stop() {
        this.player.stop();
    }

    @Override
    public void next() {
        final boolean wasPlaying = this.player.isPlaying();
        final Optional<Song> nextSong = this.manager.next();
        if (nextSong.isPresent()) {
            this.player.setPlayer(nextSong.get());
            if (wasPlaying) {
                this.player.play();
            }
        }
    }

    @Override
    public void previous() {
        final boolean wasPlaying = this.player.isPlaying();
        final Optional<Song> prevSong = this.manager.prev();
        if (prevSong.isPresent()) {
            this.player.setPlayer(prevSong.get());
            if (wasPlaying) {
                this.player.play();
            }
        }
    }

    @Override
    public void newPlaylist(String name) {
        Playlist newPlaylist = this.manager.createNewPlaylist(name);
        this.playlists.add(newPlaylist);
        this.songs.put(newPlaylist, FXCollections.observableArrayList());
        try {
            PlaylistController.savePlaylistToFile(newPlaylist, name);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void addSongToPlaylist(Song song, Playlist playlist) {
        playlist.addSong(song);
        songs.get(playlist).add(song);
    }

    @Override
    public void selectSong(Song song) {
        // System.out.println("select "+ song.getAbsolutePath());
        this.player.setPlayer(song);
        this.player.play();
    }

    public ObservableList<Playlist> getObservablePlaylists() {
        return this.playlists;
    }

    @Override
    public void moveToMoment(Double percentage) {
        this.player.cue((int) ((percentage * player.getLength()) / 100));
    }

    @Override
    public ObservableList<Song> getObservablePlaylistContent(Playlist playlist) {

        System.out.println(playlist.getName() + " SONGS ");
        this.songs.get(playlist).forEach(e -> {
            System.out.println(e.toString());
        });
        return songs.get(playlist);
    }
}

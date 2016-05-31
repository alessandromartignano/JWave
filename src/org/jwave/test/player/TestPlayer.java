package org.jwave.test.player;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.NoSuchElementException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.jwave.model.player.DynamicPlayer;
import org.jwave.model.player.DynamicPlayerImpl;
import org.jwave.model.player.Playlist;
import org.jwave.model.player.PlaylistImpl;
import org.jwave.model.player.PlaylistManager;
import org.jwave.model.player.PlaylistManagerImpl;
import org.jwave.model.player.Song;
import org.jwave.model.player.SongImpl;

public class TestPlayer {

    private static final String PATH_ONE = "/home/canta/Music/Mistery.mp3";
    private static final String PATH_TWO = "/home/canta/Music/Snow Time.mp3";
    private static Song songOne;
    private static Song songTwo;
    private static DynamicPlayer player;
    private static PlaylistManager manager;
    
    @BeforeClass
    public static void oneTimeSetUp() {
        player = new DynamicPlayerImpl();
        songOne = new SongImpl(new File(PATH_ONE));
        songTwo = new SongImpl(new File(PATH_TWO));
        manager = new PlaylistManagerImpl(new PlaylistImpl("defaultProva"));
    }
    
//    @Before
//    public void setUp() {
//        
//    }
    
    @Test
    public void testPlayerInitialization() {
        assertTrue("Player should be empty.", player.isEmpty());
        assertFalse("Player should have not started", player.hasStarted());
        assertFalse("Player should not be paused", player.isPaused());
    }
    
    @Test
    public void testPlayerSetUpAndReproduction() {
        player.setPlayer(songOne);
        assertFalse("Player should not be empty", player.isEmpty());
        player.play();
        assertTrue("Player should have started", player.hasStarted());
        assertTrue("Player should be playing", player.isPlaying());
        player.pause();
        assertTrue("Player should be in pause", player.isPaused());
        player.stop();
        assertTrue("Player should have started the loaded song at least one time.", player.hasStarted());
    }
    
    @Test
    public void testPlaylistManagerInitialization() {
        assertEquals("No song should have been loaded in the default playlsit", 
                manager.getDefaultPlaylist().getDimension(), 0);
        assertEquals("The playing queue should correspond to the default playlist", manager.getDefaultPlaylist(), manager.getPlayingQueue());
        try {
            manager.selectSongFromPlayingQueueAtIndex(0);
            fail("Expected IllegalStateException to be thrown");
        } catch (IllegalArgumentException ex) { }
    }
    
    @Test
    public void testPlaylistManagerFunctionalities() {
        try {
            manager.addAudioFile(new File(PATH_ONE));
        } catch (Exception ex) {
            fail("An exception occurring while creating file");
        }
        assertEquals("Now default playlist dimension should be 1.", manager.getDefaultPlaylist().getDimension(), 1); 
        final Playlist playlist = manager.createNewPlaylist("z1b");
        try {
            final Playlist playlistTwo = manager.createNewPlaylist("z1b");
            fail("Expected an IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException ie) { }
        manager.setQueue(playlist);
        assertEquals("the playing queue isn't equal to the playlist set as new playing queue", manager.getPlayingQueue(), playlist);        
        assertEquals("the playing queue" + playlist.getName() + "should be empty", playlist.getDimension(), 0);
        manager.getPlayingQueue().addSong(songOne);
        assertEquals("the playing queue" + playlist.getName() + "should have one song.", playlist.getDimension(), 1);
        manager.deletePlaylist(playlist.getPlaylistID());
        manager.setQueue(manager.getDefaultPlaylist());
        try {
            manager.selectPlaylist(playlist.getPlaylistID());
            fail("Expected a NoSuchElementException to be thrown");
        } catch (NoSuchElementException ie) { }
    }
}
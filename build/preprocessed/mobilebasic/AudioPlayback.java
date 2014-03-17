package mobilebasic;

import java.io.IOException;
import java.io.InputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.VolumeControl;

/**
 * @author kiriman
 */
public class AudioPlayback {

    public void play(String path) {
        String type = null;
        try {
            if (path.startsWith("http:")) {
                Manager.createPlayer(path).start();
            } else {
                if (!(type = path.toLowerCase()).endsWith(".mid") && !path.endsWith(".kar") && !path.endsWith(".midi")) {
                    if (path.endsWith(".imy")) {
                        type = "audio/imy";
                    } else if (path.endsWith(".bas")) {
                        type = "audio/bas";
                    } else if (!path.endsWith(".wav") && !path.endsWith(".wave")) {
                        if (path.endsWith(".amr")) {
                            type = "audio/amr";
                        } else if (path.endsWith(".mp3")) {
                            type = "audio/mpeg";
                        } else if (path.endsWith(".m4a")) {
                            type = "audio/m4a";
                        } else if (path.endsWith(".aac")) {
                            type = "audio/aac";
                        } else if (path.endsWith(".wma")) {
                            type = "audio/x-ms-wma";
                        }
                    } else {
                        type = "audio/x-wav";
                    }
                } else {
                    type = "audio/midi";
                }

                Object is;
                if (path.startsWith("file:")) {
                    is = ((FileConnection) Connector.open(path)).openDataInputStream();
                } else {
                    is = getClass().getResourceAsStream("/" + path);
                }

                Player player;
                (player = Manager.createPlayer((InputStream) is, type)).realize();
                player.prefetch();
                ((VolumeControl) player.getControl("VolumeControl")).setLevel(100);
                player.start();
            }

        } catch (MediaException e) {
        } catch (IOException e) {
        }
    }

    public static void playTone(int note, int duration) {
        try {
            Manager.playTone(60 + note, duration, 100);
        } catch (MediaException e) {
        }
    }
}

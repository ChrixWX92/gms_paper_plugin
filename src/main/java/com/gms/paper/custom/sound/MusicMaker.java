package com.gms.paper.custom.sound;

import cn.nukkit.Player;
import cn.nukkit.level.Sound;
import cn.nukkit.math.Vector3;

public class MusicMaker {

    public MusicMaker() {

    }

    public static void playNote(Player p, Note n, Sound instrument){
        playNote(p, n, instrument, 1F);
    }
    public static void playNote(Player p, Note n, Sound instrument, float volume){
        p.getLevel().addSound(new Vector3(p.x,p.y,p.z), instrument, volume, n.getPitch());
    }
    public static void playChord(Player p, Chord chord, Sound instrument){
        for (Note n : chord.getNotes()){
            p.getLevel().addSound(new Vector3(p.x,p.y,p.z), instrument, 1F, n.getPitch());
        }
    }
    public static void playArpeggio(Player p, Chord chord, long speed, Sound instrument) {
        Arpeggio arpeggio = new Arpeggio(p, chord, speed, instrument);
        arpeggio.start();
    }
    public static void playArpeggio(Player p, Chord[] chords, long speed, Sound instrument) {
        Arpeggio arpeggio = new Arpeggio(p, chords, speed, instrument);
        arpeggio.start();
    }
    public static void playSFX(SFX.Type type, Player player){
        SFX sfx = new SFX(type, player, 0, 0);
        sfx.start();
    }
    public static void playSFX(SFX.Type type, Player player, long speed, int iterations){
        SFX sfx = new SFX(type, player, speed, iterations);
        sfx.start();
    }
}


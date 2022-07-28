package com.gms.paper.custom.sound;

import cn.nukkit.Player;
import cn.nukkit.level.Sound;
import cn.nukkit.math.Vector3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class Arpeggio extends Thread{

    private final Player p;
    private final Chord c;
    private final long sp;
    private final Sound ins;

    public Arpeggio(Player player, Chord chord, long speed, Sound instrument) {
        p = player;
        c = chord;
        sp = speed;
        ins = instrument;
    }

    public Arpeggio(Player player, Chord[] chords, long speed, Sound instrument) {
        p = player;
        sp = speed;
        ins = instrument;
        List<Note> notes = new ArrayList<>();

        for (Chord chord : chords) {notes.addAll(Arrays.asList(chord.getNotes()));}

        Comparator<Note> noteComparator = Comparator.comparingDouble(Note::getPitch);
        notes.sort(noteComparator);

        Note[] array = notes.toArray(new Note[notes.size()]);
        //Note[] notesArr = (Note[]) notes.toArray();
        c = new Chord(array);
    }

    public void run() {
        try{
            Note[] notes = c.getNotes();
            for (Note n : notes) {
                p.getLevel().addSound(new Vector3(p.x,p.y,p.z), ins, 1F, n.getPitch());
                synchronized (this) {
                    this.wait(sp);
                }
            }
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }
}




package com.gms.paper.custom.sound;

import java.util.Arrays;

public class Chord {

    private final Note[] notes;

    public Chord(Note[] notes){this.notes = notes;}

    public Chord(Note root){this(root, ChordType.MAJ);}

    public Chord(Note root, ChordType type){this(root, type, 0, false);}

    public Chord(Note root, ChordType type, int inversion, boolean octave){

        Note[] allNotes = Note.getNotes();
        Interval[] intervals = type.getIntervals();
        Interval[] octaveIntervals = Arrays.copyOf(intervals,(intervals.length+1));
        octaveIntervals[(octaveIntervals.length-1)] = Interval.PERF_8TH;

        if (octave){
            this.notes = new Note[(octaveIntervals.length+1)];
        } else{
            this.notes = new Note[(intervals.length+1)];
        }

        Note note = allNotes[Arrays.asList(allNotes).indexOf(root)];
        this.notes[0] = note;
        int i = 0;

        if (octave){
            for (Interval interval : octaveIntervals) {
                note = allNotes[((Arrays.asList(allNotes).indexOf(root))+interval.getSemitones())];
                this.notes[i+1] = note;
                i++;
            }
        } else{
            for (Interval interval : intervals) {
                note = allNotes[((Arrays.asList(allNotes).indexOf(root))+interval.getSemitones())];
                this.notes[i+1] = note;
                i++;
            }
        }

        for (i = 0; i < inversion; i++) {

            Note inv = allNotes[((Arrays.asList(allNotes).indexOf(this.notes[0])) + (Interval.PERF_8TH.getSemitones()))];

            for (Note n : this.notes) {
                if((Arrays.asList(notes).indexOf(n)) > 0) {
                    this.notes[((Arrays.asList(notes).indexOf(n)) - 1)] = n;
                }
            }

            if (inv.getPitch() == this.notes[this.notes.length-1].getPitch()) {
                inv = allNotes[((Arrays.asList(allNotes).indexOf(this.notes[0])) + (Interval.PERF_8TH.getSemitones()))];
            }

            while (inv.getPitch() < this.notes[this.notes.length-1].getPitch()) {
                inv = allNotes[((Arrays.asList(allNotes).indexOf(this.notes[0])) + (Interval.PERF_8TH.getSemitones()))];
            }

            this.notes[(this.notes.length-1)] = inv;

        }

    }

    public Note[] getNotes() {
        return notes;
    }

}

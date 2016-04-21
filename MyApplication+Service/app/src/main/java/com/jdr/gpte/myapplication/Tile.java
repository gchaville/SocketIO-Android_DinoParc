package com.jdr.gpte.myapplication;

import android.view.View;

/**
 * Created by samue on 2016-03-16.
 */
public class Tile {
    String tag;
    Boolean isAvailable;
    public enum type {
        vide(0),
        cage(1),
        booth(12),
        tyranosaure(2),
        velociraptor(3),
        brontosaure(4),
        triceratops(5),
        Restaurant(6),
        Security(7),
        Bathroom(8),
        Casino(9),
        Spy(10),
        Paleontologist(11);

        public int value;
        private type (final int ty) {value = ty;}
    }

    public type tuileType;
    View tile;
    public int col, row;

    Tile (int i, int j, View v) {
        col = i;
        row = j;
        tile = v;
        tuileType = type.vide;
        isAvailable = true;
    }

    Tile () {

    }
}

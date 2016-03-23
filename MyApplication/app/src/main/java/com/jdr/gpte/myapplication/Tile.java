package com.jdr.gpte.myapplication;

import android.view.View;

/**
 * Created by samue on 2016-03-16.
 */
public class Tile {
    String tag;
    Boolean isAvailable;
    public enum type {
        vide,
        cage,
        tyranosaure,
        velociraptor,
        brontosaure,
        triceratops,
        Restaurant,
        Security,
        Bathroom,
        Casino,
        Spy,
        Paleontologist;

    }
    type tuileType;
    View tile;
    int col, row;

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

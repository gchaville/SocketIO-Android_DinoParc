package com.jdr.gpte.myapplication;

import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.app.Activity;

/**
 * Created by samue on 2016-03-24.
 */
public class Dinos {
    Tile tuile;
    ImageView image;
    int visitors;

    Dinos(int col, int row, int draw, Tile t, GridLayout grid, Activity main,int vis, int dim) {
        tuile = t;
        tuile.col = col;
        tuile.row = row;
        GridLayout.LayoutParams param = new GridLayout.LayoutParams();
        image = new ImageView(main);
        image.setImageResource(draw);
        image.setTag("Dino" + draw);
        param.height = dim;
        param.width = dim;
        param.columnSpec = GridLayout.spec(col);
        param.rowSpec = GridLayout.spec(row);
        image.setLayoutParams(param);
        grid.addView(image);
        visitors = vis;
    }
}

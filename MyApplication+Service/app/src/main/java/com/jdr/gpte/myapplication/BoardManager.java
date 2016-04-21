package com.jdr.gpte.myapplication;

import android.widget.GridLayout;
import android.widget.ImageView;
import android.app.Activity;
import android.widget.TextView;

/**
 * Created by samue on 2016-03-22.
 */
public class BoardManager {
        Tile [][] tuiles = new Tile[11][15];

        BoardManager (GridLayout grid, Activity main, int dim) {
                int col = 11;
                int row = 15;
                grid.setColumnCount(col);
                grid.setRowCount(row);
                for (int x =0;x<col;x++) {
                        for (int y =0;y<row;y++) {
                                if(y>2 || x>1) {
                                        String tag = "tile_"+x+"_"+y;
                                        ImageView oImageView = new ImageView(main);
                                        oImageView.setImageResource(R.drawable.tile);
                                        oImageView.setTag(tag);
                                        GridLayout.LayoutParams param =new GridLayout.LayoutParams();
                                        param.height = dim;
                                        param.width = dim;
                                        param.columnSpec = GridLayout.spec(x);
                                        param.rowSpec = GridLayout.spec(y);
                                        oImageView.setLayoutParams (param);
                                        Tile tuile = new Tile(x, y, oImageView);
                                        tuile.tag = tag;
                                        tuiles[x][y] = tuile;
                                        grid.addView(tuile.tile);
                                }
                        }
                }
        }
}

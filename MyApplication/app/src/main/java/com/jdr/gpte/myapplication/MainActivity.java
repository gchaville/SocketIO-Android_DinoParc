package com.jdr.gpte.myapplication;

import android.media.Image;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.sql.Struct;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    class views{
        int x ,y;
        View Button;
        Tile tuile_def = new Tile();
        Boolean onBoard = true;
        views(int i, int j, View b){
            x = i;
            y = j;
            Button = b;
        }
    };
    BoardManager board;
    int achat = 1;
    int col = 4,row=4;
    private ArrayList<views> arrow = new ArrayList<views>();
    private int Bcage =0;
    public ImageButton validate;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GridLayout grid = (GridLayout) findViewById(R.id.grid);
        board = new BoardManager(grid, this);
    }

    //Méthode qui se déclenchera lorsque vous appuierez sur le bouton menu du téléphone
    public boolean onCreateOptionsMenu(Menu menu) {
        ((TextView)findViewById(R.id.texte)).setText("Menu");
        //Création d'un MenuInflater qui va permettre d'instancier un Menu XML en un objet Menu
        MenuInflater inflater = getMenuInflater();
        //Instanciation du menu XML spécifier en un objet Menu
        inflater.inflate(R.menu.option, menu);
        return true;
    }
    //Méthode qui se déclenchera au clic sur un item
    public boolean onOptionsItemSelected(MenuItem item) {
        //On regarde quel item a été cliqué grâce à  son id et on déclenche une action
        switch (item.getItemId()) {
            case R.id.Park:
                ((TextView)findViewById(R.id.texte)).setText("Park");
                return true;
            case R.id.Cage:
                ((TextView)findViewById(R.id.texte)).setText("Cage");
                buycage(R.drawable.cage, 1);
                return true;
            case R.id.Ads:
                ((TextView)findViewById(R.id.texte)).setText("Ads");
                return true;
            case R.id.Booth:
                ((TextView)findViewById(R.id.texte)).setText("Booth");
                buy(R.drawable.booth);
                return true;
            case R.id.Restaurant:
                ((TextView)findViewById(R.id.texte)).setText("Restaurant");
                buy(R.drawable.restaurant);
                return true;
            case R.id.Security:
                ((TextView)findViewById(R.id.texte)).setText("Security");
                buy(R.drawable.securite);
                return true;
            case R.id.Bathroom:
                ((TextView)findViewById(R.id.texte)).setText("Bathroom");
                buy(R.drawable.washroom);
                return true;
            case R.id.Casino:
                ((TextView)findViewById(R.id.texte)).setText("Casino");
                buy(R.drawable.casino);
                return true;
            case R.id.Spy:
                ((TextView)findViewById(R.id.texte)).setText("Spy");
                buy(R.drawable.espionnage);
                return true;
            case R.id.Paleontologist:
                ((TextView)findViewById(R.id.texte)).setText("Paleontologist");
                buy(R.drawable.paleantologue);
                return true;
            case R.id.Dino:
                ((TextView)findViewById(R.id.texte)).setText("Dino");
                return true;
            case R.id.quit:
                ((TextView)findViewById(R.id.texte)).setText("quit");
                //Pour fermer l'activité il suffit de faire finish()
                finish();
                return true;
        }
        return false;
    }

    void buycage (int option, int ty) {
        RelativeLayout rel = (RelativeLayout) findViewById(R.id.frame);
        GridLayout grid = (GridLayout) findViewById(R.id.grid);
        for (int i = 0; i < arrow.size(); i++) {
            grid.removeViewInLayout(arrow.get(i).Button);
        }
        arrow.clear();
        Bcage++;
        ImageButton oImageView = new ImageButton(this);
        oImageView.setImageResource(option);
        oImageView.setTag("Cage" + Bcage);
        GridLayout.LayoutParams param = new GridLayout.LayoutParams();
        param.height = 48;
        param.width = 48;
        param.columnSpec = GridLayout.spec(col);
        param.rowSpec = GridLayout.spec(row);
        oImageView.setLayoutParams(param);
        views image = new views(col, row, oImageView);
        image.tuile_def.tuileType = Tile.type.cage;
        arrow.add(image);
        grid.addView(image.Button);
        ImageView Cageview = new ImageView(this);
        Cageview.setImageResource(option);
        Cageview.setTag("Cage" + Bcage);
        param = new GridLayout.LayoutParams();
        param.height = 48;
        param.width = 48;
        param.columnSpec = GridLayout.spec(col);
        param.rowSpec = GridLayout.spec(row+1);
        oImageView.setLayoutParams(param);
        image = new views(col, row+1, Cageview);
        image.tuile_def.tuileType = Tile.type.cage;
        arrow.add(image);
        grid.addView(image.Button);
        for (int j = 0 ; j < 2;j++) {
            Cageview = new ImageView(this);
            Cageview.setImageResource(option);
            Cageview.setTag("Cage" + Bcage);
            param = new GridLayout.LayoutParams();
            param.height = 48;
            param.width = 48;
            param.columnSpec = GridLayout.spec(col+1);
            param.rowSpec = GridLayout.spec(row+j);
            oImageView.setLayoutParams(param);
            image = new views(col+1, row+j, Cageview);
            image.tuile_def.tuileType = Tile.type.cage;
            arrow.add(image);
            grid.addView(image.Button);
        }
        arrows(grid, col - 1, row, R.drawable.arrow_west);
        arrows(grid, col + 2, row, R.drawable.arrow_east);
        arrows(grid, col, row + 2, R.drawable.arrow_south);
        arrows(grid, col, row - 1, R.drawable.arrow_north);
        frame(grid, col - 1, row + 1, R.drawable.invisible_tile);
        frame(grid, col - 1, row - 1, R.drawable.invisible_tile);
        frame(grid, col - 1, row + 2, R.drawable.invisible_tile);
        frame(grid, col + 2, row+1, R.drawable.invisible_tile);
        frame(grid, col + 2, row - 1, R.drawable.invisible_tile);
        frame(grid, col + 2, row + 2, R.drawable.invisible_tile);
        frame(grid, col + 1, row - 1, R.drawable.invisible_tile);
        frame(grid, col + 1, row + 2, R.drawable.invisible_tile);
        arrow.get(4).Button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                move(-1, 0);
            }
        });
        arrow.get(5).Button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                move(1, 0);
            }
        });
        arrow.get(6).Button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                move(0, 1);
            }
        });
        arrow.get(7).Button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                move(0, -1);
            }
        });
        validate = new ImageButton(this);
        validate.setImageResource(R.drawable.validate);
        validate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                purchase();
            }
        });
        achat =4;
        move(1,1);
    }

    void buy (int option) {
        GridLayout grid = (GridLayout) findViewById(R.id.grid);
        RelativeLayout rel = (RelativeLayout) findViewById(R.id.frame);
        for (int i =0;i<arrow.size();i++){
            grid.removeViewInLayout(arrow.get(i).Button);
        }
        arrow.clear();
        ImageButton oImageView = new ImageButton(this);
        oImageView.setImageResource(option);
        oImageView.setTag("Cage"+Bcage);
        Bcage++;
        GridLayout.LayoutParams param =new GridLayout.LayoutParams();
        param.height = 48;
        param.width = 48;
        param.columnSpec = GridLayout.spec(col);
        param.rowSpec = GridLayout.spec(row);
        oImageView.setLayoutParams(param);
        views image= new views(col,row,oImageView);
        switch (option) {
            case R.drawable.booth:
                image.tuile_def.tuileType = Tile.type.booth;
                break;
            case R.drawable.restaurant:
                image.tuile_def.tuileType = Tile.type.Restaurant;
                break;
            case R.drawable.securite:
                image.tuile_def.tuileType = Tile.type.Security;
                break;
            case R.drawable.washroom:
                image.tuile_def.tuileType = Tile.type.Bathroom;
                break;
            case R.drawable.casino:
                image.tuile_def.tuileType = Tile.type.Casino;
                break;
            case R.drawable.espionnage:
                image.tuile_def.tuileType = Tile.type.Spy;
                break;
            case R.drawable.paleantologue:
                image.tuile_def.tuileType = Tile.type.Paleontologist;
                break;
        }
        arrow.add(image);
        grid.addView(image.Button);
        arrows(grid, col - 1, row, R.drawable.arrow_west);
        arrows(grid, col + 1, row, R.drawable.arrow_east);
        arrows(grid, col, row+1, R.drawable.arrow_south);
        arrows(grid, col, row-1, R.drawable.arrow_north);
        frame(grid, col - 1, row + 1, R.drawable.invisible_tile);
        frame(grid, col - 1, row - 1, R.drawable.invisible_tile);
        frame(grid, col + 1, row + 1, R.drawable.invisible_tile);
        frame(grid, col + 1, row - 1, R.drawable.invisible_tile);
        arrow.get(1).Button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                move(-1, 0);
            }
        });
        arrow.get(2).Button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                move(1, 0);
            }
        });
        arrow.get(3).Button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                move(0, +1);
            }
        });
        arrow.get(4).Button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                move(0, -1);
            }
        });
        validate = new ImageButton(this);
        validate.setImageResource(R.drawable.validate);
        validate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                purchase();
            }
        });
        rel.addView(validate);
        achat = 1;
        move(1,1);
    }

    void arrows(GridLayout grid, int x, int y, int draw){
        ImageButton oImageView = new ImageButton(this);
        oImageView.setImageResource(draw);
        oImageView.setTag("Cage_1");
        GridLayout.LayoutParams param =new GridLayout.LayoutParams();
        param.height = 48;
        param.width = 48;
        param.columnSpec = GridLayout.spec(x);
        param.rowSpec = GridLayout.spec(y);
        oImageView.setLayoutParams(param);
        views image= new views(x,y,oImageView);
        grid.addView(image.Button);
        arrow.add(image);
    }

    void frame (GridLayout grid, int x, int y, int draw) {
        ImageView oImageView = new ImageView(this);
        oImageView.setImageResource(draw);
        oImageView.setTag("Cage_1");
        GridLayout.LayoutParams param = new GridLayout.LayoutParams();
        param.height = 48;
        param.width = 48;
        param.columnSpec = GridLayout.spec(x);
        param.rowSpec = GridLayout.spec(y);
        oImageView.setLayoutParams(param);
        views image = new views(x, y, oImageView);
        grid.addView(image.Button);
        arrow.add(image);
    }

    public void move(int x, int y){
        RelativeLayout rel = (RelativeLayout) findViewById(R.id.frame);
        Boolean available = true;
        for(int i =0 ; i<arrow.size();i++) {
            arrow.get(i).x+=x;
            arrow.get(i).y+=y;
            GridLayout grid = (GridLayout) findViewById(R.id.grid);
            GridLayout.LayoutParams param =new GridLayout.LayoutParams();
            arrow.get(i).onBoard = false;
            grid.removeViewInLayout(arrow.get(i).Button);
            if (arrow.get(i).x<10 && arrow.get(i).x>=0 && (arrow.get(i).x>2 || arrow.get(i).y>1) && arrow.get(i).y<14 && arrow.get(i).y>=0) {
                param.height = 48;
                param.width = 48;
                param.columnSpec = GridLayout.spec(arrow.get(i).x);
                param.rowSpec = GridLayout.spec(arrow.get(i).y);
                arrow.get(i).Button.setLayoutParams(param);
                grid.addView(arrow.get(i).Button);
                arrow.get(i).onBoard = true;
            }
        }
        for(int i =0 ; i<achat;i++) {
            if (arrow.get(i).onBoard) {
                if (available) {
                    if (board.tuiles[arrow.get(i).x][arrow.get(i).y].isAvailable) {
                        rel.removeViewInLayout(validate);
                        rel.addView(validate);
                    } else {
                        rel.removeViewInLayout(validate);
                        available = false;
                    }
                }
            }
        }
        for(int i = achat; i< arrow.size(); i++) {
            if (arrow.get(i).onBoard) {
                if (available) {
                    if (board.tuiles[arrow.get(i).x][arrow.get(i).y].tuileType.value == 0 || board.tuiles[arrow.get(i).x][arrow.get(i).y].tuileType == arrow.get(0).tuile_def.tuileType) {
                        rel.removeViewInLayout(validate);
                        rel.addView(validate);
                    } else {
                        rel.removeViewInLayout(validate);
                        available = false;
                    }
                }
            }
        }
    }

    public void purchase(){
        RelativeLayout rel = (RelativeLayout) findViewById(R.id.frame);
        GridLayout grid = (GridLayout) findViewById(R.id.grid);
        for(int i = achat ; i<arrow.size();i++) {
            grid.removeViewInLayout(arrow.get(i).Button);
        }
        for(int i =  0; i<achat ;i++) {
            board.tuiles[arrow.get(i).x][arrow.get(i).y].isAvailable = false;
            board.tuiles[arrow.get(i).x][arrow.get(i).y].tuileType = arrow.get(i).tuile_def.tuileType;
        }
        arrow.clear();
        rel.removeViewInLayout(validate);
    }
}

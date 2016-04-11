package com.jdr.gpte.myapplication;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.os.Vibrator;
import android.content.Context;
import 	android.media.MediaPlayer;

import android.app.NotificationManager;
import android.util.Log;

import java.sql.Struct;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private MySocket mSocketService;
    private boolean mServiceBound = false;

    private String mUsername;
    private String mGameid;

    class views{
        View Button;
        Tile tuile_def = new Tile();
        Boolean onBoard = true;
        views(int i, int j, View b){
            tuile_def.col = i;
            tuile_def.row = j;
            Button = b;
        }
    };

    BoardManager board;
    int achat = 1, price;
    int col = 4,row=4;

    private ArrayList<views> arrow = new ArrayList<views>();
    private int Bcage =0;

    public ImageButton validate;
    public ImageButton bron;
    public ImageButton velo;
    public ImageButton tyr;
    public ImageButton tri;
    Boolean tyranosaurus = false;
    Boolean brontosaurus = false;
    Boolean triceratop = false;
    Boolean velociraptor = false;
    private ArrayList<Dinos> dinosaur = new ArrayList<Dinos>();
    Dinos dino;
    Activity main = this;
    Player player;
    int dim;
    int turn = 1, turnMax = 10;



    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        dim = (int) getResources().getDimension(R.dimen.image_view_heigth);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();

        if(intent != null) {
            mUsername = intent.getExtras().getString(Constants.EXTRA_NAME);
            mGameid = intent.getExtras().getString(Constants.EXTRA_GAMEID);
        }

        Intent serviceIntent = new Intent(MainActivity.this, MySocket.class);
        bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        GridLayout grid = (GridLayout) findViewById(R.id.grid);
        board = new BoardManager(grid, this, dim);
        validate = new ImageButton(this);
        bron = new ImageButton(this);
        velo = new ImageButton(this);
        tyr = new ImageButton(this);
        tri = new ImageButton(this);
        tri.setImageResource(R.drawable.triceratops);
        tyr.setImageResource(R.drawable.tyrannosaure);
        velo.setImageResource(R.drawable.velociraptor);
        bron.setImageResource(R.drawable.brontosaure);
        player = new Player();
        ((TextView)findViewById(R.id.num_cash)).setText(player.money + "");
        ((TextView)findViewById(R.id.num_visitors)).setText(player.visitors + "");
        ((TextView)findViewById(R.id.TurnMax)).setText(turnMax + " turns");
        incomePhase();
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
                achat = 0;
                price = 0;
                player.money += player.visitors;
                endTurn();
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
        final GridLayout grid = (GridLayout) findViewById(R.id.grid);
        rel.removeViewInLayout(validate);
        for (int i = 0; i < arrow.size(); i++) {
            grid.removeViewInLayout(arrow.get(i).Button);
        }
        arrow.clear();
        Bcage++;

        ImageButton oImageView = new ImageButton(this);
        oImageView.setImageResource(option);
        oImageView.setTag("Cage" + Bcage);

        GridLayout.LayoutParams param = new GridLayout.LayoutParams();
        param.height = dim;
        param.width = dim;
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
        param.height = dim;
        param.width = dim;
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
            param.height = dim;
            param.width = dim;
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
        price = 5;
        if (player.money>=price) {
            validate.setImageResource(R.drawable.validate);
            validate.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    purchase();
                }
            });
            rel.addView(validate);
        } else {
            ((TextView)findViewById(R.id.texte)).setText("No money");
        }
        achat =4;
        move(1,1);
    }

    void buy (int option) {
        GridLayout grid = (GridLayout) findViewById(R.id.grid);
        RelativeLayout rel = (RelativeLayout) findViewById(R.id.frame);
        rel.removeViewInLayout(validate);
        for (int i =0;i<arrow.size();i++){
            grid.removeViewInLayout(arrow.get(i).Button);
        }
        arrow.clear();
        ImageButton oImageView = new ImageButton(this);
        oImageView.setImageResource(option);
        oImageView.setTag("Cage" + Bcage);
        Bcage++;
        GridLayout.LayoutParams param =new GridLayout.LayoutParams();
        param.height = dim;
        param.width = dim;
        param.columnSpec = GridLayout.spec(col);
        param.rowSpec = GridLayout.spec(row);
        oImageView.setLayoutParams(param);
        final views image= new views(col,row,oImageView);
        switch (option) {
            case R.drawable.booth:
                image.tuile_def.tuileType = Tile.type.booth;
                price = 205;
                break;
            case R.drawable.restaurant:
                image.tuile_def.tuileType = Tile.type.Restaurant;
                price = 5;
                break;
            case R.drawable.securite:
                image.tuile_def.tuileType = Tile.type.Security;
                price = 5;
                break;
            case R.drawable.washroom:
                image.tuile_def.tuileType = Tile.type.Bathroom;
                price = 5;
                break;
            case R.drawable.casino:
                image.tuile_def.tuileType = Tile.type.Casino;
                price = 5;
                break;
            case R.drawable.espionnage:
                image.tuile_def.tuileType = Tile.type.Spy;
                price = 5;
                break;
            case R.drawable.paleantologue:
                image.tuile_def.tuileType = Tile.type.Paleontologist;
                price = 5;
                break;
        }
        arrow.add(image);
        grid.addView(image.Button);
        arrows(grid, col - 1, row, R.drawable.arrow_west);
        arrows(grid, col + 1, row, R.drawable.arrow_east);
        arrows(grid, col, row + 1, R.drawable.arrow_south);
        arrows(grid, col, row - 1, R.drawable.arrow_north);
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
        if (player.money>=price) {
            validate.setImageResource(R.drawable.validate);
            validate.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    purchase();
                }
            });
            rel.addView(validate);
        } else {
            ((TextView)findViewById(R.id.texte)).setText("No money");
        }
        achat = 1;
        move(1,1);
    }

    void arrows(GridLayout grid, int x, int y, int draw){
        ImageButton oImageView = new ImageButton(this);
        oImageView.setImageResource(draw);
        oImageView.setTag("Cage_1");
        GridLayout.LayoutParams param =new GridLayout.LayoutParams();
        param.height = dim;
        param.width = dim;
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
        param.height = dim;
        param.width = dim;
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
            arrow.get(i).tuile_def.col+=x;
            arrow.get(i).tuile_def.row+=y;
            GridLayout grid = (GridLayout) findViewById(R.id.grid);
            GridLayout.LayoutParams param =new GridLayout.LayoutParams();
            arrow.get(i).onBoard = false;
            grid.removeViewInLayout(arrow.get(i).Button);
            if (arrow.get(i).tuile_def.col<10 && arrow.get(i).tuile_def.col>=0 && (arrow.get(i).tuile_def.col>2 || arrow.get(i).tuile_def.row>1) && arrow.get(i).tuile_def.row<14 && arrow.get(i).tuile_def.row>=0) {
                param.height = dim;
                param.width = dim;
                param.columnSpec = GridLayout.spec(arrow.get(i).tuile_def.col);
                param.rowSpec = GridLayout.spec(arrow.get(i).tuile_def.row);
                arrow.get(i).Button.setLayoutParams(param);
                grid.addView(arrow.get(i).Button);
                arrow.get(i).onBoard = true;
            }
        }
        for(int i =0 ; i<achat;i++) {
            if (arrow.get(i).onBoard) {
                if (available) {
                    if (board.tuiles[arrow.get(i).tuile_def.col][arrow.get(i).tuile_def.row].isAvailable && player.money>=price) {
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
                    if (board.tuiles[arrow.get(i).tuile_def.col][arrow.get(i).tuile_def.row].tuileType.value == 0 || board.tuiles[arrow.get(i).tuile_def.col][arrow.get(i).tuile_def.row].tuileType == arrow.get(0).tuile_def.tuileType && player.money>=price) {
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
        final RelativeLayout rel = (RelativeLayout) findViewById(R.id.frame);
        GridLayout grid = (GridLayout) findViewById(R.id.grid);

        for(int i = achat ; i<arrow.size();i++) {
            grid.removeViewInLayout(arrow.get(i).Button);
        }
        for(int i =  0; i<achat ;i++) {
            board.tuiles[arrow.get(i).tuile_def.col][arrow.get(i).tuile_def.row].isAvailable = false;
            board.tuiles[arrow.get(i).tuile_def.col][arrow.get(i).tuile_def.row].tuileType = arrow.get(i).tuile_def.tuileType;
        }
        if(arrow.get(0).tuile_def.tuileType == Tile.type.cage) {
            final int x = arrow.get(0).tuile_def.col+1;
            final int y = arrow.get(0).tuile_def.row+1;
            View button = arrow.get(0).Button;
            final MediaPlayer mpTrex = MediaPlayer.create(this, R.raw.trex);
            MediaPlayer Velo = MediaPlayer.create(this, R.raw.velociraptor);
            arrow.get(0).Button.setOnClickListener(new View.OnClickListener() {
                Tile t = new Tile();
                GridLayout grid = (GridLayout) findViewById(R.id.grid);

                public void onClick(View v) {
                    tri.setOnClickListener(new View.OnClickListener() {
                        Tile t = new Tile();
                        GridLayout grid = (GridLayout) findViewById(R.id.grid);

                        public void onClick(View v) {
                            t.tuileType = Tile.type.triceratops;
                            dino = new Dinos(x, y, R.drawable.triceratops, t, grid, main, 5, dim);
                            for (int i = 0; i < 2; i++) {
                                for (int j = 0; j < 2; j++) {
                                    System.out.println(i + " " + j);
                                    board.tuiles[x][y].tuileType = Tile.type.triceratops;
                                }
                            }
                            dinosaur.add(dino);
                            price = 10;
                            emitAction(88, t);
                            endTurn();
                        }
                    });
                    bron.setOnClickListener(new View.OnClickListener() {
                        Tile t = new Tile();
                        GridLayout grid = (GridLayout) findViewById(R.id.grid);

                        public void onClick(View v) {
                            t.tuileType = Tile.type.brontosaure;
                            dino = new Dinos(x, y, R.drawable.brontosaure, t, grid, main, 1, dim);
                            for (int i = 0; i < 2; i++) {
                                for (int j = 0; j < 2; j++) {
                                    System.out.println(i + " " + j);
                                    board.tuiles[x][y].tuileType = Tile.type.brontosaure;
                                }
                            }
                            dinosaur.add(dino);
                            price = 2;
                            emitAction(88, t);
                            endTurn();
                        }
                    });
                    velo.setOnClickListener(new View.OnClickListener() {
                        Tile t = new Tile();
                        GridLayout grid = (GridLayout) findViewById(R.id.grid);

                        public void onClick(View v) {
                            t.tuileType = Tile.type.velociraptor;
                            dino = new Dinos(x, y, R.drawable.velociraptor, t, grid, main, 2, dim);
                            for (int i = 0; i < 2; i++) {
                                for (int j = 0; j < 2; j++) {
                                    System.out.println(i + " " + j);
                                    board.tuiles[x][y].tuileType = Tile.type.velociraptor;
                                }
                            }
                            dinosaur.add(dino);
                            price = 5;
                            emitAction(88, t);
                            endTurn();
                        }
                    });
                    tyr.setOnClickListener(new View.OnClickListener() {
                        Tile t = new Tile();
                        GridLayout grid = (GridLayout) findViewById(R.id.grid);

                        public void onClick(View v) {
                            mpTrex.start();
                            t.tuileType = Tile.type.tyranosaure;
                            dino = new Dinos(x, y, R.drawable.tyrannosaure, t, grid, main, 10, dim);
                            for (int i = 0; i < 2; i++) {
                                for (int j = 0; j < 2; j++) {
                                    System.out.println(i + " " + j);
                                    board.tuiles[x][y].tuileType = Tile.type.tyranosaure;
                                }
                            }
                            dinosaur.add(dino);
                            price = 25;
                            emitAction(88, t);
                            endTurn();
                        }
                    });
                    GridLayout dinos = (GridLayout) findViewById(R.id.dino_button);
                    GridLayout.LayoutParams param = new GridLayout.LayoutParams();
                    int column = 0;
                    param.height = dim;
                    param.width = dim;
                    param.rowSpec = GridLayout.spec(0);
                    param.columnSpec = GridLayout.spec(column);
                    if (player.money >= 2 && !brontosaurus) {
                        System.out.println(column);
                        bron.setLayoutParams(param);
                        dinos.addView(bron);
                        brontosaurus = true;
                        column++;
                    }
                    if (player.money >= 5 && !velociraptor) {
                        param = new GridLayout.LayoutParams();
                        param.height = dim;
                        param.width = dim;
                        param.rowSpec = GridLayout.spec(0);
                        param.columnSpec = GridLayout.spec(column);
                        System.out.println(column);
                        velo.setLayoutParams(param);
                        dinos.addView(velo);
                        column++;
                        velociraptor = true;
                    }
                    if (player.money >= 10 && !triceratop) {
                        param = new GridLayout.LayoutParams();
                        param.height = dim;
                        param.width = dim;
                        param.rowSpec = GridLayout.spec(0);
                        param.columnSpec = GridLayout.spec(column);
                        System.out.println(column);
                        tri.setLayoutParams(param);
                        dinos.addView(tri);
                        triceratop = true;
                        column++;
                    }
                    if (player.money >= 25 && !tyranosaurus) {
                        param = new GridLayout.LayoutParams();
                        param.height = dim;
                        param.width = dim;
                        param.rowSpec = GridLayout.spec(0);
                        param.columnSpec = GridLayout.spec(column);
                        System.out.println(column);
                        tyr.setLayoutParams(param);
                        dinos.addView(tyr);
                        tyranosaurus = true;
                    }
                }
            });
            emitAction(R.drawable.cage, arrow.get(0).tuile_def);
        }
        else
            emitAction(R.drawable.booth, arrow.get(0).tuile_def);

        arrow.clear();
        endTurn();
    }

    void endTurn () {
        RelativeLayout rel = (RelativeLayout) findViewById(R.id.frame);
        GridLayout grid = (GridLayout) findViewById(R.id.grid);

        rel.removeViewInLayout(validate);
        for(int i = 0 ; i<arrow.size();i++) {
            grid.removeViewInLayout(arrow.get(i).Button);
        }
        player.money = player.money - price;
        System.out.println(player.money);
        for (int i = 0; i < dinosaur.size();i++) {
            player.visitors += dinosaur.get(i).visitors;
            System.out.println(dinosaur.get(i).visitors);
        }
        dino_button();
        System.out.println(player.visitors);
        ((TextView) findViewById(R.id.num_cash)).setText(player.money + "");
        ((TextView)findViewById(R.id.num_visitors)).setText(player.visitors + "");
        if (turn >= turnMax) {
            Intent intent = new Intent(getApplicationContext(), GameOver.class);
            startActivity(intent);
            finish();
        }
        else {
            turn++;
            ((TextView)findViewById(R.id.Turns)).setText(turn + "");
            Bundle infos = new Bundle();
            infos.putString(Constants.EXTRA_NAME, mUsername);
            infos.putString(Constants.EXTRA_GAMEID, mGameid);

            Intent intent = new Intent(MainActivity.this, TURN_SCREEN.class);
            intent.putExtras(infos);
            startActivity(intent);
            incomePhase();
        }
    }

    void incomePhase () {
        //MediaPlayer mpTurn = MediaPlayer.create(this, R.raw.bell);
        //mpTurn.start();
        ((TextView) findViewById(R.id.Turns)).setText(turn + "");
        // Get instance of Vibrator from current Context
       // Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
       // v.vibrate(400);
        player.money+= player.visitors*2;
        ((TextView)findViewById(R.id.num_cash)).setText(player.money + "");
    }

    void dino_button() {
        GridLayout dinos = (GridLayout) findViewById(R.id.dino_button);
        if (tyranosaurus) {
            dinos.removeViewInLayout(tyr);
            tyranosaurus = false;
        }
        if (triceratop) {
            dinos.removeViewInLayout(tri);
            triceratop = false;
        }
        if (brontosaurus) {
            dinos.removeViewInLayout(bron);
            brontosaurus = false;
        }
        if (velociraptor) {
            dinos.removeViewInLayout(velo);
            velociraptor = false;
        }

    }

    void emitAction(int option, Tile tile) {
        Intent emitIntent = new Intent();
        emitIntent.setAction(Constants.PLAYER_ACTION);

        switch(option) {
            case R.drawable.cage:
                emitIntent.putExtra("gameId", mGameid);
                emitIntent.putExtra("playerName", mUsername);
                emitIntent.putExtra("action", "playerBuyCage");
                emitIntent.putExtra("coordX", tile.col);
                emitIntent.putExtra("coordY", tile.row);
                break;

            case R.drawable.booth:
                emitIntent.putExtra("gameId", mGameid);
                emitIntent.putExtra("playerName", mUsername);
                emitIntent.putExtra("action", "playerBuyBooth");
                emitIntent.putExtra("boothType", tile.tuileType.value);
                emitIntent.putExtra("coordX", tile.col);
                emitIntent.putExtra("coordY", tile.row);
                break;

            case 88:
                emitIntent.putExtra("gameId", mGameid);
                emitIntent.putExtra("playerName", mUsername);
                emitIntent.putExtra("action", "playerBuyDino");
                emitIntent.putExtra("dinoType", tile.tuileType.value);
                emitIntent.putExtra("coordX", tile.col);
                emitIntent.putExtra("coordY", tile.row);
                break;
        }
        sendBroadcast(emitIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("Connect_afta", "onStart");
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.cancel(0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mServiceBound) {
            unbindService(mServiceConnection);
            mServiceBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //mSocket.disconnect();
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MySocket.MyBinder myBinder = (MySocket.MyBinder) service;
            mSocketService = myBinder.getService();
            mServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
        }
    };
}

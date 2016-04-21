package com.jdr.gpte.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
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

import java.util.ArrayList;
import java.util.List;

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
        Log.i("Main", "onCreate");
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
                emitAction(R.id.Ads, new Tile());
                endTurn();
                return true;
            case R.id.Booth:
                ((TextView)findViewById(R.id.texte)).setText("Booth");
                buy(R.drawable.booth);
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

        setView(option, grid, Tile.type.cage, col, row + 1);
        setView(option, grid, Tile.type.cage, col, row);
        for (int j = 0 ; j < 2;j++) {
            setView(option, grid, Tile.type.cage, col+1, row+j);
        }
        arrows(grid, col - 1, row, R.drawable.arrow_west);
        arrows(grid, col + 2, row, R.drawable.arrow_east);
        arrows(grid, col, row + 2, R.drawable.arrow_south);
        arrows(grid, col, row - 1, R.drawable.arrow_north);
        frame(grid, col - 1, row + 1, R.drawable.invisible_tile);
        frame(grid, col - 1, row - 1, R.drawable.invisible_tile);
        frame(grid, col - 1, row + 2, R.drawable.invisible_tile);
        frame(grid, col + 2, row + 1, R.drawable.invisible_tile);
        frame(grid, col + 2, row - 1, R.drawable.invisible_tile);
        frame(grid, col + 2, row + 2, R.drawable.invisible_tile);
        frame(grid, col + 1, row - 1, R.drawable.invisible_tile);
        frame(grid, col + 1, row + 2, R.drawable.invisible_tile);
        setArrows(3);
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
        GridLayout.LayoutParams param =new GridLayout.LayoutParams();
        param.height = dim;
        param.width = dim;
        param.columnSpec = GridLayout.spec(col);
        param.rowSpec = GridLayout.spec(row);
        oImageView.setLayoutParams(param);
        final views image= new views(col,row,oImageView);

        AlertDialog.Builder popUpMenu = new AlertDialog.Builder(this);
        popUpMenu.setTitle("Select a type");

        popUpMenu.setItems(R.array.items_booth, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    //--------------- BOOTHS ---------------
                    case 0:
                        System.out.print("Resto bought");
                        image.tuile_def.tuileType = Tile.type.Restaurant;
                        price = 5;
                        break;
                    case 1:
                        System.out.print("Secu bought");
                        image.tuile_def.tuileType = Tile.type.Security;
                        price = 5;
                        break;
                    case 2:
                        System.out.print("Bath bought");
                        image.tuile_def.tuileType = Tile.type.Bathroom;
                        price = 5;
                        break;
                    case 3:
                        System.out.print("Casino bought");
                        image.tuile_def.tuileType = Tile.type.Casino;
                        price = 5;
                        break;
                    case 4:
                        System.out.print("Spy bought");
                        image.tuile_def.tuileType = Tile.type.Spy;
                        price = 5;
                        break;
                    case 5:
                        System.out.print("Paleontologist bought");
                        image.tuile_def.tuileType = Tile.type.Paleontologist;
                        price = 5;
                        break;
                }
            }
        });
        popUpMenu.create().show();
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
        setArrows(0);
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

    void setView(int option, GridLayout grid, Tile.type type, int col, int row) {
        ImageView Cageview = new ImageView(this);
        Cageview.setImageResource(option);
        Cageview.setTag("Cage" + Bcage);
        GridLayout.LayoutParams param = new GridLayout.LayoutParams();
        param.height = dim;
        param.width = dim;
        param.columnSpec = GridLayout.spec(col);
        param.rowSpec = GridLayout.spec(row);
        Cageview.setLayoutParams(param);
        views image = new views(col, row, Cageview);
        image.tuile_def.tuileType = type;
        arrow.add(image);
        grid.addView(image.Button);
    }

    void setArrows (int debut) {
        arrow.get(debut+1).Button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                move(-1, 0);
            }
        });
        arrow.get(debut+2).Button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                move(1, 0);
            }
        });
        arrow.get(debut+3).Button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                move(0, +1);
            }
        });
        arrow.get(debut+4).Button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                move(0, -1);
            }
        });
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
            if (arrow.get(i).tuile_def.col<11 && arrow.get(i).tuile_def.col>=0 && (arrow.get(i).tuile_def.col>1 || arrow.get(i).tuile_def.row>2) && arrow.get(i).tuile_def.row<15 && arrow.get(i).tuile_def.row>=0) {
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
            int col = arrow.get(i).tuile_def.col, row = arrow.get(i).tuile_def.row;
            if (arrow.get(i).onBoard) {
                if (available) {
                    Tile.type tile = board.tuiles[col][row].tuileType;
                    if (arrow.get(0).tuile_def.tuileType == Tile.type.cage) {
                        if (tile.value == 0 || tile == arrow.get(0).tuile_def.tuileType || tile  == Tile.type.brontosaure || tile == Tile.type.tyranosaure || tile == Tile.type.triceratops || tile == Tile.type.velociraptor  && player.money>=price) {
                            rel.removeViewInLayout(validate);
                            rel.addView(validate);
                        } else {
                            rel.removeViewInLayout(validate);
                            available = false;
                        }
                    } else {
                        if (tile.value == 0 || (tile == arrow.get(0).tuile_def.tuileType || tile  == Tile.type.Spy || tile == Tile.type.Casino || tile == Tile.type.Bathroom || tile == Tile.type.Restaurant || tile == Tile.type.Security || tile == Tile.type.Paleontologist)  && player.money>=price) {
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

        if(arrow.get(1).tuile_def.tuileType == Tile.type.cage) {
            final int x = arrow.get(1).tuile_def.col+1;
            final int y = arrow.get(1).tuile_def.row+1;
            View button = arrow.get(1).Button;

            final MediaPlayer mpTrex = MediaPlayer.create(this, R.raw.trex2);
            final MediaPlayer mpVelo = MediaPlayer.create(this, R.raw.velociraptor);
            final MediaPlayer mpBron = MediaPlayer.create(this, R.raw.brontosaurus2);
            final MediaPlayer mpTri = MediaPlayer.create(this, R.raw.triceratops);

            final AlertDialog.Builder popUpMenu = new AlertDialog.Builder(this);
            popUpMenu.setTitle("Select a type");

            arrow.get(1).Button.setOnClickListener(new View.OnClickListener() {
                Boolean buy = false;
                int nbDino = 0;
                Tile t = new Tile();
                GridLayout grid = (GridLayout) findViewById(R.id.grid);

                public void onClick(View v) {
                    dino_button();
                    List<String> strings = new ArrayList<String>();

                    if (t.tuileType == Tile.type.velociraptor) {
                        strings.clear();
                        dino_button();
                        velociraptor = true;
                        strings.add("Velociraptor");
                        final CharSequence[] item = strings.toArray(new String[strings.size()]);

                        popUpMenu.setItems(item, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    //--------------- VELOCIRAPTOR ----------------
                                    case 0:
                                        clickVelo();
                                        break;
                                }
                            }
                        });
                    } else if (t.tuileType == Tile.type.brontosaure) {
                        strings.clear();
                        dino_button();
                        brontosaurus = true;
                        strings.add("Brontosaurus");
                        final CharSequence[] item = strings.toArray(new String[strings.size()]);

                        popUpMenu.setItems(item, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    //--------------- BRONTOSAURUS ----------------
                                    case 0:
                                        clickBron();
                                        break;
                                }
                            }
                        });
                    } else {
                        if (player.money >= 2 && !brontosaurus && nbDino < 2 && !buy) {
                            brontosaurus = true;
                            strings.add("Brontosaurus");
                        }
                        if (player.money >= 5 && !velociraptor && nbDino < 4 && !buy) {
                            velociraptor = true;
                            strings.add("Velociraptor");
                        }
                        if (player.money >= 10 && !triceratop && nbDino < 1 && !buy) {
                            triceratop = true;
                            strings.add("Triceratops");
                        }
                        if (player.money >= 25 && !tyranosaurus && nbDino < 1 && !buy) {
                            tyranosaurus = true;
                            strings.add("Tyrannosaurus");
                        }

                        final CharSequence[] items_list = strings.toArray(new String[strings.size()]);
                        popUpMenu.setItems(items_list, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    //--------------- DINOS ----------------
                                    case 0:
                                        clickBron();
                                        break;
                                    case 1:
                                        clickVelo();
                                        break;
                                    case 2:
                                        clickTric();
                                        break;
                                    case 3:
                                        clickTyra();
                                        break;
                                }
                            }
                        });
                    }
                    if (buy) {
                        popUpMenu.setMessage("Cage remplie").
                                setCancelable(true).
                                setNegativeButton("Close", null);
                    }
                    popUpMenu.create().show();
                }

                private void clickBron() {
                    System.out.print("Bronto bought");
                    mpBron.start();
                    t.tuileType = Tile.type.brontosaure;
                    nbDino++;
                    switch (nbDino) {
                        case 1:
                            dino = new Dinos(x, y, R.drawable.brontosaure, t, grid, main, 2, dim);
                            break;
                        case 2:
                            dino = new Dinos(x - 1, y, R.drawable.brontosaure, t, grid, main, 2, dim);
                            break;
                    }
                    for (int i = 0; i < 2; i++) {
                        for (int j = 0; j < 2; j++) {
                            //System.out.println(i + " " + j);
                            board.tuiles[x - i][y - j].tuileType = Tile.type.brontosaure;
                        }
                    }
                    dinosaur.add(dino);
                    price = 2;
                    if (nbDino >= 2) {
                        buy = true;
                    }
                    emitAction(88, dino.tuile);
                    endTurn();
                }

                private void clickVelo() {
                    System.out.print("Velo bought");
                    mpVelo.start();
                    t.tuileType = Tile.type.velociraptor;
                    nbDino++;
                    switch (nbDino) {
                        case 1:
                            dino = new Dinos(x, y, R.drawable.velociraptor, t, grid, main, 2, dim);
                            break;
                        case 2:
                            dino = new Dinos(x - 1, y, R.drawable.velociraptor, t, grid, main, 2, dim);
                            break;
                        case 3:
                            dino = new Dinos(x, y - 1, R.drawable.velociraptor, t, grid, main, 2, dim);
                            break;
                        case 4:
                            dino = new Dinos(x - 1, y - 1, R.drawable.velociraptor, t, grid, main, 2, dim);
                            break;
                    }
                    for (int i = 0; i < 2; i++) {
                        for (int j = 0; j < 2; j++) {
                            //System.out.println(i + " " + j);
                            board.tuiles[x - i][y - j].tuileType = Tile.type.velociraptor;
                        }
                    }
                    dinosaur.add(dino);
                    price = 5;
                    if (nbDino >= 4) {
                        buy = true;
                    }
                    emitAction(88, dino.tuile);
                    endTurn();
                }

                private void clickTric() {
                    System.out.print("Tric bought");
                    mpTri.start();
                    t.tuileType = Tile.type.triceratops;
                    dino = new Dinos(x, y, R.drawable.triceratops, t, grid, main, 5, dim);
                    for (int i = 0; i < 2; i++) {
                        for (int j = 0; j < 2; j++) {
                            //System.out.println(i + " " + j);
                            board.tuiles[x - i][y - j].tuileType = Tile.type.triceratops;
                        }
                    }
                    dinosaur.add(dino);
                    price = 10;
                    nbDino++;
                    buy = true;
                    emitAction(88, dino.tuile);
                    endTurn();
                }

                private void clickTyra() {
                    System.out.print("Tyra bought");
                    mpTrex.start();
                    t.tuileType = Tile.type.tyranosaure;
                    dino = new Dinos(x, y, R.drawable.tyrannosaure, t, grid, main, 10, dim);
                    for (int i = 0; i < 2; i++) {
                        for (int j = 0; j < 2; j++) {
                            //System.out.println(i + " " + j);
                            board.tuiles[x - i][y - j].tuileType = Tile.type.tyranosaure;
                        }
                    }
                    dinosaur.add(dino);
                    price = 25;
                    nbDino++;
                    buy = true;
                    emitAction(88, dino.tuile);
                    endTurn();
                }
            });
            emitAction(R.drawable.cage, arrow.get(1).tuile_def);
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
        turn++;
        incomePhase();
    }

    void incomePhase () {
        ((TextView) findViewById(R.id.Turns)).setText(turn + "");
        player.money+= player.visitors*2;
        ((TextView)findViewById(R.id.num_cash)).setText(player.money + "");
        Log.i("Main", "turn " + turn + "/" + turnMax );
        if (turn >= turnMax) {
            Intent intent = new Intent(getApplicationContext(), GameOver.class);
            startActivity(intent);
            finish();
        }
        else {
            ((TextView)findViewById(R.id.Turns)).setText(turn + "");
            Bundle infos = new Bundle();
            infos.putString(Constants.EXTRA_NAME, mUsername);
            infos.putString(Constants.EXTRA_GAMEID, mGameid);
            //infos.putBoolean(Constants.EXTRA_NEWTURN, false);

            Intent intent = new Intent(this, TURN_SCREEN.class);
            intent.putExtras(infos);
            startActivity(intent);
        }
    }

    void dino_button() {
        GridLayout dinos = (GridLayout) findViewById(R.id.dino_button);
        if (tyranosaurus) {
            tyranosaurus = false;
        }
        if (triceratop) {
            triceratop = false;
        }
        if (brontosaurus) {
            brontosaurus = false;
        }
        if (velociraptor) {
            velociraptor = false;
        }

    }

    void emitAction(int option, Tile tile) {

        // Adjust col and row for the server
        tile.col += 1;
        tile.row += 1;

        Intent emitIntent = new Intent();
        emitIntent.setAction(Constants.PLAYER_ACTION);

        switch(option) {
            case R.drawable.cage:
                emitIntent.putExtra("gameId", mGameid);
                emitIntent.putExtra("playerName", mUsername);
                emitIntent.putExtra("action", "playerBuyCage");
                emitIntent.putExtra("coordX", tile.row);
                emitIntent.putExtra("coordY", tile.col);
                break;

            case R.drawable.booth:
                emitIntent.putExtra("gameId", mGameid);
                emitIntent.putExtra("playerName", mUsername);
                emitIntent.putExtra("action", "playerBuyBooth");
                emitIntent.putExtra("boothType", tile.tuileType.value);
                emitIntent.putExtra("coordX", tile.row);
                emitIntent.putExtra("coordY", tile.col);
                break;

            case 88:
                emitIntent.putExtra("gameId", mGameid);
                emitIntent.putExtra("playerName", mUsername);
                emitIntent.putExtra("action", "playerBuyDino");
                emitIntent.putExtra("dinoType", tile.tuileType.value);
                emitIntent.putExtra("coordX", tile.row);
                emitIntent.putExtra("coordY", tile.col);
                break;

            case R.id.Ads:
                emitIntent.putExtra("gameId", mGameid);
                emitIntent.putExtra("playerName", mUsername);
                emitIntent.putExtra("action", "playerMakeAds");
                break;
        }
        sendBroadcast(emitIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("Main", "onStart");
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.cancel(0);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        Log.i("Main", "onSaveInstanceState");
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    protected  void onPause() {
        super.onPause();
        Log.i("Main", "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("Main", "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("Main", "onDestroy");
        //mSocket.disconnect();
        if(mServiceBound) {
            unbindService(mServiceConnection);
            mServiceBound = false;
        }
        stopService(new Intent(this, MySocket.class));
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

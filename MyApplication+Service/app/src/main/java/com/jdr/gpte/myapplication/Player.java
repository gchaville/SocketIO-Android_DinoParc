package com.jdr.gpte.myapplication;

/**
 * Created by samue on 2016-03-27.
 */
public class Player {

    public int money, visitors;

    public enum playerAction {
        buyCage,
        buyDinos,
        buyBooth,
        makeAds;

        playerAction() {}
    };

    public playerAction action;

    Player () {
        money = 15;
        visitors = 0;
    }
}

package com.stonecraft.datastore.datastoredemo.app;

import com.stonecraft.datastore.DbTableName;

/**
 * This class
 * <p/>
 * Author: michaeldelaney
 * Created: 4/01/16
 */
public class JoinTest {
    @DbTableName("SHORT_LIST")
    private Shortlist myShortlist;
    @DbTableName("SHORT_LIST_JOIN")
    private ShortlistJoin myShortlistJoin;

    public Shortlist getShortlist() {
        return myShortlist;
    }

    public ShortlistJoin getShortlistJoin() {
        return myShortlistJoin;
    }
}

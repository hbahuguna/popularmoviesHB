package com.himanshubahuguna.android.popularmovieshb.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hbahuguna on 11/26/2015.
 */
public class SearchResponse {

    private List<Result> results = new ArrayList<Result>();

    private int page;

    public List<Result> getResults() {
        return results;
    }

    public int getPage() {
        return page;
    }
}

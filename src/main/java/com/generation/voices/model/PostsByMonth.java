package com.generation.voices.model;

import java.util.ArrayList;
import java.util.List;

public class PostsByMonth {

    int month;
    int year;
    List<BlogPost> posts = new ArrayList<BlogPost>();


    public PostsByMonth(int month, int year)
    {
        this.month = month;
        this.year = year;
    }

    @Override
    public String toString()
    {
        String res = month+"\n";
        for(BlogPost post:posts)
            res+=post+"\n";
        return res;
    }



}
package com.generation.voices.model;

import java.util.ArrayList;
import java.util.List;

public class PostsByYear {

    // i mesi sono un vettore di liste di post
    int year;
    PostsByMonth[] months = new PostsByMonth[12];

    public PostsByYear(int year)
    {
        this.year = year;
        for(int i=0;i<12;i++)
            months[i] = new PostsByMonth(i+1, year);
    }


    @Override
    public String toString()
    {
        String res = "YEAR "+year+"\n";
        for(PostsByMonth month:months)
            res += month;
        res+="\n===========================\n";
        return res;
    }

}

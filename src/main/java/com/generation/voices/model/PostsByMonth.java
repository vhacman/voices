package com.generation.voices.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

// @Data genera i getter per i campi package-private (getMonth(), getYear(), getPosts())
// necessari a BlogService per accedere ai dati.
@Data
public class PostsByMonth
{
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
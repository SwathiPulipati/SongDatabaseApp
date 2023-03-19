package com.example.finalprojecttake2;

public class Song {

    Object name, ragam, type;

    public Song(Object name, Object ragam, Object type){
        this.name = name;
        this.ragam = ragam;
        this.type = type;
    }

    public String getName() {
        return (String) name;
    }

    public String getRagam() {
        return (String) ragam;
    }

    public String getType() {
        return (String) type;
    }
}

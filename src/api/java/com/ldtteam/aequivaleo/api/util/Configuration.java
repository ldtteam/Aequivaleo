package com.ldtteam.aequivaleo.api.util;


//TODO: Enable config again in 1.14+
public class Configuration
{
    public static Persistence persistence = new Persistence();

    public static class Persistence
    {
        public boolean prettyPrint = true;
    }
}
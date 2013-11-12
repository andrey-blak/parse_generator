package com.example.annotations;

import blak.annotations.simple.RActivity;
import blak.annotations.simple.Repeat;

@RActivity
public class RootActivity {
    @Repeat
    public int view = 1;
}

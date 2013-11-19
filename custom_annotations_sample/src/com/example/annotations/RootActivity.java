package com.example.annotations;

import blak.annotations.EActivity;
import blak.annotations.ViewById;
import blak.annotations.simple.RActivity;
import blak.annotations.simple.Repeat;

@RActivity
@EActivity
public class RootActivity {
    @Repeat
    public int view = 1;

    @ViewById
    public String txt;
}

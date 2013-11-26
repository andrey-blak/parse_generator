package com.example.annotations.activities;

import blak.annotations.EActivity;
import blak.annotations.ViewById;

@EActivity(1)
public class MyActivity {
    @ViewById(2)
    public int alk;

    @ViewById(3)
    public int aslk;

    private void someMethod() {
    }
}

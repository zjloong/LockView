package com.heihei.hehe.lockview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LockView lockView = (LockView) findViewById(R.id.lock);
        lockView.setCallBack(new LockView.CallBack() {
            @Override
            public boolean isRight(String passWord) {
                return false;
            }
        });
    }
}

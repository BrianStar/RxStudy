package com.example.junqingfan.myapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.junqingfan.Observable;
import com.example.junqingfan.Subscriber;

public class SecondActivity extends AppCompatActivity {

    private static final String TAG = "SecondActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
    }


    public void go(View v) {
        Toast.makeText(SecondActivity.this, "kkk", Toast.LENGTH_SHORT).show();

        sampleObservable().subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {
                Log.d(TAG, "onCompleted()");
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError()", e);
            }

            @Override
            public void onNext(String string) {
                Log.d(TAG, "onNext(" + string + ")");
            }
        });
    }


    static Observable<String> sampleObservable() {
        return Observable.just("one", "two", "three", "four", "five");
    }
}

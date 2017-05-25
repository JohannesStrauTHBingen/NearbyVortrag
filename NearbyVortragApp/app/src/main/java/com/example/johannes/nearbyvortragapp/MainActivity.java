package com.example.johannes.nearbyvortragapp;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    GoogleApiClient mGoogleApiClient;
    Message mActiveMessage;
    MessageListener mMessageListener;

    EditText msg;
    Button pub, unpub, sub, unsub;
    ListView listView;

    //LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
    ArrayList<String> listItems;
    //DEFINING A STRING ADAPTER WHICH WILL HANDLE THE DATA OF THE LISTVIEW
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        msg = (EditText) findViewById(R.id.msg);
        pub = (Button) findViewById(R.id.pub);
        unpub = (Button) findViewById(R.id.unpub);
        sub = (Button) findViewById(R.id.sub);
        unsub = (Button) findViewById(R.id.unsub);

        initListView();
        setListeners();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .build();

    }

    private void initListView(){
        listView = (ListView) findViewById(R.id.list);
        listItems = new ArrayList<>();
        adapter=new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                listItems);
        listView.setAdapter(adapter);
    }

    private void setListeners(){
        pub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publish(msg.getText().toString()); // Publish message from EditText as mActiveMessage
            }
        });
        unpub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unpublish(); // Unpublish active message
            }
        });
        sub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subscribe(); // Subscribe to receive active messages
            }
        });
        unsub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unsubscribe(); // Unsubscribe from active messages
            }
        });

        mMessageListener = new MessageListener() {
            @Override
            public void onFound(Message message) {
                String messageAsString = new String(message.getContent());
                Log.d("FOUND", "Found message: " + messageAsString);
                addItemToListView(messageAsString);
            }

            @Override
            public void onLost(Message message) {
                String messageAsString = new String(message.getContent());
                Log.d("LOST", "Lost sight of message: " + messageAsString);
                removeItemFromListView(messageAsString);
            }
        };
    }

    public void addItemToListView(String item) {
        listItems.add(item);
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "found: "+item, Toast.LENGTH_LONG).show();
    }

    public void removeItemFromListView(String item){
        listItems.remove(item);
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "removed: "+item, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        super.onStart();
        mGoogleApiClient.connect();

        //publish("Hello World");
        //subscribe();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        Toast.makeText(this, "Connected to API Client", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStop() {
        if(mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    private void publish(String message) {
        unpublish();
        Log.i("PUB", "Publishing message: " + message);
        mActiveMessage = new Message(message.getBytes());
        Nearby.Messages.publish(mGoogleApiClient, mActiveMessage);
        Toast.makeText(this, "Published message: "+message, Toast.LENGTH_LONG).show();
    }

    private void unpublish() {

        if (mActiveMessage != null) {
            Log.i("UNPUB", "Unpublishing.");
            Nearby.Messages.unpublish(mGoogleApiClient, mActiveMessage);
            Toast.makeText(this, "Unpublished message: "+mActiveMessage.toString(), Toast.LENGTH_LONG).show();
            mActiveMessage = null;
        } else {
            Toast.makeText(this, "No message to unpublish.", Toast.LENGTH_LONG).show();
        }
    }

    private void subscribe() {
        Log.i("SUB", "Subscribing.");
        Nearby.Messages.subscribe(mGoogleApiClient, mMessageListener);
        Toast.makeText(this, "Subscribed", Toast.LENGTH_LONG).show();
    }

    private void unsubscribe() {
        Log.i("UNSUB", "Unsubscribing.");
        Nearby.Messages.unsubscribe(mGoogleApiClient, mMessageListener);
        Toast.makeText(this, "Unsubscribed", Toast.LENGTH_LONG).show();
    }

}

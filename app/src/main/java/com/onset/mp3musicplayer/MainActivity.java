package com.onset.mp3musicplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    String[] items;
    ArrayList<String> songList; // Müzik listesi
    TextView txtSongMain; // Mini oynatıcıdaki şarkı adı
    Button btnPlayMain;
    Button btnNextMain;
    Button btnPreviousMain;
    MediaPlayer mediaPlayer;
    int currentSongIndex = 0;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 1001;

    ArrayList<File> mySongs;

    String songName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView)findViewById(R.id.listView);
        listView = findViewById(R.id.listView);
        txtSongMain = findViewById(R.id.txtSongMain);
        btnPlayMain = findViewById(R.id.btnPlayMain);
        btnNextMain = findViewById(R.id.btnNextMain);
        btnPreviousMain = findViewById(R.id.btnPreviousMain);
        requestStoragePermission();

    }
    private void requestStoragePermission() {
        // Android 10 ve üzeri
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE);
            } else {
                displaySong();
            }
        } else {
            // Android 9 ve altı
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE);
            } else {
                displaySong();
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                displaySong();
            } else {
                Toast.makeText(this, "Depolama izni reddedildi!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public ArrayList<File> findSong (File file)
    {
        ArrayList<File> arrayList =  new ArrayList<>();
        File[] files = file.listFiles();

        for (File singleFile : files){
            if (singleFile.isDirectory() && !singleFile.isHidden())
            {
                arrayList.addAll(findSong(singleFile));
            }
            else
            {
                if (singleFile.getName().endsWith(".mp3")|| singleFile.getName().endsWith(".wav"))
                {
                    arrayList.add(singleFile);
                }
            }
        }
        return arrayList;
    }
    public void playSong(String songNamee ,int position){

        if (mediaPlayer != null)
        {
            mediaPlayer.start();
            mediaPlayer.release();
        }

        txtSongMain.setSelected(true);
        Uri uri = Uri.parse(mySongs.get(currentSongIndex).toString());
        songName = mySongs.get(currentSongIndex).getName();
        txtSongMain.setText(songName);

        mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
        mediaPlayer.start();

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                btnNextMain.performClick();
            }
        });

        btnPlayMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying())
                {
                    btnPlayMain.setBackgroundResource(R.drawable.ic_play);
                    mediaPlayer.pause();
                }
                else
                {
                    btnPlayMain.setBackgroundResource(R.drawable.ic_pause);
                    mediaPlayer.start();
                }
            }
        });

        btnPreviousMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                mediaPlayer.start();
                mediaPlayer.release();
                currentSongIndex = ((currentSongIndex-1)<0)?(mySongs.size()-1):currentSongIndex-1;
                Uri uri = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                songName = mySongs.get(currentSongIndex).getName();
                txtSongMain.setText(songName);
                btnPlayMain.setBackgroundResource(R.drawable.ic_pause);
                mediaPlayer.start();

            }
        });

        btnNextMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                currentSongIndex = ((currentSongIndex + 1) % mySongs.size());
                Uri uri = Uri.parse(mySongs.get(currentSongIndex).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                songName = mySongs.get(currentSongIndex).getName();
                txtSongMain.setText(songName);
                btnPlayMain.setBackgroundResource(R.drawable.ic_pause);
                mediaPlayer.start();
            }
        });

    }
    public void displaySong()
    {
        mySongs = findSong(Environment.getExternalStorageDirectory());
        items = new String[mySongs.size()];
        for (int i=0;i<mySongs.size();i++)
        {
            items[i] = mySongs.get(i).getName().toString().replace(".mp3", "").replace(".wav", "");
        }

        customAdapter customAdapter = new customAdapter();
        listView.setAdapter(customAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String songName = (String) listView.getItemAtPosition(i);
                currentSongIndex = i;
                playSong(songName,i);
                startActivity(new Intent(getApplicationContext(),PlayerActivity.class)
                        .putExtra("songs", mySongs)
                        .putExtra("songname", songName)
                        .putExtra("pos", i)
                );
            }
        });
    }

    class customAdapter extends BaseAdapter
    {
        @Override
        public int getCount() {
            return items.length ;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = getLayoutInflater().inflate(R.layout.list_items, null);
            TextView txtSong = view.findViewById(R.id.txtSong);
            txtSong.setSelected(true);
            txtSong.setText(items[position]);
            return view;
        }
    }
}


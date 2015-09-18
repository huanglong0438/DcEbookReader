package com.selfstudy.dc.ebookreader;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.ipaulpro.afilechooser.FileChooserActivity;
import com.ipaulpro.afilechooser.utils.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;



public class MainActivity extends ActionBarActivity {

    private StringBuffer sb = new StringBuffer();
    private ListView listView;
    private TextView tv_load;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    String filename = msg.getData().getString("filename");
                    Intent intent = new Intent(MainActivity.this,ReadingActivity.class);
                    intent.putExtra("textContent",sb.toString());
                    intent.putExtra("title", filename);
                    startActivity(intent);
                    MainActivity.this.finish();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_load = (TextView) findViewById(R.id.tv_load);
        listView = (ListView) findViewById(R.id.list_option);
        listView.setVisibility(View.VISIBLE);
        tv_load.setVisibility(View.INVISIBLE);
        ArrayAdapter adapter = new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,getData());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        Intent intent = new Intent(MainActivity.this, FileChooserActivity.class);
                        startActivityForResult(intent, 0);
                        break;
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private String[] getData() {
        return new String[]{"打开新文件"};
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {

                    final Uri uri = data.getData();

                    // Get the File path from the Uri
                    String path = FileUtils.getPath(this, uri);

                    // Alternatively, use FileUtils.getFile(Context, Uri)
                    if (path != null && FileUtils.isLocal(path)) {
                        File file = new File(path);
                        Thread tOpenFile = new OpenFile(path);

                        tOpenFile.start();
                        listView.setVisibility(View.INVISIBLE);
                        tv_load.setVisibility(View.VISIBLE);

                    }
                }
                break;
        }
    }


    public class OpenFile extends Thread {
        private String path;

        OpenFile(String path){
            this.path = path;
        }


        @Override
        public void run() {
            {
                File file = new File(path);
                sb = new StringBuffer();
                BufferedReader br = null;
                try {
                    String buffer;
                    br = new BufferedReader(new InputStreamReader(new FileInputStream(file),"gbk"));
                    while((buffer = br.readLine()) != null){
                        sb.append(buffer+"\n");
                    }
                    Message msg = new Message();
                    msg.what = 1;
                    Bundle b = new Bundle();
                    b.putString("filename",file.getName());
                    msg.setData(b);
                    handler.sendMessage(msg);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }


}

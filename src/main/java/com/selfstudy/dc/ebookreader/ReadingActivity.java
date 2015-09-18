package com.selfstudy.dc.ebookreader;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;


public class ReadingActivity extends ActionBarActivity {

    private String title;
    private String textContent;
    private TextView tv_title;
    private TextView tv_content;
    private LinearLayout ll_content;
    private TextView tv_reading_load;
    private TextView tv_percent;
    private ObservableScrollView sv_content;
    private int height=1;
    private String filename;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading);
        ll_content = (LinearLayout) findViewById(R.id.ll_content);
        tv_reading_load = (TextView) findViewById(R.id.tv_reading_load);
        ll_content.setVisibility(View.INVISIBLE);
        tv_reading_load.setVisibility(View.VISIBLE);

        tv_title = (TextView) findViewById(R.id.title);
        tv_content = (TextView) findViewById(R.id.textContent);
        tv_percent = (TextView) findViewById(R.id.tv_percent);
        final DecimalFormat df = new DecimalFormat("0.00%");
        sv_content = (ObservableScrollView) findViewById(R.id.sv_content);
        sv_content.setScrollViewListener(new ScrollViewListener() {
            @Override
            public void onScrollChanged(ObservableScrollView scrollView, int x, int y, int oldx, int oldy) {
                height = tv_content.getHeight();
                Log.d("tv_percent","y="+y);
                Log.d("tv_percent","height="+height);
                String percent = df.format(  ((double)y)/(double)height  );
                tv_percent.setText(percent);
            }
        });

        filename = getIntent().getStringExtra("title");

        new inflateContent().start();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_reading,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        int nlabel;

        //noinspection SimplifiableIfStatement
        switch (id){
            case R.id.item_open:

                break;
            case R.id.item_add_label:
                //save the current y, tv_pecent.height, and filename.
                SharedPreferences add_label_sp = getSharedPreferences(filename,MODE_PRIVATE);
                SharedPreferences.Editor editor = add_label_sp.edit();

                nlabel = add_label_sp.getInt("nlabel",0);
                editor.putInt("y"+nlabel,sv_content.getScrollY());
                editor.putInt("height"+nlabel,height);
                //get current text and
                //save current text
                int line = tv_content.getLayout().getLineForVertical(sv_content.getScrollY()-tv_content.getPaddingTop());
                int st = tv_content.getLayout().getOffsetForHorizontal(line, sv_content.getScrollX() - tv_content.getPaddingLeft());
                String digest = tv_content.getText().subSequence(st, st + 10).toString();
                editor.putString("digest"+nlabel,digest);
                Log.d("label","digest"+ nlabel +" has been saved: "+digest);
                editor.putInt("nlabel",++nlabel);

                editor.apply();
                Toast.makeText(this,"��ǩ����ɹ�",Toast.LENGTH_SHORT).show();
                break;
            case R.id.item_load_label:
                //open a new dialog and show all labels.
                //it's better to set my own view to the AlertDialog. use SwipeMenuListView on the github.
                final SharedPreferences load_label_sp = getSharedPreferences(filename, MODE_PRIVATE);
                nlabel = load_label_sp.getInt("nlabel",-1);

                if(nlabel == -1){
                    Toast.makeText(this,"��δ�������ǩ",Toast.LENGTH_SHORT).show();
                    return super.onOptionsItemSelected(item);
                }

                AlertDialog.Builder load_label_builder = new AlertDialog.Builder(this);
                ArrayList<String> labels = new ArrayList<String>();

                for(int i=0;i<nlabel;i++){
                    String load_label_digest = load_label_sp.getString("digest"+i,null);
                    if(load_label_digest != null){
                        labels.add(load_label_digest + "...");
                    }
                }
                String slabels[] = (String[]) labels.toArray(new String[labels.size()]);
                load_label_builder.setItems(slabels, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int y = load_label_sp.getInt("y"+which, 0);
                        int height = load_label_sp.getInt("height"+which, -1);
                        if (height == -1) {
                            Toast.makeText(getApplicationContext(), "��ǩ������", Toast.LENGTH_SHORT).show();

                            return;
                        }
                        sv_content.scrollTo(sv_content.getScrollX(), y);
                        Toast.makeText(getApplicationContext(), "������ǩ�ɹ�", Toast.LENGTH_SHORT).show();
                    }
                });
                load_label_builder.setTitle("ѡ��Ҫ�������ǩ");
                load_label_builder.show();

                break;
            case R.id.item_quit:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("ȷ��Ҫ�˳���");
                builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                    }
                });
                builder.setNegativeButton("ȡ��", null);
                builder.show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    private class inflateContent extends Thread{
        public void run(){
            title = getIntent().getStringExtra("title");
            textContent = getIntent().getStringExtra("textContent");
            tv_title.setText(title);
            tv_content.setText(textContent);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    ll_content.setVisibility(View.VISIBLE);
                    tv_reading_load.setVisibility(View.INVISIBLE);
                }
            });

        }
    }

}

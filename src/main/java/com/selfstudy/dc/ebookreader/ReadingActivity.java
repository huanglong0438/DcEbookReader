package com.selfstudy.dc.ebookreader;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.Inflater;


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
        getMenuInflater().inflate(R.menu.menu_reading, menu);
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
                Toast.makeText(this,"书签保存成功",Toast.LENGTH_SHORT).show();
                break;
            case R.id.item_load_label:
                //open a new dialog and show all labels.
                //it's better to set my own view to the AlertDialog. use SwipeMenuListView on the github.
                final SharedPreferences load_label_sp = getSharedPreferences(filename, MODE_PRIVATE);
                nlabel = load_label_sp.getInt("nlabel",0);

                if(nlabel == 0){
                    Toast.makeText(this,"尚未保存过书签",Toast.LENGTH_SHORT).show();
                    return super.onOptionsItemSelected(item);
                }

                final AlertDialog.Builder load_label_builder = new AlertDialog.Builder(this);
                final AlertDialog dialog = load_label_builder.create();
                ArrayList<String> labels = new ArrayList<String>();

                //filter the /n in the load_label_digest, and add a /n at last of every digest.
                for(int i=0;i<nlabel;i++){
                    String load_label_digest = load_label_sp.getString("digest"+i,null);
                    if(load_label_digest != null){
                        load_label_digest = load_label_digest.replaceAll("\\n"," ");
                        labels.add(i +". "+ load_label_digest + "...\n");
                    }
                }
                String slabels[] = labels.toArray(new String[labels.size()]);

                LayoutInflater inflater = getLayoutInflater();
                View view = inflater.inflate(R.layout.label_layout, null);
                final SwipeMenuListView listView = (SwipeMenuListView) view.findViewById(R.id.label_listView);
                dialog.setTitle("选择要载入的书签");

                SwipeMenuCreator creator = new SwipeMenuCreator() {
                    @Override
                    public void create(SwipeMenu menu) {
                        SwipeMenuItem deleteItem = new SwipeMenuItem(
                                getApplicationContext());
                        deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                                0x3F, 0x25)));
                        deleteItem.setWidth(dp2px(listView, 90));
                        deleteItem.setTitle("delete");
                        deleteItem.setTitleSize(18);
                        deleteItem.setTitleColor(Color.WHITE);
                        menu.addMenuItem(deleteItem);
                    }
                };

                listView.setMenuCreator(creator);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(ReadingActivity.this,android.R.layout.simple_expandable_list_item_1,slabels);
                listView.setAdapter(adapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        int y = load_label_sp.getInt("y" + position, 0);
                        int height = load_label_sp.getInt("height" + position, -1);
                        if (height == -1) {
                            Toast.makeText(getApplicationContext(), "书签不存在", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        sv_content.scrollTo(sv_content.getScrollX(), y);
                        dialog.dismiss();
                        Toast.makeText(getApplicationContext(), "载入书签成功", Toast.LENGTH_SHORT).show();
                    }
                });
                listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(int position, SwipeMenu swipeMenu, int index) {
                        switch (index){
                            case 0:
                                //delete
                                SharedPreferences.Editor load_label_editor = load_label_sp.edit();
                                deleteLabel(load_label_sp,position);
                                rollUp(load_label_sp,position);
                                Toast.makeText(getApplicationContext(),"成功删除书签",Toast.LENGTH_SHORT).show();
                                break;
                        }
                        dialog.dismiss();
                        return false;
                    }
                });

                dialog.setView(view);
                dialog.show();

                break;
            case R.id.item_change_bg:
                final int[] selectImagePosition = {0};
                AlertDialog.Builder change_bg_builder = new AlertDialog.Builder(ReadingActivity.this);
                AlertDialog change_bg_dialog = change_bg_builder.create();

                LayoutInflater change_bg_inflater = getLayoutInflater();
                final View change_bg_view = change_bg_inflater.inflate(R.layout.layout_change_bg, null);

                ArrayList<HashMap<String,Object>> listImageItem = getBgItems();
                SimpleAdapter gridviewAdapter = new SimpleAdapter(ReadingActivity.this,listImageItem,R.layout.bg_change_item,new String[]{"ItemImage","ItemRadio"},new int[]{R.id.changebggridview_ImageItem,R.id.changebggridview_RadioItem});
                final GridView change_bg_gridview = (GridView) change_bg_view.findViewById(R.id.change_bg_gridview);

                change_bg_gridview.setAdapter(gridviewAdapter);
                gridviewAdapter.notifyDataSetChanged();


                change_bg_gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        RadioButton radio = (RadioButton) view.findViewById(R.id.changebggridview_RadioItem);
                        selectImagePosition[0] = position;
                        radio.setChecked(true);
                        for(int i=0;i<2;i++){
                            View view2 = change_bg_gridview.getChildAt(i);
                            radio = (RadioButton) view2.findViewById(R.id.changebggridview_RadioItem);
                            if(i!=position){
                                radio.setChecked(false);
                            }
                        }
                    }
                });


                change_bg_dialog.setTitle("选择一个背景");
                change_bg_dialog.setView(change_bg_view);
                change_bg_dialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        HashMap<String,Object> item = (HashMap<String, Object>) change_bg_gridview.getItemAtPosition(selectImagePosition[0]);

                        if(item == null) {
                            Toast.makeText(ReadingActivity.this, "请选择一个背景", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        ll_content.setBackgroundResource((Integer) item.get("ItemImage"));
                        tv_content.setTextColor((Integer) item.get("TextColor"));

                        String name = (String) item.get("ItemName");
                        Toast.makeText(ReadingActivity.this,name,Toast.LENGTH_SHORT).show();

                    }
                });

                change_bg_dialog.setButton(DialogInterface.BUTTON_NEGATIVE,"取消",(OnClickListener)null);

                change_bg_dialog.show();

                break;
            case R.id.item_quit:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("确定要退出吗？");
                builder.setPositiveButton("确定", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                    }
                });
                builder.setNegativeButton("取消", null);
                builder.show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private ArrayList<HashMap<String,Object>> getBgItems() {
        ArrayList<HashMap<String,Object>> list = new ArrayList<>();
        HashMap<String,Object> map = new HashMap<>();
        map.put("ItemImage", R.drawable.background);
        map.put("ItemName", "木板背景");
        map.put("TextColor", Color.BLACK);
        list.add(map);

        map = new HashMap<>();
        map.put("ItemImage", R.drawable.bg_rock);
        map.put("ItemName","石板背景");
        map.put("TextColor",Color.WHITE);
        list.add(map);

        return list;

    }

    private void rollUp(SharedPreferences sp, int position) {
        int nlabel = sp.getInt("nlabel", -1);
        SharedPreferences.Editor editor = sp.edit();
        if (nlabel == -1){
            return;
        }
        for(int i=position;i<nlabel;i++){
            int y = sp.getInt("y"+(i+1),-1);
            int height = sp.getInt("height" + (i +1),-1);
            String digest = sp.getString("digest"+(i+1),null);

            if(y == -1) return;
            if(height == -1) return;
            if(digest == null) return;

            editor.putInt("y"+i,y);
            editor.remove("y"+(i+1));
            editor.putInt("height"+i,height);
            editor.remove("height"+(i+1));
            editor.putString("digest"+i,digest);
            editor.remove("digest"+(i+1));

            editor.apply();
        }
    }

    private void deleteLabel(SharedPreferences sp, int position) {
        SharedPreferences.Editor editor = sp.edit();
        editor.remove("y" + position);
        editor.remove("height" + position);
        editor.remove("digest" + position);
        int nlabel = sp.getInt("nlabel",-1);
        if(nlabel == -1){
            return;
        }
        editor.putInt("nlabel",--nlabel);
        editor.apply();
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




    private int dp2px(SwipeMenuListView listView ,int dp) {
        return (int) TypedValue.applyDimension(1, (float) dp, listView.getContext().getResources().getDisplayMetrics());
    }

}

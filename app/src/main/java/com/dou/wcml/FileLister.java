package com.dou.wcml;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class FileLister extends AppCompatActivity {

    String WCML_PATH="", WORDS_PATH="", FILE_CLICK = "";

    File WORD_ROOT;

    TextView tv_path;

    ListView lv_files;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_lister);

        setTitle("WordFiles");

        tv_path = (TextView) findViewById(R.id.textView_path);

        lv_files = (ListView)findViewById(R.id.listView_w_files);

        final Intent intent = getIntent();
        final Bundle bundle = intent.getExtras();

        WCML_PATH = bundle.getString("WCML_PATH");
        WORDS_PATH = bundle.getString("WORDS_PATH");

        if (WCML_PATH.isEmpty() || WORDS_PATH.isEmpty()) {
            Toast.makeText(FileLister.this, "intent error", Toast.LENGTH_SHORT).show();
            setTitle("Intent Error, Please Return");
            return;
        }

        tv_path.setText(WORDS_PATH);

        WORD_ROOT = new File(WORDS_PATH);

        final ArrayList<File> word_files_list = new ArrayList<>(Arrays.asList(WORD_ROOT.listFiles()));
        ArrayList<String> word_files_names = new ArrayList<>();

        Iterator<File> the_word_files = word_files_list.iterator();

        while(the_word_files.hasNext()){
            File a_word_file = the_word_files.next();

            String exname = ExName(a_word_file.getName());

            if (exname.equals("mer")){
                the_word_files.remove();
            }else{
                word_files_names.add(a_word_file.getName());
            }

        }

        lv_files.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_expandable_list_item_1,word_files_names));

        lv_files.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                bundle.putString("WORD_FILE_PATH",word_files_list.get(i).getAbsolutePath());

                tv_path.setText(word_files_list.get(i).getAbsolutePath());

                Intent call_lister = new Intent(FileLister.this,Lister.class);
                call_lister.putExtras(bundle);
                startActivity(call_lister);
            }
        });
        
        lv_files.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                final File mer_file = new File(word_files_list.get(i).getPath()+".mer");

                if (!mer_file.exists()){
                    Toast.makeText(FileLister.this, "This txt hasn't memorized", Toast.LENGTH_SHORT).show();
                }else {
                    AlertDialog alertDialog = new AlertDialog.Builder(FileLister.this)
                            .setTitle("Delete memorize")
                            .setMessage("This will delete the memory file")
                            .setPositiveButton("Yes! Delete it", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    if (mer_file.exists()){
                                        mer_file.delete();
                                        Toast.makeText(FileLister.this, "Memory file deleted", Toast.LENGTH_SHORT).show();
                                    }else {
                                        Toast.makeText(FileLister.this, "haven't memorized", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            })
                            .setNegativeButton("No, don't del", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Toast.makeText(FileLister.this, "delete canceled", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .create();
                    alertDialog.show();
                }
                
                return true;
            }
        });

    }

    private String ExName(String name) {
        return name.substring(name.lastIndexOf(".")+1);
    }
}

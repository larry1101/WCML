package com.dou.wcml;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Button btn_c2e_unm,btn_c2e_m;

    TextView textView_dir,tv_help;

    Switch first_rand_word;

    String PATH_ROOT, WCML_PATH, WORDS_PATH, FIRST_RAND_WORD;
    File WCML_ROOT, WORDS_ROOT;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences  = getSharedPreferences("wcmlsettings",MODE_PRIVATE);
        FIRST_RAND_WORD = sharedPreferences.getString("FIRST_RAND_WORD","FALSE");

        first_rand_word = (Switch)findViewById(R.id.switch_1_rand);
        first_rand_word.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                editor = sharedPreferences.edit();
                if (b){
                    FIRST_RAND_WORD = "TRUE";
                    editor.putString("FIRST_RAND_WORD","TRUE");
                }else {
                    FIRST_RAND_WORD = "FALSE";
                    editor.putString("FIRST_RAND_WORD","FALSE");
                }
                editor.commit();
            }
        });
        if (FIRST_RAND_WORD.equals("TRUE")){
            first_rand_word.setChecked(true);
        }

        btn_c2e_unm = (Button)findViewById(R.id.button_ce_unm);
        btn_c2e_unm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle bundle = new Bundle();
                bundle.putString("WORDS_PATH",WORDS_PATH);
                bundle.putString("WCML_PATH",WCML_PATH);

                bundle.putString("CETYPE","C");
                bundle.putString("METYPE","UNM");

                bundle.putString("FIRST_RAND",FIRST_RAND_WORD);

                Intent intent = new Intent(MainActivity.this,FileLister.class);
                intent.putExtras(bundle);

                startActivity(intent);
            }
        });

        btn_c2e_m = (Button)findViewById(R.id.button_ce_m);
        btn_c2e_m.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("WORDS_PATH",WORDS_PATH);
                bundle.putString("WCML_PATH",WCML_PATH);

                bundle.putString("CETYPE","C");
                bundle.putString("METYPE","M");

                bundle.putString("FIRST_RAND",FIRST_RAND_WORD);

                Intent intent = new Intent(MainActivity.this,FileLister.class);
                intent.putExtras(bundle);

                startActivity(intent);
            }
        });

        textView_dir = (TextView)findViewById(R.id.textView_dir);

        tv_help = (TextView)findViewById(R.id.textView_help);
        tv_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "haven't written...please wait...", Toast.LENGTH_SHORT).show();
            }
        });
        tv_help.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("More Help")
                        .setMessage("mail @ liaoderui@126.com\n@liaoderui on Twitter will not get any reply")
                        .setNeutralButton("Back", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(MainActivity.this, R.string.doudou, Toast.LENGTH_SHORT).show();
                            }
                        })
                        .create();
                alertDialog.show();

                return true;
            }
        });

        //SD Check
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            Toast.makeText(MainActivity.this, "No SD Card ......", Toast.LENGTH_SHORT).show();
            btn_c2e_unm.setEnabled(false);
            return;
        }

        PATH_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
        WCML_PATH = PATH_ROOT + "/" + "WCMLS";
        WORDS_PATH = WCML_PATH + "/" + "Words";

        WCML_ROOT = new File(WCML_PATH);
        WORDS_ROOT = new File(WORDS_PATH);

        //Dir Check
        if (!WCML_ROOT.exists()){
            Toast.makeText(MainActivity.this, "Dir !exists", Toast.LENGTH_SHORT).show();

            WCML_ROOT.mkdirs();

            Toast.makeText(MainActivity.this, "Dir created", Toast.LENGTH_SHORT).show();
        }

        if (!WORDS_ROOT.exists()){
            Toast.makeText(MainActivity.this, "WDir !exists", Toast.LENGTH_SHORT).show();

            WORDS_ROOT.mkdirs();

            Toast.makeText(MainActivity.this, "WDir created", Toast.LENGTH_SHORT).show();
        }

        textView_dir.setText("Words @ " + WORDS_ROOT.getAbsolutePath());
    }

}

package com.dou.wcml;

import android.content.Context;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Administrator on 2016-9-6.
 */
public class WordMemorizedHelper {

    //TODO can be more efficient

    /*
    gene. an array to locate the memorized word
    save the array
    1000=10*100
    must know the WCML storage root path
    while read words IO, append the flag to each word
    create a filter to filter the memorized / unmemorized
    */

    Context CONTEXT;

    byte[][] flags;

    int current_line = 0;
    int TOTAL_WORDS,WORDS_PER_ARRAY, FLAG_ROW, FLAG_LINE;

    String WORD_PATH,WORD_FILE_NAME;

    File memorizer;

    byte memorize_type;
    private int a0s;

    //词典路径，词典文件名，单词数，一次读多少单词数量
    public WordMemorizedHelper(String word_path, String word_file_name, int total_words, int words_per_array, String METYPE, Context context){
        WORD_PATH = word_path;
        WORD_FILE_NAME = word_file_name;
        TOTAL_WORDS = total_words;
        WORDS_PER_ARRAY = words_per_array;

        if (METYPE.equals("UNM")){
            memorize_type = 0;
        }else {
            memorize_type = 1;
        }

        CONTEXT = context;

        int rows_count = total_words/words_per_array;
        rows_count ++ ;

        memorizer = new File(WORD_FILE_NAME+".mer");

        flags = new byte[rows_count][words_per_array];

        FLAG_ROW = rows_count;
        FLAG_LINE = words_per_array;

        if (!memorizer.exists()){
            try {
                memorizer.createNewFile();
                Save2File();
            } catch (IOException e) {
                Toast.makeText(CONTEXT, e.toString(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

        try {
            FileInputStream fileInputStream = new FileInputStream(memorizer);

            for (int i = 0; i < rows_count; i++) {
                fileInputStream.read(flags[i]);
            }

            fileInputStream.close();
        } catch (IOException e) {
            Toast.makeText(CONTEXT, e.toString(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    //true - next line; false - previous line
    public void OnCurrentChange(boolean next_line){
        if (next_line){
            current_line++;
        }else {
            current_line--;
        }
    }

    public void Memorized(int index){
        index--;
        int row_at = index/WORDS_PER_ARRAY;
        int col_at = index%WORDS_PER_ARRAY;

        flags[row_at][col_at] = (byte) ((memorize_type+1)%2);
    }

    public void Unmemorized(int index){
        index--;
        int row_at = index/WORDS_PER_ARRAY;
        int col_at = index%WORDS_PER_ARRAY;

        flags[row_at][col_at] = memorize_type;
    }

    public void Save2File(){
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(memorizer,false);

            for (byte[] flag_row : flags) {
                fileOutputStream.write(flag_row);
                fileOutputStream.flush();
            }

            fileOutputStream.close();

        } catch (IOException e) {
            Toast.makeText(CONTEXT, e.toString(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public int get1st0() {
        int index = 0;
        for (byte[] line :
                flags) {
            for (byte flag :
                    line) {
                index++;
                if (flag == memorize_type){
                    return (index);
                }
            }
        }

        return -1;
    }

    public int getPre0(int wordAt) {

        int pre0 = -2, tmp = -1;

        wordAt--;
        int row = wordAt / WORDS_PER_ARRAY;
        int col = wordAt % WORDS_PER_ARRAY;
        for (int i = 0; i <= row+1; i++) {
            for (int j = 0; j < FLAG_LINE; j++) {
                if (flags[i][j] == memorize_type){
                    tmp = i * WORDS_PER_ARRAY;
                    tmp += j;

                    if (tmp >= wordAt){
                        return pre0+1;
                    }else {
                        pre0 = tmp;
                    }
                }
            }
        }

        //TODO ???Seems bugful
        //return -2;

        return pre0+1;
    }

    public int getNext0(int wordAt) {
        int next0 = -2;

        wordAt--;
        int row = wordAt / WORDS_PER_ARRAY;
        int col = wordAt % WORDS_PER_ARRAY;

        for (int i = row; i < FLAG_ROW; i++) {
            //// TODO: 2016-9-6 More efficient
            for (int j = 0; j < FLAG_LINE; j++) {
                if (flags[i][j] == memorize_type){
                    next0 = i * WORDS_PER_ARRAY;
                    next0 += j;

                    if (next0 > wordAt){
                        return next0+1;
                    }
                }
            }
        }

        //TODO ???Seems bugful
        return -2;//


    }

    public int get0s() {
        byte unmz = (byte) ((memorize_type+1)%2);
        int count = 0, count0 = 0;
        for (byte[] line :
                flags) {
            for (byte flag :
                    line) {
                count ++;
                //// TODO: 2016-9-7 seems bugful
                if (count >= TOTAL_WORDS){
                    return count0;
                }
                if (flag == unmz){
                    count0 ++;
                }
            }
        }
        return -1;
    }
}

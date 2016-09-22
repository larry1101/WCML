package com.dou.wcml;

import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Lister extends AppCompatActivity {

	TextView title, tv_index;
	ListView lv;
	Button btn_hinder, btn_pre, btn_next, btn_memorized;
	ProgressBar progressBar_words;
	SeekBar seekBar_word_location;

	ArrayList<String> wordlist = new ArrayList<>();
	List<String> adapterlist;

	String CETYPE = "", METYPE = "", FIRST_RAND = "";
	String WCML_PATH="", WORDS_PATH="", WORD_FILE_PATH = "";
	File WORD_FILE;

	int wordAt, totalWordCount, current_min, current_max;
	final int WORDS_EACH_ARRAY = 50;

	boolean seekbar_visibility = false;

	WordMemorizedHelper helper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lister);

		setTitle("Lister");

		title = (TextView) findViewById(R.id.word);
		tv_index = (TextView) findViewById(R.id.textView_index);

		lv = (ListView) findViewById(R.id.listView);

		btn_hinder = (Button) findViewById(R.id.button_hinder);

		btn_pre = (Button) findViewById(R.id.button_previous_word);
		btn_next = (Button) findViewById(R.id.button_next_word);

		btn_memorized = (Button)findViewById(R.id.button_memorized);

		progressBar_words = (ProgressBar)findViewById(R.id.progressBar_words);

		seekBar_word_location = (SeekBar)findViewById(R.id.seekBar_word_location);

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();

		WCML_PATH = bundle.getString("WCML_PATH");
		WORDS_PATH = bundle.getString("WORDS_PATH");
		WORD_FILE_PATH = bundle.getString("WORD_FILE_PATH");
		CETYPE = bundle.getString("CETYPE");
		METYPE = bundle.getString("METYPE");
		FIRST_RAND = bundle.getString("FIRST_RAND");

		if (METYPE.equals("M")){
			btn_memorized.setText(R.string.unmemo);
		}

		if (WCML_PATH.isEmpty() || WORDS_PATH.isEmpty()) {
			Toast.makeText(Lister.this, "intent error", Toast.LENGTH_SHORT).show();
			btn_next.setEnabled(false);
			btn_pre.setEnabled(false);
			btn_hinder.setEnabled(false);
			btn_hinder.setVisibility(View.INVISIBLE);
			return;
		}

		if (!getCurrentFile(WORD_FILE_PATH)) {
			btn_next.setEnabled(false);
			btn_pre.setEnabled(false);
			btn_hinder.setEnabled(false);
			btn_hinder.setVisibility(View.INVISIBLE);
			return;
		}

		totalWordCount = getTotalWordCount();

		progressBar_words.setMax(totalWordCount-1);
		seekBar_word_location.setMax(totalWordCount-1);

		//TODO: Useless?
		wordAt = 1;
		current_min = 1;
		current_max = WORDS_EACH_ARRAY;

		/*
		//TODO: Not 10
		 */

		helper = new WordMemorizedHelper(WORDS_PATH, WORD_FILE_PATH, totalWordCount, WORDS_EACH_ARRAY, METYPE, this);

		progressBar_words.setProgress(helper.get0s());

		wordAt = helper.get1st0();

		if (wordAt == -1 || wordAt > totalWordCount){
			Toast.makeText(Lister.this, "Congratulations! This txt is finished!", Toast.LENGTH_SHORT).show();
			btn_next.setEnabled(false);
			btn_pre.setEnabled(false);
			btn_hinder.setEnabled(false);
			btn_hinder.setVisibility(View.INVISIBLE);
			title.setText(R.string.congra);
			tv_index.setVisibility(View.INVISIBLE);
			btn_memorized.setEnabled(false);
			return;
		}

		getCurrentWordRange();

		if (wordlist.size() <= 0) {
			Toast.makeText(Lister.this, "No word get :(", Toast.LENGTH_SHORT).show();
			btn_next.setEnabled(false);
			btn_pre.setEnabled(false);
			btn_hinder.setEnabled(false);
			btn_hinder.setVisibility(View.INVISIBLE);
			return;
		}

		btn_hinder.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				btn_hinder.setVisibility(View.INVISIBLE);
			}
		});
        btn_hinder.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
				helper.Unmemorized(wordAt);
                helper.Save2File();
                Toast.makeText(Lister.this, "Saved", Toast.LENGTH_SHORT).show();
                return true;//// TODO: 2016-9-11 t or f
            }
        });

		btn_pre.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				int wordAtTMP = helper.getPre0(wordAt);

				//if (wordAtTMP == -2){
				//    Toast.makeText(Lister.this, "Something Dou don't want to solve happened @pre", Toast.LENGTH_SHORT).show();
				//}

				if (wordAtTMP < 1) {
					Toast.makeText(Lister.this, "This is the first word", Toast.LENGTH_SHORT).show();
					return;
				}
				if (wordAtTMP < current_min){
					wordAt = wordAtTMP;
					getCurrentWordRange();
				}
				//btn_hinder.setVisibility(View.VISIBLE);
				wordAt = wordAtTMP;
				showWordAt(wordAt);
			}
		});

		btn_next.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				int wordAtTMP = helper.getNext0(wordAt);

				if (wordAtTMP == -2){
					Toast.makeText(Lister.this, "This is the last word", Toast.LENGTH_SHORT).show();
					return;
				}

				if (wordAtTMP <= 0 ){
					Toast.makeText(Lister.this, "Something Dou don't want to solve happened @next", Toast.LENGTH_SHORT).show();
				}

				if (wordAtTMP > totalWordCount) {
					Toast.makeText(Lister.this, "This is the last word", Toast.LENGTH_SHORT).show();
					return;
				}
				if (wordAtTMP > current_max) {
					wordAt = wordAtTMP;
					getCurrentWordRange();
				}
				wordAt = wordAtTMP;
				//btn_hinder.setVisibility(View.VISIBLE);
				showWordAt(wordAt);
			}
		});

		btn_pre.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				int wordAtTMP = wordAt - 1;

				//if (wordAtTMP == -2){
				//    Toast.makeText(Lister.this, "Something Dou don't want to solve happened @pre", Toast.LENGTH_SHORT).show();
				//}

				if (wordAtTMP < 1) {
					Toast.makeText(Lister.this, "This is the first word", Toast.LENGTH_SHORT).show();
					return true;
				}
				if (wordAtTMP < current_min){
					wordAt = wordAtTMP;
					getCurrentWordRange();
				}
				//btn_hinder.setVisibility(View.VISIBLE);
				wordAt = wordAtTMP;
				showWordAt(wordAt);
				return true;
			}
		});

		btn_next.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				int wordAtTMP = wordAt + 1;

				if (wordAtTMP > totalWordCount) {
					Toast.makeText(Lister.this, "This is the last word", Toast.LENGTH_SHORT).show();
					return true;
				}
				if (wordAtTMP > current_max) {
					wordAt = wordAtTMP;
					getCurrentWordRange();
				}
				wordAt = wordAtTMP;
				//btn_hinder.setVisibility(View.VISIBLE);
				showWordAt(wordAt);
				return true;
			}
		});

		btn_memorized.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				helper.Memorized(wordAt);
				try {
					progressBar_words.setProgress(helper.get0s());
				}catch (Exception e){
					e.printStackTrace();
					Toast.makeText(Lister.this, e.toString(), Toast.LENGTH_SHORT).show();
				}

			}
		});

		btn_memorized.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {

				Toast.makeText(Lister.this, "Save Memorizer", Toast.LENGTH_SHORT).show();

				helper.Save2File();

				return true;
			}
		});


		title.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showRandWord();
			}
		});
		title.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {

				ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

				//// TODO: 2016-9-7 ?????how?
				clipboardManager.setText(title.getText());

				//// TODO: 2016-9-7 nomdict
				try{
					//lv.setAdapter(new ArrayAdapter<>(Lister.this,android.R.layout.simple_expandable_list_item_1,getAllApps()));
					Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage("cn.mdict");
					startActivity(LaunchIntent);
				}
				catch (Exception e){
					e.printStackTrace();
					Toast.makeText(Lister.this, e.toString(), Toast.LENGTH_SHORT).show();
				}

				return true;
			}
		});

		progressBar_words.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				seekBar_word_location.setProgress(wordAt-1);
				seekbar_visibility = !seekbar_visibility;
				if (seekbar_visibility){
					seekBar_word_location.setVisibility(View.VISIBLE);
				}else {
					seekBar_word_location.setVisibility(View.GONE);
				}

				return true;
			}
		});

		progressBar_words.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				seekBar_word_location.setProgress(wordAt-1);
				seekbar_visibility = !seekbar_visibility;
				if (seekbar_visibility){
					seekBar_word_location.setVisibility(View.VISIBLE);
				}else {
					seekBar_word_location.setVisibility(View.GONE);
				}
			}
		});

		seekBar_word_location.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

				tv_index.setText((seekBar.getProgress()+1)+"");
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

				wordAt = seekBar.getProgress();
				wordAt++;
				getCurrentWordRange();
				showWordAt(wordAt);
				seekbar_visibility = false;
				seekBar_word_location.setVisibility(View.GONE);

			}
		});

		if (FIRST_RAND.equals("TRUE")){
			showRandWord();
		}else {
			showWordAt(wordAt);
		}



	}

    //// TODO: 2016-9-11 long press to exit without save , how to
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {

            //Toast.makeText(Lister.this, "Saving memory file......", Toast.LENGTH_SHORT).show();
            //Toast.makeText(Lister.this, "Memory file saved, exit....", Toast.LENGTH_SHORT).show();

        }else if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 3){
            //Toast.makeText(Lister.this, "Exit without saving memory file.", Toast.LENGTH_SHORT).show();
            helper.Save2File();
            Toast.makeText(Lister.this, "Saved", Toast.LENGTH_SHORT).show();
        }

        return super.onKeyDown(keyCode, event);
    }

    private void showRandWord() {
        wordAt = (int) (((Math.random()*totalWordCount+1)%totalWordCount)+1);
		getCurrentWordRange();
		showWordAt(wordAt);
	}

	/*
	public List<PackageInfo> getAllApps() {
		List<PackageInfo> apps = new ArrayList<PackageInfo>();
		PackageManager packageManager = this.getPackageManager();
		//获取手机内所有应用
		List<PackageInfo> paklist = packageManager.getInstalledPackages(0);
		for (int i = 0; i < paklist.size(); i++) {
			PackageInfo pak = (PackageInfo) paklist.get(i);
			//判断是否为非系统预装的应用  (大于0为系统预装应用，小于等于0为非系统应用)
			if ((pak.applicationInfo.flags & pak.applicationInfo.FLAG_SYSTEM) <= 0) {
				apps.add(pak);
			}
		}
		return apps;
	}
	*/

	private void showWordAt(int index) {

		index --;
		index %= wordlist.size();

		adapterlist = word2ml(wDivider(wordlist.get(index)));

		btn_hinder.setVisibility(View.VISIBLE);

		lv.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_expandable_list_item_1,adapterlist));

	}

	private List<String> word2ml(HashMap<String, Object> aWord) {

		title.setText((String)aWord.get("word"));
		tv_index.setText("" + wordAt );

		List<String> adapterlist = new ArrayList<>();

		ArrayList<String> cx = (ArrayList)aWord.get("cx");
		ArrayList<String> wordMeanings;

		for (String str:cx
				) {
			wordMeanings = (ArrayList)aWord.get(str);
			adapterlist.add(str);
			for (String wmstr:wordMeanings
					) {
				adapterlist.add(wmstr);
			}
		}

		return adapterlist;
	}

	private HashMap<String, Object> wDivider(String word) {
		HashMap<String, Object> aWord = new HashMap<>();
		ArrayList<String> div1 = new ArrayList<>(Arrays.asList(word.split("@")));
		aWord.put("word",div1.get(0));
		div1.remove(0);

		ArrayList<String> div2,cx,wordMeanings;
		cx = new ArrayList<>();
		for (String seg1 :
				div1) {
			div2 = new ArrayList<>(Arrays.asList(seg1.split(",")));

			String thisCX = div2.get(0);

			cx.add(thisCX);
			div2.remove(0);

			wordMeanings = new ArrayList<>();
			for (String seg2 :
					div2) {
				wordMeanings.add(seg2);
			}

			aWord.put(thisCX,wordMeanings);
		}

		aWord.put("cx",cx);

		return aWord;
	}

	/*
	Ctrl+H:

	^t

	n.
	a.
	ad.
	adj.
	adv.
	vt.
	vn.
	prep.
	conj.

	[pl.]

	, <--white space
	，
	；
	;
	 */

	public ArrayList<String> getWords(int from , int to) {

		wordlist.clear();

		try {

			//BufferedReader r = new BufferedReader(new FileReader(WORD_FILE));

			BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(WORD_FILE),"GB2312"));

			String word_line;

			int read_off;

			for (read_off  = 0; read_off < from; read_off++){
				if (r.readLine() == null){
					r.close();
					return wordlist;
				}
			}

			for (; read_off <= to; read_off++) {
				if ((word_line = r.readLine()) != null){
					wordlist.add(word_line);
				}else {
					break;
				}
			}

			r.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Toast.makeText(Lister.this, e.toString(), Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			Toast.makeText(Lister.this, e.toString(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}

		return wordlist;
	}

	public boolean getCurrentFile(String FileName) {
		WORD_FILE = new File(WORD_FILE_PATH);

		if (!WORD_FILE.exists()){
			Toast.makeText(Lister.this, "WordsFile : "+ FileName + "!exist", Toast.LENGTH_SHORT).show();
			return false;
		}

		return true;
	}

	public int getTotalWordCount() {

		String WordCount = "0";

		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(WORD_FILE),"GB2312"));
			WordCount = r.readLine();
		} catch (IOException e) {
			Toast.makeText(Lister.this, e.toString(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}

		return Integer.parseInt(WordCount);
	}

	//bug fixed @2.3.4
	public void getCurrentWordRange() {

		current_min = ((wordAt-1)/WORDS_EACH_ARRAY);
		current_min *= WORDS_EACH_ARRAY;
		current_min ++;
		current_max = current_min + WORDS_EACH_ARRAY - 1;

		wordlist = getWords(current_min,current_max);

	}
}

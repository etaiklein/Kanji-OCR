package edu.dartmouth.cs.drawing;

/**
 * Main Activity
 * 
 * Main class for the Kanji Data Collection app
 * 
 * based on this tutorial from http://code.tutsplus.com/tutorials/android-sdk-create-a-drawing-app-essential-functionality--mobile-19328
 * Etai Klein
 * 6/7/14
 * 
 */


import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;

//colors
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
// functionality
import java.util.*;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener{

	//view
	private DrawingView drawView;

	//functionality
	private float mediumBrush;
	private ImageButton currPaint, newBtn, saveBtn, nextBtn, prevBtn;

	//kanjis
	String[] kanjis = new String[]{"一", "七", "万", "三", "上", "下", "中", "九", "二", "五", "人", "今", "休", "会", "何", "先", "入", "八", "六", "円", "出", "分", "前", "北", "十", "千", "午", "半", "南", "友", "口", "古", "右", "名", "四", "国", "土", "外", "多", "大", "天", "女", "子", "学", "安", "小", "少", "山", "川", "左", "年", "店", "後", "手", "新", "日", "時", "書", "月", "木", "本", "来", "東", "校", "母", "毎", "気", "水", "火", "父", "生", "男", "白", "百", "目", "社", "空", "立", "耳", "聞", "花", "行", "西", "見", "言", "話", "語", "読", "買", "足", "車", "週", "道", "金", "長", "間", "雨", "電", "食", "飲", "駅", "高", "魚"};

	//dictionaries
	HashMap<String, byte[]> byteDict = new HashMap<String, byte[]>();
	HashMap<String, ArrayList<Integer>> strokeDict = new HashMap<String, ArrayList<Integer>>();

	//kanji index
	public int currentKanji = 0;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//view
		drawView = (DrawingView)findViewById(R.id.drawing);
		LinearLayout paintLayout = (LinearLayout)findViewById(R.id.paint_colors);
		currPaint = (ImageButton)paintLayout.getChildAt(0);
		currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));

		//functionality
		mediumBrush = getResources().getInteger(R.integer.medium_size);
		drawView.setBrushSize(mediumBrush);


		newBtn = (ImageButton)findViewById(R.id.new_btn);
		newBtn.setOnClickListener(this);
		saveBtn = (ImageButton)findViewById(R.id.save_btn);
		saveBtn.setOnClickListener(this);
		nextBtn = (ImageButton)findViewById(R.id.next_btn);
		nextBtn.setOnClickListener(this);
		prevBtn = (ImageButton)findViewById(R.id.prev_btn);
		prevBtn.setOnClickListener(this);

	}


	//compress Bitmaps for easier storage
	public byte[] compress(Bitmap bmp){
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
		byte[] byteArray = stream.toByteArray();
		return byteArray;
	}

	//decompress byte array for retrieval 
	public Bitmap decompress(byte[] bytes){
		return BitmapFactory.decodeByteArray(bytes, 0, bytes.length).copy(Bitmap.Config.ARGB_8888, true);
	}

	//view

	@Override
	// Inflate the menu; this adds items to the action bar if it is present.
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}


	// functionality

	//respond to clicks
	public void onClick(View view){
		//draw button clicked
		if(view.getId()==R.id.new_btn){
			AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
			newDialog.setTitle("New drawing");
			newDialog.setMessage("Start new drawing (you will lose the current drawing)?");
			newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which){
					drawView.startNew();
					dialog.dismiss();
				}
			});
			newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which){
					dialog.cancel();
				}
			});
			newDialog.show();		
		}
		//save button clicked
		else if(view.getId()==R.id.save_btn){
			AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
			saveDialog.setTitle("Save drawing");

			saveDialog.setMessage("Send Kanjis to the database?");
			saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){

				public void onClick(DialogInterface dialog, int which){

					byteDict.put(kanjis[currentKanji], compress(drawView.getBitmap().copy(drawView.getBitmap().getConfig(), true)));
					strokeDict.put(kanjis[currentKanji], drawView.strokes);

					Toast.makeText(getApplicationContext(), "Saving...", Toast.LENGTH_LONG).show();

					//save the information from the dictionaries to the phone
					//NOTE: it takes an hour or so for this to show up on the phone 
					File file = null;
					try{
						UUID id = UUID.randomUUID();
						//This will get the SD Card directory and create a folder named Kanjis in it.
						File sdCard = Environment.getExternalStorageDirectory();
						File directory = new File (sdCard.getAbsolutePath() + "/Kanjis");
						directory.mkdirs();

						//Now create the file in the above directory and write the contents into it
						file = new File(directory,"kanjis" + id + ".txt");
						FileOutputStream fOut = new FileOutputStream(file);
						OutputStreamWriter osw = new OutputStreamWriter(fOut);
						osw.write(dictionaryString());
						osw.flush();
						osw.close();
					}catch(IOException e){e.printStackTrace(); 
					Toast.makeText(getApplicationContext(), "Something went wrong....", Toast.LENGTH_LONG).show();
					Log.d("error", "Something went wrong");
					}finally{
						Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_LONG).show();
					}

					try {
						BufferedReader br = new BufferedReader(new FileReader(file));
						Toast.makeText(getApplicationContext(), br.readLine(), Toast.LENGTH_LONG).show();
						br.close();
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}



				}
			});
			saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which){
					dialog.cancel();
				}
			});
			saveDialog.show();


		}
		//move to the next Kanji
		else if(view.getId()==R.id.next_btn){

			ImageButton nextBtn = (ImageButton)findViewById(R.id.next_btn);
			nextBtn.setOnClickListener(new OnClickListener(){
				@Override

				public void onClick(View v) {
					//reminder to save @ the last kanji
					if (currentKanji > kanjis.length -2){
						Toast saveme = Toast.makeText(getApplicationContext(), "Hit the save button to submit!", Toast.LENGTH_SHORT);
						saveme.show();
					}else{
						//save the current stroke/bitmap
						strokeDict.put(kanjis[currentKanji], drawView.strokes);
						byteDict.put(kanjis[currentKanji], compress(drawView.getBitmap().copy(drawView.getBitmap().getConfig(), true)));

						//then get the stroke/bitmap for the new kanji if it exists
						if (byteDict.containsKey(kanjis[currentKanji+1])){
							drawView.setBitmap(decompress(byteDict.get(kanjis[currentKanji+1])));
							drawView.strokes = strokeDict.get(kanjis[currentKanji]);}

					
						//update the kanji (by incrementing the Kanji Index)
						drawView.setBackgroundImage(kanjis[++currentKanji]);

						
					}
				}
			});
		}
		//move to the previous kanji
		else if(view.getId()==R.id.prev_btn){

			ImageButton prevBtn = (ImageButton)findViewById(R.id.prev_btn);
			prevBtn.setOnClickListener(new OnClickListener(){
				@Override

				public void onClick(View v) {
					//store the current bitmap/stroke values 
					strokeDict.put(kanjis[currentKanji], drawView.strokes);
					byteDict.put(kanjis[currentKanji], compress(drawView.getBitmap().copy(drawView.getBitmap().getConfig(), true)));
					//if the index is positive,
					if (currentKanji > 0){
						//reduce it
						--currentKanji;
						//set the bitmap and strokes
						drawView.setBitmap(decompress(byteDict.get(kanjis[currentKanji])));
						drawView.strokes = strokeDict.get(kanjis[currentKanji]);

						//						Log.d("this", kanjis[currentKanji] + " " + decompress(byteDict.get(kanjis[currentKanji])).getPixel(0, 0));
						//set the new image
						drawView.setBackgroundImage(kanjis[currentKanji]);

						
					}
				}
			});
		}
	}

	//takes my dictionaries and writes it to a string for sending
	protected String dictionaryString() {
		String mystring = "";

		Iterator<String> iter = byteDict.keySet().iterator();
		while(iter.hasNext()){
			String temp = iter.next();
			mystring+= temp + " [";

			if (strokeDict.get(temp) != null){
				ArrayList<Integer> intarray = strokeDict.get(temp);
				for (int i : intarray){
					mystring += i + ", ";
				}

				mystring = mystring.substring(0, mystring.length()-3) + "] ";
			}			

			mystring += Arrays.toString(byteDict.get(temp));

			mystring = mystring.substring(0, mystring.length()-3) + "\n";
		}



		return mystring;
	}

}

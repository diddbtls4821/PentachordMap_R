package com.example.usin.pentachordmap;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE PENTAMAP (idx INTEGER PRIMARY KEY AUTOINCREMENT,update_date DEFAULT (datetime('now','localtime'))," +
                                                                                        "phone VARCAHR,latitude VARCHAR(1000), longitude VARCHAR(1000))");
    }

    @Override
         public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insert(String time,String phone,String X,String Y) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO PENTAMAP VALUES(null, '" + time+ "', " + phone+ ", '" + X + "','"+Y+"');");
        db.close();
    }


    public String getResult() {
        SQLiteDatabase db = getReadableDatabase();
        String result = "";

        Cursor cursor = db.rawQuery("SELECT * FROM PENTAMAP", null);
        while(cursor.moveToNext()){
            result += cursor.getString(0)
                    + " : "
                    + cursor.getString(1)
                    + " , "
                    + cursor.getInt(2)
                    + ", "
                    + cursor.getString(3)
                    + ", "
                    + cursor.getString(4)
                    + "\n ";
        }
        return result;
    }

    public void SQL() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM PENTAMAP", null);
        while (cursor.moveToNext()) {

            String time = cursor.getString(1);
            String phone = cursor.getString(2);
            String X = cursor.getString(3);
            String Y= cursor.getString(4);
            INSERTDB(time,phone,X,Y);
            db.delete("PENTAMAP","update_date = ? AND phone = ? AND latitude = ? AND longitude = ?",new String[]{time,phone,X,Y});
        }
    }

    private void INSERTDB(String time, String phone, String x, String y) {
        class InsertDB extends AsyncTask<String, String, String> {
            @Override
            protected String doInBackground(String... params) {
                String link = ("http://192.168.1.209/index_android.php");
                String data;
                String time = params[0];
                String phone = params[1];
                String x = params[2];
                String y = params[3];

                try {
                    URL url = new URL(link); //link = 192.168.1.204
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection(); //HTTP 열기
                    data = URLEncoder.encode("time","UTF-8") + "=" + URLEncoder.encode(time,"UTF-8"); //Xp 데이터를 php로 보냄
                    data += "&" + URLEncoder.encode("phone","UTF-8") + "=" + URLEncoder.encode(phone,"UTF-8"); // Yp데이터를 php로 보냄
                    data += "&" + URLEncoder.encode("x","UTF-8") + "=" + URLEncoder.encode(x,"UTF-8"); // Yp데이터를 php로 보냄
                    data += "&" + URLEncoder.encode("y","UTF-8") + "=" + URLEncoder.encode(y,"UTF-8"); // Yp데이터를 php로 보냄

                    Log.d("XXX",data);
                    Log.d("XXXXXXX", String.valueOf(url));
                    urlConnection.setDoInput(true); //서버에서 Input설정
                    urlConnection.setDoOutput(true); //서버에서 output 설정
                    OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());

                    writer.write(data);
                    writer.flush();
                    writer.close();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(),"UTF-8"));
                    StringBuilder stringBuilder = new StringBuilder(); // 라인단위로 DB 집어넣기
                    String line = null;
                    while((line = reader.readLine()) != null){
                        stringBuilder.append(line);
                        break;
                    }
                    return stringBuilder.toString();
                }catch (Exception e){
                    return new String("Exception: "+ e.getMessage());
                }
            }
        }
        new InsertDB().execute(time,phone,x,y);
    }
}

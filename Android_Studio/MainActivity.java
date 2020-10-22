package com.cookandroid.loginregisterexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.widget.TextView;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static android.provider.ContactsContract.Intents.Insert.NAME;

public class MainActivity extends AppCompatActivity {

    final static private String URL = "http://xoqja.dothome.co.kr/Register.php";
    private Map<String, String> map;
    private BluetoothSPP bt;
    dbHelper helper;
    SQLiteDatabase db;
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("메인 화면");
        bt = new BluetoothSPP(this); //Initializing
        helper=new dbHelper(this);

        db=helper.getWritableDatabase();


        if (!bt.isBluetoothAvailable()) { //블루투스 사용 불가
            Toast.makeText(getApplicationContext()
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }

        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() { //데이터 수신
            EditText e=(EditText)findViewById(R.id.editText);
            EditText e2=(EditText)findViewById(R.id.editText2);
            EditText e3=(EditText)findViewById(R.id.editText4);


            public void onDataReceived(byte[] data, String message) {
                if(message.charAt(0)=='W'){
                    String[] array=message.split(",");
                    Toast.makeText(getApplicationContext(),array[1],Toast.LENGTH_SHORT).show();
                }
                else if(message.charAt(0)=='D'){
                    long now = System.currentTimeMillis();
                    Date date = new Date(now);
                    SimpleDateFormat mFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    String time = mFormat.format(date);

                    String[] array=message.split(",");
                    e.setText(array[1].concat("%"));
                    e2.setText(array[2].concat("C"));
                    e3.setText(array[3].concat("un"));
                    db.execSQL("INSERT INTO contacts VALUES (null, " + "'" + time + "      " + array[1] + "', " + "'"+array[2]+"', " + "'"+array[3]+"');");

                }
                else if(message.charAt(0)=='M'){
                    String[] array=message.split(",");
                    Toast.makeText(getApplicationContext(),array[1],Toast.LENGTH_SHORT).show();

                }
                else{
                    /*LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.customtoast, (ViewGroup) findViewById(R.id.custom_toast_layout));
                    TextView tv = (TextView) layout.findViewById(R.id.txtvw);
                    tv.setText("안내 메세지");
                    Toast toast = new Toast(getApplicationContext());
                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, 100);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.show();*/
                }
            }
        });


        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() { //연결됐을 때
            public void onDeviceConnected(String name, String address) {
                Toast.makeText(getApplicationContext()
                        , "Connected to " + name + "\n" + address
                        , Toast.LENGTH_SHORT).show();
            }

            public void onDeviceDisconnected() { //연결해제
                Toast.makeText(getApplicationContext()
                        , "Connection lost", Toast.LENGTH_SHORT).show();
            }

            public void onDeviceConnectionFailed() { //연결실패
                Toast.makeText(getApplicationContext()
                        , "Unable to connect", Toast.LENGTH_SHORT).show();
            }
        });

        Button btnConnect = findViewById(R.id.btnOn);
        btnConnect.setOnClickListener(new View.OnClickListener() { //연결시도
            public void onClick(View v) {
                if (bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
                    bt.disconnect();
                } else {
                    Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                }
            }
        });

        Button Nextbtn= findViewById(R.id.button);
        Nextbtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), NextActivity.class);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
            }
        });
    }



    public void onDestroy() {
        super.onDestroy();
        bt.stopService(); //블루투스 중지
    }

    public void onStart() {
        super.onStart();
        if (!bt.isBluetoothEnabled()) { //
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if (!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER); //DEVICE_ANDROID는 안드로이드 기기 끼리
                setup();
            }
        }


    }

    public void setup() {
        Button btn1 = findViewById(R.id.button1); //데이터 전송
        Button btn4 = findViewById(R.id.button4);

        btn1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                bt.send("1", true);
            }
        });
        btn4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                bt.send("0", true);
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK)
                bt.connect(data);
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
                setup();
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}

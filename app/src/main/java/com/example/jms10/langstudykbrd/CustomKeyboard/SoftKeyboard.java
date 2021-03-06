package com.example.jms10.langstudykbrd.CustomKeyboard;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.example.jms10.langstudykbrd.DictionaryActivity;
import com.example.jms10.langstudykbrd.Notification.NotificationService;
import com.example.jms10.langstudykbrd.R;
import com.example.jms10.langstudykbrd.SharedPreferenceUtil;
import com.example.jms10.langstudykbrd.WordSelectActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by Sojeong Jin on 30/11/2017.
 */

public class SoftKeyboard extends InputMethodService implements KeyboardView.OnKeyboardActionListener{

    private LinearLayout fullKeyboard;
    private LangKeyboardView mInputView;
    private EditText edit_k;
    private  int prevtextlength = 0;
    private int prevCurrentlength=0;
    private InputMethodManager mInputMethodManager;

    private LangKeyboard QwertyKeyboard;
    private LangKeyboard SymbolKeyboard;
    private LangKeyboard t_SymbolKeyboard;
    private LangKeyboard SymbolShiftKeyboard;
    private LangKeyboard t_SymbolShiftKeyboard;
    private LangKeyboard TranslationKeyboard;

    String tent = "";

    private final int[] ExceptedKeyArray = {};
    /*private final HashSet<Integer> ExceptedKeySet = new HashSet<>() {
        {
            for (int key : ExceptedKeyArray)
                add(key);
        }
    };*/
    private boolean caps = false;
    private boolean trans_flag = false;

    private final int KEYCODE_DICTIONARY = -3;
    private final int KEYCODE_TRANSLATION = -11;
    private final int KEYCODE_LANGUAGE_SWITCH = -101;
    private final int KEYCODE_SYMBOL = -2;
    private final int KEYCODE_CONVERT = -4;
    private final int KEYCODE_COMMIT = -10;

    BroadcastReceiver broadcastRevieceter;


    @Override
    public void onCreate() {
        super.onCreate();
        mInputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        setInputView(onCreateInputView());
        Log.d("hihi", "onStartInputView has been started.");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //unregisterReceiver(broadcastRevieceter);
        Log.d("hihi", "onDestroy has been started.");
    }

    @Override
    public void onInitializeInterface() {
        QwertyKeyboard = new LangKeyboard(this, R.xml.qwerty);
        SymbolKeyboard = new LangKeyboard(this, R.xml.symbols);
        SymbolShiftKeyboard = new LangKeyboard(this, R.xml.symbols_shift);
        TranslationKeyboard = new LangKeyboard(this, R.xml.translation);
        t_SymbolKeyboard = new LangKeyboard(this, R.xml.symbols_t);
        t_SymbolShiftKeyboard = new LangKeyboard(this, R.xml.symbols_shift_t);
        super.onInitializeInterface();
    }

    @Override
    public View onCreateInputView() {
        SharedPreferenceUtil util = new SharedPreferenceUtil(getApplicationContext());
        fullKeyboard = (LinearLayout)getLayoutInflater().inflate(R.layout.input, null);
        QwertyKeyboard.changeKeyHeight(util.getKeyboardHegiht() / (float) 10, true);
        SymbolKeyboard.changeKeyHeight(util.getKeyboardHegiht() / (float) 10, false);
        SymbolShiftKeyboard.changeKeyHeight(util.getKeyboardHegiht() / (float) 10, false);
        TranslationKeyboard.changeKeyHeight(util.getKeyboardHegiht() / (float) 10, true);
        t_SymbolShiftKeyboard.changeKeyHeight(util.getKeyboardHegiht() / (float) 10, false);
        t_SymbolKeyboard.changeKeyHeight(util.getKeyboardHegiht() / (float) 10, false);
        mInputView = (LangKeyboardView)fullKeyboard.findViewById(R.id.keyboard);
        edit_k = (EditText)fullKeyboard.findViewById(R.id.edit_keyboard);
        mInputView.setKeyboard(QwertyKeyboard);
        mInputView.setPreviewEnabled(false);
        mInputView.setOnKeyboardActionListener(this);

        Log.d("TESTEEE","test");
        if (!util.getConvertedWord().equals("NULL@NULL")) {

            getCurrentInputConnection().commitText(util.getConvertedWord(), 1);
            util.setConvertedWord("NULL@NULL");
        }

        Intent intent  = new Intent(this,NotificationService.class);
        startService(intent);
        return fullKeyboard;
    }


    @Override
    public void onKey(int primaryCode, int[] keyCodes) {

        Log.d("Test", "KEYCODE: "+primaryCode);
        SharedPreferenceUtil sharedPreferenceUtil = new SharedPreferenceUtil(getApplicationContext());
        InputConnection ic = edit_k.onCreateInputConnection(new EditorInfo());
        AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        Vibrator v = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        String bt ="";
        if(mInputView.getKeyboard() == TranslationKeyboard) {
            getCurrentInputConnection().setSelection(0, prevCurrentlength);
        }
        if(mInputView.getKeyboard() == t_SymbolShiftKeyboard){
            getCurrentInputConnection().setSelection(0, prevCurrentlength);
        }
        if(mInputView.getKeyboard() == t_SymbolKeyboard){
            getCurrentInputConnection().setSelection(0, prevCurrentlength);
        }
        Keyboard current;

        if(sharedPreferenceUtil.getKeySound())am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
        if(sharedPreferenceUtil.getKeyVibration()){
            v.vibrate(sharedPreferenceUtil.getVibrationLength()*100);

        }
        switch(primaryCode){
            case Keyboard.KEYCODE_DELETE :
                if(edit_k.length() == 0) {
                    ic = getCurrentInputConnection();
                }
                ic.deleteSurroundingText(1, 0);
                if(mInputView.getKeyboard() == TranslationKeyboard) {
                    prevtextlength = edit_k.getText().length();
                    bt = edit_k.getText().toString();
                    if(bt.length() != 0) {
                        handletrasn(bt);
                    }
                    else{
                        getCurrentInputConnection().commitText("", 1);
                    }
                }
                break;
            case Keyboard.KEYCODE_SHIFT:
                current = mInputView.getKeyboard();
                if (current == QwertyKeyboard || current == TranslationKeyboard) {
                    caps = !caps;
                    mInputView.setShifted(caps);
                } else {
                    if (current == SymbolKeyboard) {
                        mInputView.setKeyboard(SymbolShiftKeyboard);
                    } else if (current == SymbolShiftKeyboard) {
                        mInputView.setKeyboard(SymbolKeyboard);
                    } else if (current == t_SymbolKeyboard) {
                        mInputView.setKeyboard(t_SymbolShiftKeyboard);
                    } else {
                        mInputView.setKeyboard(t_SymbolKeyboard);
                    }
                }

                break;

            case KEYCODE_LANGUAGE_SWITCH:
                handleLanguageSwitch();
                break;
            case KEYCODE_DICTIONARY:
                long time = System.currentTimeMillis();
                long prevtime = sharedPreferenceUtil.getPrevTime();


                Log.d("hihi", time + "||||"+ prevtime);
                if(sharedPreferenceUtil.getDicWaitingTime()*1000 + prevtime <= time) {

                    Intent intent = new Intent(this, DictionaryActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    startActivity(intent);

                    sharedPreferenceUtil.setPrevTime(time);
                }
                else{
                    AlertDialog dialog;
                    AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                    dialog = builder.setMessage("아직 사전을 띄울 수 없습니다. 쿨타임이 설정되어 있습니다").create();
                    WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
                    params.token = mInputView.getWindowToken();
                    params.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
                    dialog.getWindow().setAttributes(params);
                    dialog.show();
                }
                                 break;
            case KEYCODE_TRANSLATION:
                trans_flag = !trans_flag;
                if (!trans_flag) {

                    mInputView.setKeyboard(QwertyKeyboard);
                } else {
                    mInputView.setKeyboard(TranslationKeyboard);
                }
                edit_k.getText().clear();

                break;
            case Keyboard.KEYCODE_MODE_CHANGE:
                current = mInputView.getKeyboard();
                if (current == TranslationKeyboard) {
                    mInputView.setKeyboard(t_SymbolKeyboard);
                } else if (current == t_SymbolKeyboard || current == t_SymbolShiftKeyboard) {
                    mInputView.setKeyboard(TranslationKeyboard);
                } else if (current == QwertyKeyboard) {
                    mInputView.setKeyboard(SymbolKeyboard);
                } else if (current == SymbolKeyboard || current == SymbolShiftKeyboard) {
                    mInputView.setKeyboard(QwertyKeyboard);
                }
                break;

            case KEYCODE_CONVERT:


                String word = edit_k.getText().toString();
                Intent conintent = new Intent(this, WordSelectActivity.class);
                conintent.putExtra("WORD", word);
                conintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                conintent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

                conintent.setAction(Intent.ACTION_PICK_ACTIVITY);
                startActivity(conintent);

                sharedPreferenceUtil.setConvertedWord("NULL@NULL");

                break;

            case KEYCODE_COMMIT:
                edit_k.setText("");
                break;

            default:
                char code = (char) primaryCode;
                if (Character.isLetter(code) && caps) {
                    code = Character.toUpperCase(code);
                }

                Keyboard cur_keyboard = mInputView.getKeyboard();
                if (cur_keyboard == SymbolKeyboard ||
                    cur_keyboard == SymbolShiftKeyboard) {
                    getCurrentInputConnection().commitText(String.valueOf(code), 1);
                } else if (cur_keyboard == QwertyKeyboard && String.valueOf(code).equals(" "))
                    getCurrentInputConnection().commitText(" ", 1);
                else
                    ic.commitText(String.valueOf(code), 1);

                if (mInputView.getKeyboard() == TranslationKeyboard) {

                    prevtextlength = edit_k.getText().length();
                    bt = edit_k.getText().toString();
                    handletrasn(bt);
                }
        }
    }

    private void handletrasn(final String contents){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("test", "hihihihihihih");
                String result = "";
                String clientId = "2qfcuefDmoUdMafl8I8W";
                String clientSecret = "OZag0EZWEQ";//이용시 secret 입력.
                try {
                    Log.d("test", "I tried");
                    String text = URLEncoder.encode(contents, "UTF-8");
                    String apiURL = "https://openapi.naver.com/v1/language/translate";
                    URL url = new URL(apiURL);
                    HttpURLConnection con = (HttpURLConnection)url.openConnection();
                    con.setRequestMethod("POST");
                    con.setRequestProperty("X-Naver-Client-Id", clientId);
                    con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
                    // post request
                    String postParams = "source=en&target=ko&text=" + text;
                    con.setDoOutput(true);
                    DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                    wr.writeBytes(postParams);
                    wr.flush();
                    wr.close();
                    int responseCode = con.getResponseCode();
                    BufferedReader br;
                    if(responseCode==200) { // 정상 호출
                        br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    } else {  // 에러 발생
                        br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                    }
                    String inputLine;
                    StringBuffer response = new StringBuffer();
                    while ((inputLine = br.readLine()) != null) {
                        response.append(inputLine);
                    }
                    br.close();
                    String res = response.toString() + "\n";

                    JSONObject resj = new JSONObject(res);
                    res = resj.get("message").toString();
                    resj = new JSONObject(res);
                    res = resj.get("result").toString();
                    resj = new JSONObject(res);
                    res = resj.get("translatedText").toString();
                    Log.d("test", response.toString());
                    tent = res;
                    getCurrentInputConnection().commitText(tent.toString(), 1);
                    getCurrentInputConnection().setSelection(0, tent.length());
                    prevCurrentlength = tent.length();
                    //System.out.println(response.toString());
                } catch (Exception e) {
                    Log.d("test", e.toString());
                    Log.d("test", "failed");
                }
            }
        }).start();
    }

    private IBinder getToken() {
        final Dialog dialog = getWindow();
        if (dialog == null) {
            return null;
        }
        final Window window = dialog.getWindow();
        if (window == null) {
            return null;
        }
        return window.getAttributes().token;
    }
    private void handleLanguageSwitch(){
        //Log.d("Test", String.valueOf(mInputMethodManager.switchToNextInputMethod(getToken(), true)));
        mInputMethodManager.switchToNextInputMethod(getToken(), false);
        //Log.d("Test", String.valueOf(mInputMethodManager.switchToLastInputMethod(getToken())));
    }
    @Override
    public void onPress(int primaryCode) {

    }


    @Override
    public void onText(CharSequence text) {

    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeUp() {

    }

    @Override
    public void swipeDown() {

    }

    @Override
    public void onRelease(int primaryCode) {

    }
}

package com.example.jms10.langstudykbrd.CustomKeyboard;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.inputmethodservice.Keyboard;

/**
 * Created by Sojeong Jin on 30/11/2017.
 */

public class LangKeyboard extends Keyboard {

    private Key mSpaceKey;
    private Key mModeChangeKey;
    private Key mSavedModeChangeKey;

    public LangKeyboard(Context context, int xmlLayoutResId) {
        super(context, xmlLayoutResId);
    }

    public LangKeyboard(Context context, int layoutTemplateResId, CharSequence characters, int columns, int horizontalPadding) {
        super(context, layoutTemplateResId, characters, columns, horizontalPadding);
    }

    @Override
    protected Key createKeyFromXml(Resources res, Row parent, int x, int y, XmlResourceParser parser) {
        Key key = new LangKey(res, parent, x, y, parser);
        if(key.codes[0] == ' '){
            mSpaceKey = key;
        } else if(key.codes[0] == Keyboard.KEYCODE_MODE_CHANGE){
            mModeChangeKey = key;
            mSavedModeChangeKey = new LangKey(res, parent, x, y, parser);
        }
        return key;
    }




    static class LangKey extends Keyboard.Key{

        public LangKey(Resources res, Keyboard.Row parent, int x, int y,
                       XmlResourceParser parser) {
            super(res, parent, x, y, parser);
        }
    }
}

package com.googlecode.gtalksms.panels.wizard;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.googlecode.gtalksms.Log;
import com.googlecode.gtalksms.R;
import com.googlecode.gtalksms.SettingsManager;
import com.googlecode.gtalksms.tools.StringFmt;
import com.googlecode.gtalksms.tools.Tools;

/**
 * Wizard control flow:
 * 
 * Welcome --> Choose Method -->  Choose Server --> Create --> Create Success
 *                            \
 *                             --> Same Account
 *                             |
 *                             --> Existing Account
 *                             
 * Not that "Same Account" and "Existing Account" share the same layout, the
 * only difference is that with "Same Account" the notification address is 
 * set as login and the editText field is made unchangeable
 * 
 * @author Florian Schmaus fschmaus@gmail.com - on behalf of the GTalkSMS Team
 *
 */
public class Wizard extends Activity {
    
    protected final static int VIEW_WELCOME = 0;
    protected final static int VIEW_CHOOSE_METHOD = 1;    
    protected final static int VIEW_CREATE_CHOOSE_SERVER = 2;
    protected final static int VIEW_CREATE = 3;
    protected final static int VIEW_CREATE_SUCCESS = 4;
    protected final static int VIEW_EXISTING_ACCOUNT = 5;
    protected final static int VIEW_SAME_ACCOUNT = 6;
    
    protected final static int METHOD_CREATE_NEW = 1;
    protected final static int METHOD_USE_EXISTING = 2;
    protected final static int METHOD_USE_SAME = 3;
    
    protected final static int CHOOSEN_SERVER_PREDEFINED = 1;
    protected final static int CHOOSEN_SERVER_MANUAL = 2;
        
    // these attributes define the state of the wizzard
    // they should be save restored from savedInstanceState
    protected String mNotifiedAddress;
    protected int mChoosenMethod;
    protected int mChoosenServer;
    protected String mChoosenServername;
    protected String mLogin;
    protected String mPassword1;
    protected String mPassword2;
 
    private int mCurrentView = 0;
    private SettingsManager mSettingsMgr;
    
    public void onSaveInstanceState(Bundle savedBundle) {
        if (mNotifiedAddress != null) 
            savedBundle.putString("mNotifiedAddress", mNotifiedAddress);
        if (mChoosenMethod != 0) 
            savedBundle.putInt("mChoosenMethod", mChoosenMethod);
        if (mChoosenServer != 0)
            savedBundle.putInt("mChoosenServer", mChoosenServer);
        if (mChoosenServername != null)
            savedBundle.putString("mChoosenServername", mChoosenServername);
        if (mLogin != null)
            savedBundle.putString("mLogin", mLogin);
        if (mPassword1 != null)
            savedBundle.putString("mPassword1", mPassword1);
        if (mPassword2 != null)
            savedBundle.putString("mPassword2", mPassword2);
        if (mCurrentView != 0)
            savedBundle.putInt("mCurrentView", mCurrentView);
    }
    
    private void restoreStateFromBundle(Bundle savedBundle) {
        String nA = savedBundle.getString("mNotifiedAddress");
        int cM = savedBundle.getInt("mChoosenMethod");
        int cS = savedBundle.getInt("mChoosenServer");
        String cSN = savedBundle.getString("mChoosenServername");
        String l = savedBundle.getString("mLogin");
        String psw1 = savedBundle.getString("mPassword1");
        String psw2 = savedBundle.getString("mPassword2");
        int cV = savedBundle.getInt("mCurrentView");
        
        if (nA != null)
            mNotifiedAddress = nA;
        if (cM != 0)
            mChoosenMethod = cM;
        if (cS != 0)
            mChoosenServer = cS;
        if (cSN != null)
            mChoosenServername = cSN;
        if (l != null)
            mLogin = l;
        if (psw1 != null)
            mPassword1 = psw1;
        if (psw2 != null)
            mPassword2 = psw2;
        if (cV != 0) 
            mCurrentView = cV;
    }
    
    /** 
     * Called when the activity is first created. 
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            restoreStateFromBundle(savedInstanceState);
        }
        mSettingsMgr = SettingsManager.getSettingsManager(this);
        
        Log.initialize(mSettingsMgr);
        initView(mCurrentView);
    }
    
    class WizardButtonListener implements View.OnClickListener {
        private int mView;
        
        public WizardButtonListener(int view) {
            mView = view;
        }
        
        @Override
        public void onClick(View arg0) {
            initView(mView);
        }
    }
    
    private void mapWizardButton(int id, int view) {
        Button button = (Button) findViewById(id);
        if (button != null) {
            button.setOnClickListener(new WizardButtonListener(view));   
        } else {
            Log.w("Failed to initialize Wizard button mapping, id=" + id + ", view=" + view);
        }
    }
    
    protected void initView(int viewId) {
        
        Button next;
        RadioGroup rg;
        switch (viewId) {
            case VIEW_WELCOME:
                setContentView(R.layout.wizard_welcome);
                next = (Button) findViewById(R.id.nextBut);
                EditText textNotiAddress = (EditText) findViewById(R.id.notificationAddress);
                if (mNotifiedAddress != null) {
                    textNotiAddress.setText(mNotifiedAddress);
                }
                next.setOnClickListener(new WelcomeNextButtonClickListener(this, textNotiAddress));
                break;
            case VIEW_CHOOSE_METHOD:
                setContentView(R.layout.wizard_choose_method);
                mapWizardButton(R.id.backBut, VIEW_WELCOME);
                next = (Button) findViewById(R.id.nextBut);
                rg = (RadioGroup) findViewById(R.id.radioGroupMethod);
                switch (mChoosenMethod) {
                    case METHOD_CREATE_NEW:
                       ((RadioButton) findViewById(R.id.radioDifferentAccount)).setChecked(true);
                       break;
                    case METHOD_USE_EXISTING:
                        ((RadioButton) findViewById(R.id.radioExsistingAccount)).setChecked(true);
                        break;
                    case METHOD_USE_SAME:
                        ((RadioButton) findViewById(R.id.radioSameAccount)).setChecked(true);
                        break;
                }
                next.setOnClickListener(new ChooseMethodNextButtonClickListener(this, rg));
                break;
            case VIEW_CREATE_CHOOSE_SERVER:
                setContentView(R.layout.wizard_create_choose_server);
                Spinner spinner = (Spinner) findViewById(R.id.serverChooser);
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.predefined_xmpp_servers, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
                EditText textServer = (EditText) findViewById(R.id.textServer);
                rg = (RadioGroup) findViewById(R.id.radioGroupServer);
                rg.setOnCheckedChangeListener(new ChooseServerRadioGroupChangeListener(spinner, textServer));
                mapWizardButton(R.id.backBut, VIEW_CHOOSE_METHOD);
                mapWizardButton(R.id.nextBut, VIEW_CREATE);
                break;
            case VIEW_CREATE:
                setContentView(R.layout.wizard_create);
                mapWizardButton(R.id.backBut, VIEW_CREATE_CHOOSE_SERVER);
                Button create = (Button) findViewById(R.id.createBut);
                create.setOnClickListener(new CreateButtonClickListener(this, mSettingsMgr));
                break;
            case VIEW_EXISTING_ACCOUNT:
                break;
            case VIEW_SAME_ACCOUNT:
                setContentView(R.layout.wizard_existing_account);
                String login = ((EditText)findViewById(R.id.notificationAddress)).getText().toString();
                EditText loginText = (EditText) findViewById(R.id.login);
                loginText.setEnabled(false);
                loginText.setText(login);
                mapWizardButton(R.id.backBut, VIEW_CHOOSE_METHOD);
                // TODO map next button
            default:
                throw new IllegalStateException();
        }
        
        TextView label = (TextView) findViewById(R.id.VersionLabel);
        label.setText(StringFmt.Style(Tools.APP_NAME + " " + Tools.getVersionName(getBaseContext()), Typeface.BOLD));

        mCurrentView = viewId;
    }

    /** Called when the activity is first created. */
    @Override
    public void onDestroy() {
        super.onDestroy();
    }    
}
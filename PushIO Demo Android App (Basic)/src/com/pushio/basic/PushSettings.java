package com.pushio.basic;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.pushio.manager.PushIOManager;

import java.util.ArrayList;
import java.util.List;

public class PushSettings extends Activity {
    private static String PUSH_CATEGORY_ENABLE_PUSH = "enable_push";
    private static String PUSH_CATEGORY_US = "US";
    private static String PUSH_CATEGORY_SPORTS = "Sports";

    private static String PUSH_KEY_ALERT = "alert";

	private PushIOManager mPushIOManager;
	private BroadcastReceiver mBroadcastReceiver;
    private List<View> mSettingViewList;
    private TextView mPushText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_push_settings);

        mPushText = (TextView)findViewById(R.id.pushText);

        //Checking to see if this activity was created by a user engaging with a notification
        if (getIntent().hasExtra(PUSH_KEY_ALERT)) {
            //Since they did engage with a notification we can grab anything from the payload, for this simple case, let's display the text from the last alert.
    mPushText.setText("Last Push: " + getIntent().getStringExtra(PUSH_KEY_ALERT));
        }

        mSettingViewList = new ArrayList<View>();
        //Grab or great the instance of the Push IO Manager
        mPushIOManager = PushIOManager.getInstance(this);
        //ensure that any registration changes with Google get reflected with Push IO
		mPushIOManager.ensureRegistration();

        View pushEnabledSetting = findViewById(R.id.push_setting_enabled);
        View usSetting = findViewById(R.id.push_setting_us);
        View sportsSetting = findViewById(R.id.push_setting_sports);

        setupSettingsView(pushEnabledSetting, PUSH_CATEGORY_ENABLE_PUSH, "Enable Push?", "This will enable push notifications");
        setupSettingsView(usSetting, PUSH_CATEGORY_US, "US News", "Registers the category for US News");
        setupSettingsView(sportsSetting, PUSH_CATEGORY_SPORTS, "Sports", "Registers the category for Sports");
	}



	@Override
	public void onResume() {
		super.onResume();

        //If the user has the app open we don't want to add a notification, instead we will handle the push in-app.
		mBroadcastReceiver = new BroadcastReceiver() {
			@Override
			/**
			 * The push intent payload has the main "alert" in the Extra Bundle. If you have set any additional
			 * json keys you can also access them.
			 * 
			 */
			public void onReceive(Context context, Intent intent) {
                //Creating a simple toast to show the user the alert message
				String alert = intent.getStringExtra(PUSH_KEY_ALERT);
				Toast.makeText(context, alert, Toast.LENGTH_LONG).show();

                //We can also take this opportunity to update any parts of the view with parts of the push payload...
				mPushText.setText("Last Push: " + intent.getStringExtra(PUSH_KEY_ALERT));

                //Or you could kick of another update from here...


                //These lines tell the push io manager NOT to create a notification for you, because you are handing it in-app.
                //This will also track an in-app engagement.
				Bundle extras = getResultExtras(true);
				extras.putInt(PushIOManager.PUSH_STATUS, PushIOManager.PUSH_HANDLED_IN_APP);
				setResultExtras(extras);
				this.abortBroadcast();
			}

		};

        //This registers the receiver with android, for the app namespace plus the keywork "PUSHIOPUSH", in your app replace the com.pushio.basic with your app namespace
		registerReceiver(mBroadcastReceiver, new IntentFilter("com.pushio.basic.PUSHIOPUSH"));
	}


    private void setupSettingsView(View view, final String category, String settingTitle, String settingDetail) {
        TextView title = (TextView)view.findViewById(R.id.title_view);
        title.setText(settingTitle);

        TextView detailView = (TextView)view.findViewById(R.id.detail_view);
        detailView.setText(settingDetail);

        final CheckBox checkBox = (CheckBox)view.findViewById(R.id.check_box);

        if (category.equals("enable_push")) {
            checkBox.setChecked(mPushIOManager.getIsBroadcastRegistered());

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (checkBox.isChecked()) {
                        //Register a null category to just for broadcasts,
                        // if you have any default categories this would be a good place to register them
                        mPushIOManager.registerCategory(null);
                        for (View settingView : mSettingViewList) {
                            settingView.setVisibility(View.VISIBLE);
                            ((CheckBox)settingView.findViewById(R.id.check_box)).setChecked(false);
                        }

                    } else {
                        for (View settingView : mSettingViewList) {
                            settingView.setVisibility(View.GONE);
                        }
                        mPushIOManager.unregisterDevice();
                    }
                }
            });

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkBox.setChecked(!checkBox.isChecked());
                }
            });
        } else {
            //Ask the push io manager if the category is registered or not
            mSettingViewList.add(view);
            checkBox.setChecked(mPushIOManager.getRegisteredCategories().contains(category));

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (checkBox.isChecked()) {
                                mPushIOManager.registerCategory(category);
                            } else {
                                mPushIOManager.unregisterCategory(category);
                            }
                        }
                    });

                    checkBox.setChecked(!checkBox.isChecked());
                }
            });
        }



    }

	@Override
	public void onPause(){
		super.onPause();
        //unregister the broadcast receiver when the user leaves the activity so the push io manager creates notifications.
		unregisterReceiver(mBroadcastReceiver);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_push_settings, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent mIntent = new Intent(this, AboutThisApp.class);
		startActivity(mIntent);
		return true;
	}

	@Override
	protected void onStop(){
		super.onStop();
		mPushIOManager.resetEID();
	}
}

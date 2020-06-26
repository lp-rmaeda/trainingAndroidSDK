package com.liveperson.sample.app;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.auth0.android.Auth0;
import com.auth0.android.Auth0Exception;
import com.auth0.android.provider.VoidCallback;
import com.auth0.android.provider.WebAuthProvider;
import com.liveperson.infra.InitLivePersonProperties;
import com.liveperson.infra.MonitoringInitParams;
import com.liveperson.infra.callbacks.InitLivePersonCallBack;
import com.liveperson.messaging.sdk.api.LivePerson;
import com.liveperson.messaging.sdk.api.callbacks.LogoutLivePersonCallback;
import com.liveperson.sample.app.utils.SampleAppStorage;
import com.liveperson.sample.app.notification.NotificationUI;

public class IntroActivity extends AppCompatActivity {

	EditText mAccountIdEditText;
	EditText mAppinstallidEditText;

	private Auth0 auth0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_intro);

		auth0 = new Auth0(this);
		auth0.setOIDCConformant(true);

		mAccountIdEditText = findViewById(R.id.account_id_edit_text);
		mAppinstallidEditText = findViewById(R.id.appinstallid_edit_text);
		Button messagingButton = findViewById(R.id.messaging_button);
		Button monitoringButton = findViewById(R.id.monitoring_button);
		Button logoutButton = findViewById(R.id.logout_button);

		// mAccountIdEditText.setText(SampleAppStorage.getInstance(this).getAccount());
		mAccountIdEditText.setText("87301551");
		// mAppinstallidEditText.setText(SampleAppStorage.getInstance(this).getAppInstallId());
		mAppinstallidEditText.setText("b1026930-2fa4-4516-a5b7-e0c8df3edf8d");

		// Messaging
		messagingButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				MonitoringInitParams monitoringInitParams = null;

				if (!isAccountIdValid()) {
					Toast.makeText(IntroActivity.this, "Account ID not valid", Toast.LENGTH_SHORT).show();
					return;
				}

				// Initialize Monitoring only if AppInstallId was set
				if(isAppInstallIdValid()) {
					monitoringInitParams = new MonitoringInitParams(mAppinstallidEditText.getText().toString());
				}

				storeParams();

				LivePerson.initialize(getApplicationContext(), new InitLivePersonProperties(mAccountIdEditText.getText().toString(), SampleAppStorage.SDK_SAMPLE_FCM_APP_ID, monitoringInitParams, new InitLivePersonCallBack() {

					@Override
					public void onInitSucceed() {
						enableLogoutButton(true);

						Intent messagingIntent = new Intent(IntroActivity.this, MessagingActivity.class);
						startActivity(messagingIntent);
					}

					@Override
					public void onInitFailed(Exception e) {
						Toast.makeText(IntroActivity.this, "Init failed", Toast.LENGTH_SHORT).show();
					}
				}));

			}
		});

		// Monitoring
		monitoringButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				MonitoringInitParams monitoringInitParams = null;

				if (!isAccountIdValid()) {
					Toast.makeText(IntroActivity.this, "Account ID not valid", Toast.LENGTH_SHORT).show();
					return;
				}

				if (!isAppInstallIdValid()) {
					Toast.makeText(IntroActivity.this, "AppInstallID not valid", Toast.LENGTH_SHORT).show();
					return;
				}

				storeParams();

				monitoringInitParams = new MonitoringInitParams(mAppinstallidEditText.getText().toString());

				LivePerson.initialize(getApplicationContext(), new InitLivePersonProperties(mAccountIdEditText.getText().toString(), SampleAppStorage.SDK_SAMPLE_FCM_APP_ID, monitoringInitParams, new InitLivePersonCallBack() {

					@Override
					public void onInitSucceed() {
						enableLogoutButton(true);

						Intent monitoringIntent = new Intent(IntroActivity.this, MonitoringActivity.class);
						startActivity(monitoringIntent);
					}

					@Override
					public void onInitFailed(Exception e) {
						Toast.makeText(IntroActivity.this, "Init failed", Toast.LENGTH_SHORT).show();
					}
				}));

			}
		});

		logoutButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if (auth0.isLoggingEnabled()) {
					WebAuthProvider.logout(auth0)
							.withScheme("demo")
							.start(IntroActivity.this, new VoidCallback() {
								@Override
								public void onSuccess(Void payload) {

								}

								@Override
								public void onFailure(Auth0Exception error) {

								}
							});
				}

				LivePerson.logOut(getApplicationContext(), SampleAppStorage.getInstance(IntroActivity.this).getAccount(), SampleAppStorage.SDK_SAMPLE_FCM_APP_ID,
						new LogoutLivePersonCallback() {
							@Override
							public void onLogoutSucceed() {
								enableLogoutButton(false);

								SampleAppStorage.getInstance(IntroActivity.this).setAuthCode("");

								// Reset badge
								NotificationUI.setBadge(IntroActivity.this, 0);
								// Remove the notification (if any)
								NotificationUI.hideNotification(IntroActivity.this);

								Toast.makeText(IntroActivity.this, "Logout success", Toast.LENGTH_SHORT).show();
							}

							@Override
							public void onLogoutFailed() {
								mAccountIdEditText.setText(R.string.logout_failed);
							}
						});

			}
		});
	}

	private void enableLogoutButton(final boolean enable) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				findViewById(R.id.logout_button).setEnabled(enable);

			}
		});
	}

	private boolean isAccountIdValid() {
		return !TextUtils.isEmpty(mAccountIdEditText.getText());
	}

	private boolean isAppInstallIdValid() {
		return !TextUtils.isEmpty(mAppinstallidEditText.getText());
	}

	private void storeParams(){
		SampleAppStorage.getInstance(IntroActivity.this).setAccount(mAccountIdEditText.getText().toString());
		SampleAppStorage.getInstance(IntroActivity.this).setAppInstallId(mAppinstallidEditText.getText().toString());
	}
}

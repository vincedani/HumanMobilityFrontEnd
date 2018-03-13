package hu.daniel.vince.humanmobility.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hu.daniel.vince.humanmobility.R;
import hu.daniel.vince.humanmobility.model.entities.ApplicationUser;
import hu.daniel.vince.humanmobility.model.entities.ApplicationUserViewModel;
import hu.daniel.vince.humanmobility.model.entities.RegistrationViewModel;
import hu.daniel.vince.humanmobility.model.handlers.connection.AccountConnectionManager;
import hu.daniel.vince.humanmobility.model.handlers.connection.ConnectionHandler;
import hu.daniel.vince.humanmobility.model.handlers.database.DatabaseHandler;
import hu.daniel.vince.humanmobility.view.dialog.DialogBuilder;
import okhttp3.Response;

public class LoginActivity extends BaseCompactActivity {

    // region Members

    private static final int PASSWORD_MIN_LENGTH = 6;

    private InputMethodManager inputMethodManager;

    private Button registerButton;
    private Button loginButton;
    private TextView termsTextView;

    // endregion

    // region Overrides

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        requestPermissions(
            new BaseCompactActivity.OnPermissionRequestListener(){

                @Override
                public void onGranted() {

                    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                    setSupportActionBar(toolbar);

                    ApplicationUser user = DatabaseHandler.getInstance(LoginActivity.this)
                            .getUser();

                    if(user != null && user.getIsLoggedIn()) {
                        nextMainActivity();
                        return;
                    }

                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                            .permitAll()
                            .build();
                    StrictMode.setThreadPolicy(policy);

                    inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    initViews();
                    initOnClicks();
                }

                @Override
                public void onDenied() {
                    finish();
                }
        });

    }

    // endregion

    // region Helpers

    private void initViews() {
        registerButton = (Button) findViewById(R.id.register_button);
        loginButton = (Button) findViewById(R.id.login_button);
        termsTextView = (TextView) findViewById(R.id.terms_text);
    }

    private void initOnClicks() {
        registerButton.setOnClickListener(v -> createAuthenticationDialog(0).show());

        loginButton.setOnClickListener(v -> createAuthenticationDialog(1).show());

        termsTextView.setOnClickListener(v -> {
            DialogBuilder.createWebViewDialog(
                    LoginActivity.this,
                    getString(R.string.navigation_terms),
                    ConnectionHandler.getTermsUrl(),
                    false).show();
        });

    }

    public AlertDialog createAuthenticationDialog(int type) {
        final ScrollView scrollView =
                (ScrollView) View.inflate(LoginActivity.this, R.layout.dialog_authentication, null);

        final EditText usernameET = (EditText) scrollView.findViewById(R.id.email_input);
        final EditText passwordET = (EditText) scrollView.findViewById(R.id.password_input);
        final EditText password2ET = (EditText) scrollView.findViewById(R.id.password_2_input);

        final AlertDialog loadingDialog = DialogBuilder.createLoadingDialog(LoginActivity.this);

        // Login
        if (type == 1) {
            final LinearLayout termsAcceptLayout =
                    (LinearLayout) scrollView.findViewById(R.id.terms_accept_layout);
            termsAcceptLayout.setVisibility(View.GONE);
            password2ET.setVisibility(View.GONE);
        }

        final AlertDialog.Builder builder =
                new AlertDialog.Builder(LoginActivity.this, R.style.AlertDialogStyle);

        builder.setTitle(type == 1 ? R.string.authentication_login : R.string.authentication_register);
        builder.setView(scrollView);
        builder.setNegativeButton(R.string.application_cancel, (dialog, which) -> dialog.dismiss());

        builder.setPositiveButton(R.string.application_ok, null);

        final AlertDialog dialog = builder.create();

        dialog.setOnShowListener(d -> {
            Button b = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            b.setOnClickListener(view -> {
                hideSoftInput(usernameET, passwordET, password2ET);

                if (!isValidUserName(usernameET))
                    return;

                if (!isValidPasswords(passwordET, password2ET, type))
                    return;

                dialog.dismiss();
                loadingDialog.show();

                AccountConnectionManager connectionManager =
                        ConnectionHandler.getInstance(this).getAccountManager();

                ApplicationUserViewModel userViewModel = new ApplicationUserViewModel(
                        usernameET.getText().toString(),
                        passwordET.getText().toString());

                // Login
                if(type == 1) {
                    login(userViewModel, loadingDialog, connectionManager);

                // RegistrationViewModel
                } else {
                    RegistrationViewModel registrationViewModel =
                            RegistrationViewModel.create(userViewModel);

                    connectionManager.register(registrationViewModel,
                            new ConnectionHandler.ConnectionCallback() {
                        @Override
                        public void onFailure(String error) {
                            runOnUiThread(() -> {
                                String text =
                                        getString(R.string.authentication_error_register_failed)
                                        .concat(" ")
                                        .concat(error);

                                loadingDialog.dismiss();
                                DialogBuilder.createErrorDialog(
                                        LoginActivity.this,
                                        text,
                                        (dialog1, which) -> createAuthenticationDialog(0).show())
                                        .show();
                            });
                        }

                        @Override
                        public void onSuccess(Response response) {
                            login(userViewModel, loadingDialog, connectionManager);
                        }
                    });
                }
            });
        });

        return dialog;
    }


    private boolean isValidUserName(EditText userNameET) {
        boolean result = !TextUtils.isEmpty(userNameET.getText().toString()) &&
                userNameET.getText().toString().length() < 30 &&
                userNameET.getText().toString().length() > 6;

        if (result) {
            userNameET.setBackgroundResource(R.drawable.selector_edittext_bg);

        } else {
            userNameET.setBackgroundResource(R.drawable.selector_edittext_error_bg);
            Toast.makeText(
                    LoginActivity.this,
                    R.string.authentication_error_empty_email,
                    Toast.LENGTH_SHORT).show();
        }

        return result;
    }

    private boolean isValidPasswords(EditText password1EditText,
                                     EditText password2EditText,
                                     int type) {
        // Passwords must have at least one non letter or digit character.
        // Passwords must have at least one digit ('0'-'9').
        // Passwords must have at least one uppercase ('A'-'Z').
        // Passwords must have at least one lowercase (a-z).
        boolean result;
        String message = "";

        if (type == 1) {
            result = !password1EditText.getText().toString().isEmpty();
            message = getString(R.string.authentication_error_empty_password);

        } else {
            Pattern lowercase = Pattern.compile("([a-z])");
            Pattern uppercase = Pattern.compile("([A-Z])");
            Pattern number = Pattern.compile("([0-9])");
            Pattern nonAlphanumeric = Pattern.compile("([^a-zA-Z0-9])");

            String password = password1EditText.getText().toString();

            result = password.length() >= PASSWORD_MIN_LENGTH;

            if(!result)
                message = getString(R.string.authentication_error_short_password);


            if(result) {
                boolean isTwoPasswordsSame = password1EditText.getText().toString().
                        equals(password2EditText.getText().toString());

                if(!isTwoPasswordsSame)
                    message = message.concat(getString(
                            R.string.authentication_error_not_match_password)).concat(" ");

                boolean hasUppercaseCharacter = false;
                boolean hasLowercaseCharacter = false;
                boolean hasNumber = false;
                boolean hasNonAlphanumericCharacter = false;

                if(isTwoPasswordsSame) {
                    message = message.concat(getString(
                            R.string.authentication_error_contains_password));

                    Matcher matcher = uppercase.matcher(password);
                    hasUppercaseCharacter = matcher.find();

                    if(!hasUppercaseCharacter)
                        message = message.concat(getString(
                                R.string.authentication_error_uppercase_password)).concat(" ");

                    matcher = number.matcher(password);
                    hasNumber = matcher.find();

                    if(!hasNumber)
                        message = message.concat(getString(
                                R.string.authentication_error_digit_password)).concat(" ");

                    matcher = nonAlphanumeric.matcher(password);
                    hasNonAlphanumericCharacter = matcher.find();

                    if(!hasNonAlphanumericCharacter)
                        message = message.concat(getString(
                            R.string.authentication_error_non_letter_password)).concat(" ");

                    matcher = lowercase.matcher(password);
                    hasLowercaseCharacter = matcher.find();

                    if(!hasLowercaseCharacter)
                        message = message.concat(getString(
                                R.string.authentication_error_lowercase_password)).concat(" ");


                }
                result = hasUppercaseCharacter && hasNumber && hasLowercaseCharacter
                        && hasNonAlphanumericCharacter && isTwoPasswordsSame;
            }

        }
        if (result) {
            password1EditText.setBackgroundResource(R.drawable.selector_edittext_bg);
            password2EditText.setBackgroundResource(R.drawable.selector_edittext_bg);

        } else {
            password1EditText.setBackgroundResource(R.drawable.selector_edittext_error_bg);
            password2EditText.setBackgroundResource(R.drawable.selector_edittext_error_bg);
            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();

        }
        return result;
    }

    private void hideSoftInput(EditText... editTexts) {
        if (editTexts != null && editTexts.length > 0) {
            for (EditText editText : editTexts) {
                inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            }
        }
    }

    private void nextMainActivity() {
        final Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void login(ApplicationUserViewModel user,
                       AlertDialog loadingDialog,
                       AccountConnectionManager connectionManager) {
       connectionManager.authenticateUserAsync(user, new ConnectionHandler.ConnectionCallback() {
            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    String text = getString(R.string.authentication_error_login_failed)
                            .concat(" ")
                            .concat(error);

                    loadingDialog.dismiss();
                    DialogBuilder.createErrorDialog(
                            LoginActivity.this,
                            text,
                            (dialog1, which) -> createAuthenticationDialog(1).show())
                            .show();
                });
            }

            @Override
            public void onSuccess(Response response) {
                try {
                    // User has logged out by application's logic.
                    ApplicationUser dbUser = DatabaseHandler
                                        .getInstance(LoginActivity.this).getUser();
                    if(dbUser != null && !dbUser.getUserName().equals(user.getUserName())) {
                        // Another user is trying to use the app, delete the existing data.
                        DatabaseHandler.getInstance(LoginActivity.this).cleanDatabase();
                    }

                    JSONObject responseObj = new JSONObject(response.body().string());
                    String token = responseObj.getString("access_token");

                    ApplicationUser entity = new ApplicationUser(user.getUserName(), token);
                    entity.setIsLoggedIn(true);
                    DatabaseHandler.getInstance(getApplicationContext()).addOrUpdateUser(entity);

                    runOnUiThread(() -> {
                        loadingDialog.dismiss();
                        requestPermissions(
                                new BaseCompactActivity.OnPermissionRequestListener(){

                                    @Override
                                    public void onGranted() {
                                        nextMainActivity();
                                    }

                                    @Override
                                    public void onDenied() {
                                        showPermissionDeniedToast();
                                        DatabaseHandler.getInstance(getApplicationContext())
                                                .cleanDatabase();
                                    }
                        });
                    });

                } catch (Exception e) {
                    // Ha ide kerül a végrehajtás, akkor nagy gáz van.
                }

            }
        });
    }

    private void showPermissionDeniedToast() {
        Toast.makeText(this, R.string.permission_error, Toast.LENGTH_SHORT).show();
    }

    // endregion
}

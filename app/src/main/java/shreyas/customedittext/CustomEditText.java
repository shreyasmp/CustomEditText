package shreyas.customedittext;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by SM64279 on 6/20/2017.
 */

public class CustomEditText extends LinearLayout {

    private boolean enableClear = true;
    private LayoutInflater layoutInflater = null;
    private TextView redMargin;
    private EditText inputBox;
    private TextView errorMessageView;
    private String errorMessage;
    private String inputValidationCheck;
    private int maxLength;

    private Drawable clearButton = getResources().getDrawable(R.drawable.clear_button);

    public CustomEditText(Context context) {
        super(context);
    }

    public CustomEditText(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public CustomEditText(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    public EditText getInputBox() {
        return inputBox;
    }

    public void setInputBox(EditText inputBox) {
        this.inputBox = inputBox;
    }

    public TextView getErrorMessageView() {
        return errorMessageView;
    }

    public void setErrorMessageView(TextView errorMessageView) {
        this.errorMessageView = errorMessageView;
    }

    public void initView(Context context, AttributeSet attrs) {

        layoutInflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.custom_view, this, true);

        redMargin = (TextView)findViewById(R.id.error_red_line);
        errorMessageView = (TextView)findViewById(R.id.error_message);
        inputBox = (EditText)findViewById(R.id.editText);

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CustomEditText, 0, 0);

        try {
            enableClear = typedArray.getBoolean(R.styleable.CustomEditText_hideClearButton, true);
            errorMessage = typedArray.getString(R.styleable.CustomEditText_setErrorMessage);
            inputValidationCheck = typedArray.getString(R.styleable.CustomEditText_inputValidationCheck).toLowerCase();
            maxLength = typedArray.getInt(R.styleable.CustomEditText_setMaxLength, 254);

        } finally {
            typedArray.recycle();
        }


        if(enableClear == true) {
            inputBox.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        } else {
            clearButton.setBounds(0, 0, clearButton.getIntrinsicWidth(), clearButton.getIntrinsicHeight());

            handleClearButton();

            inputBox.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {

                    if (inputBox.getCompoundDrawables()[2] == null)
                        return false;

                    if (motionEvent.getAction() != MotionEvent.ACTION_UP)
                        return false;

                    if (motionEvent.getX() > inputBox.getWidth() - inputBox.getPaddingRight() - clearButton.getIntrinsicWidth()) {
                        inputBox.setText("");
                        CustomEditText.this.handleClearButton();
                    }
                    return false;
                }
            });

            inputBox.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    CustomEditText.this.handleClearButton();
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            inputBox.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {

                    if(hasFocus) {
                        if (inputBox.getText().toString().isEmpty() && inputValidationCheck.equalsIgnoreCase("default")) {
                            redMargin.setVisibility(GONE);
                            errorMessageView.setVisibility(GONE);
                        }
                    } else {
                        if (inputBox.getText().toString().isEmpty() && inputValidationCheck.equalsIgnoreCase("default")) {
                            redMargin.setVisibility(GONE);
                            errorMessageView.setVisibility(GONE);
                        }
                    }
                }
            });
        }

        InputFilter[] inputFilters = new InputFilter[1];
        inputFilters[0] = new InputFilter.LengthFilter(maxLength);

        switch(inputValidationCheck) {
            case "generic":
                inputBox.setInputType(InputType.TYPE_CLASS_TEXT);
                inputBox.setFilters(inputFilters);
                errorMessageView.setText(errorMessage);
                validateGeneric();
                break;

            case "numeric":
                inputBox.setInputType(InputType.TYPE_CLASS_NUMBER);
                inputBox.setFilters(inputFilters);
                errorMessageView.setText(errorMessage);
                validatePinCCNumber();
                break;

            case "zipcode":
                inputBox.setInputType(InputType.TYPE_CLASS_NUMBER);
                InputFilter[] zipFilter = new InputFilter[1];
                zipFilter[0] = new InputFilter.LengthFilter(5);
                inputBox.setFilters(zipFilter);
                errorMessageView.setText(errorMessage);
                validateZip();
                break;

            case "fullzip":
                inputBox.setInputType(InputType.TYPE_CLASS_NUMBER);
                InputFilter[] fullZipFilter = new InputFilter[1];
                fullZipFilter[0] = new InputFilter.LengthFilter(10);
                inputBox.setFilters(fullZipFilter);
                inputBox.setKeyListener(DigitsKeyListener.getInstance("0123456789-"));
                errorMessageView.setText(errorMessage);
                validateFullZip();
                break;

            case "textonly":
                inputBox.setFilters(new InputFilter[]{
                        new InputFilter() {
                            @Override
                            public CharSequence filter(CharSequence charSequence, int start, int end, Spanned spanned, int dStart, int dEnd) {
                                if(charSequence.toString().matches("[a-zA-Z]+")){
                                    return charSequence;
                                }
                                return "";
                            }
                        }, new InputFilter.LengthFilter(maxLength)
                });
                errorMessageView.setText(errorMessage);
                validateTextOnly();
                break;

            case "username":
                inputBox.setInputType(InputType.TYPE_CLASS_TEXT);
                inputBox.setFilters(inputFilters);
                errorMessageView.setText(errorMessage);
                validateUsername();
                break;

            case "password":
                inputBox.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                inputBox.setTransformationMethod(PasswordTransformationMethod.getInstance());
                inputBox.setFilters(inputFilters);
                errorMessageView.setText(errorMessage);
                validatePassword();
                break;

            case "phone":
                if(maxLength == 12) {
                    inputBox.setInputType(InputType.TYPE_CLASS_NUMBER);
                    inputBox.setFilters(inputFilters);
                    formatPhoneNumberWithDashes();
                } else if(maxLength == 14) {
                    inputBox.setInputType(InputType.TYPE_CLASS_PHONE);
                    inputBox.setFilters(inputFilters);
                    formatPhoneNumberWithParanthesis();
                }
                errorMessageView.setText(errorMessage);
                validatePhone();
                break;

            case "email":
                inputBox.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                inputBox.setFilters(inputFilters);
                errorMessageView.setText(errorMessage);
                validateEmail();
                break;
        }
    }

    void handleClearButton() {
        if(inputBox.getText().toString().equals("")) {
            inputBox.setCompoundDrawables(inputBox.getCompoundDrawables()[0], inputBox.getCompoundDrawables()[1], null, inputBox.getCompoundDrawables()[3]);
        } else {
            inputBox.setCompoundDrawables(inputBox.getCompoundDrawables()[0], inputBox.getCompoundDrawables()[1], clearButton, inputBox.getCompoundDrawables()[3]);
        }
    }

    void validatePinCCNumber() {
        inputBox.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus) {
                    if(inputBox.getText().toString().length() < maxLength) {
                        redMargin.setVisibility(GONE);
                        errorMessageView.setVisibility(GONE);
                    } else if(inputBox.getText().toString().isEmpty()) {
                        redMargin.setVisibility(GONE);
                        errorMessageView.setVisibility(GONE);
                    }
                } else {
                    if(inputBox.getText().toString().length() < maxLength) {
                        redMargin.setVisibility(VISIBLE);
                        errorMessageView.setVisibility(VISIBLE);
                    } else if(inputBox.getText().toString().isEmpty()) {
                        redMargin.setVisibility(VISIBLE);
                        errorMessageView.setVisibility(VISIBLE);
                    }
                }
            }
        });
    }

    void validateGeneric() {
        inputBox.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus) {
                    if(inputBox.getText().toString().isEmpty()) {
                        redMargin.setVisibility(GONE);
                        errorMessageView.setVisibility(GONE);
                    }
                } else {
                    if(inputBox.getText().toString().isEmpty()) {
                        redMargin.setVisibility(VISIBLE);
                        errorMessageView.setVisibility(VISIBLE);
                    }
                }
            }
        });
    }

    void validateZip() {
        inputBox.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
            if(hasFocus) {
                if(inputBox.getText().toString().length() < 5) {
                    redMargin.setVisibility(GONE);
                    errorMessageView.setVisibility(GONE);
                }
            } else {
                if(inputBox.getText().toString().length() < 5) {
                    redMargin.setVisibility(VISIBLE);
                    errorMessageView.setVisibility(VISIBLE);
                }
            }
            }
        });
    }

    void validateFullZip(){
        inputBox.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus) {
                    if(inputBox.getText().toString().isEmpty()) {
                        redMargin.setVisibility(GONE);
                        errorMessageView.setVisibility(GONE);
                    } else if(inputBox.getText().toString().matches("^[0-9]{5}(?:-[0-9]{4})?$")) {
                        redMargin.setVisibility(GONE);
                        errorMessageView.setVisibility(GONE);
                    } else {
                        redMargin.setVisibility(GONE);
                        errorMessageView.setVisibility(GONE);
                    }
                } else {
                    if(inputBox.getText().toString().isEmpty()) {
                        redMargin.setVisibility(VISIBLE);
                        errorMessageView.setVisibility(VISIBLE);
                    } else if(inputBox.getText().toString().matches("^[0-9]{5}(?:-[0-9]{4})?$")) {
                        redMargin.setVisibility(GONE);
                        errorMessageView.setVisibility(GONE);
                    } else {
                        redMargin.setVisibility(VISIBLE);
                        errorMessageView.setVisibility(VISIBLE);
                    }
                }
            }
        });
    }

    void validateTextOnly() {
        inputBox.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus) {
                    if(inputBox.getText().toString().isEmpty()) {
                        redMargin.setVisibility(GONE);
                        errorMessageView.setVisibility(GONE);
                    }
                } else {
                    if(inputBox.getText().toString().isEmpty()) {
                        redMargin.setVisibility(VISIBLE);
                        errorMessageView.setVisibility(VISIBLE);
                    }
                }
            }
        });
    }

    void validateUsername(){
        inputBox.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus) {
                    if(inputBox.getText().toString().isEmpty()) {
                        redMargin.setVisibility(GONE);
                        errorMessageView.setVisibility(GONE);
                    } else if(inputBox.getText().toString().matches("^[a-zA-Z0-9._-]{3,15}$")) {
                        redMargin.setVisibility(GONE);
                        errorMessageView.setVisibility(GONE);
                    } else {
                        redMargin.setVisibility(GONE);
                        errorMessageView.setVisibility(GONE);
                    }
                } else {
                    if(inputBox.getText().toString().isEmpty()) {
                        redMargin.setVisibility(VISIBLE);
                        errorMessageView.setVisibility(VISIBLE);
                    } else if(inputBox.getText().toString().matches("^[a-zA-Z0-9._-]{3,15}$")) {
                        redMargin.setVisibility(GONE);
                        errorMessageView.setVisibility(GONE);
                    } else {
                        redMargin.setVisibility(VISIBLE);
                        errorMessageView.setVisibility(VISIBLE);
                    }
                }
            }
        });
    }

    void validatePassword() {
        inputBox.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus) {
                    if(inputBox.getText().toString().isEmpty()) {
                        redMargin.setVisibility(GONE);
                        errorMessageView.setVisibility(GONE);
                    } else {
                        redMargin.setVisibility(GONE);
                        errorMessageView.setVisibility(GONE);
                    }
                } else {
                    if(inputBox.getText().toString().isEmpty()) {
                        redMargin.setVisibility(VISIBLE);
                        errorMessageView.setVisibility(VISIBLE);
                    } else if(inputBox.getText().toString().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#\\$%\\^&\\*])(?=.{8,})")) {
                        redMargin.setVisibility(GONE);
                        errorMessageView.setVisibility(GONE);
                    } else {
                        redMargin.setVisibility(VISIBLE);
                        errorMessageView.setVisibility(VISIBLE);
                    }
                }
            }
        });
    }

    void formatPhoneNumberWithDashes(){
        inputBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int before) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if(count == 1) {
                    if(start == 2 || start == 6) {
                        inputBox.setText(inputBox.getText() + "-");
                        inputBox.setSelection(inputBox.getText().length());
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    void formatPhoneNumberWithParanthesis() {
        inputBox.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
    }

    void validatePhone() {
        inputBox.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus) {
                    if(inputBox.getText().toString().isEmpty()) {
                        redMargin.setVisibility(GONE);
                        errorMessageView.setVisibility(GONE);
                    } else if(inputBox.getText().toString().length() < maxLength) {
                        redMargin.setVisibility(GONE);
                        errorMessageView.setVisibility(GONE);
                    } else if(isValidPhoneNumber(inputBox.getText().toString())) {
                        redMargin.setVisibility(GONE);
                        errorMessageView.setVisibility(GONE);
                    } else{
                        redMargin.setVisibility(GONE);
                        errorMessageView.setVisibility(GONE);
                    }
                } else {
                    if(inputBox.getText().toString().isEmpty()) {
                        redMargin.setVisibility(VISIBLE);
                        errorMessageView.setVisibility(VISIBLE);
                    } else if(inputBox.getText().toString().length() < maxLength) {
                        redMargin.setVisibility(VISIBLE);
                        errorMessageView.setVisibility(VISIBLE);
                    } else if(inputBox.getText().toString().matches("^(\\([0-9]{3}\\) |[0-9]{3}-)[0-9]{3}-[0-9]{4}$")){
                        redMargin.setVisibility(GONE);
                        errorMessageView.setVisibility(GONE);
                    } else if(!isValidPhoneNumber(inputBox.getText().toString())) {
                        redMargin.setVisibility(VISIBLE);
                        errorMessageView.setVisibility(VISIBLE);
                    }
                }
            }
        });
    }

    String getDigitsOnlyPhoneNumber(String phoneNumber) {
        String phoneNumberDigitsOnly = phoneNumber;
        if(!TextUtils.isEmpty(phoneNumber)) {
            phoneNumberDigitsOnly = phoneNumber.replaceAll("[^0-9]", "");
        }
        return phoneNumberDigitsOnly;
    }

    boolean isValidPhoneNumber(String phoneNumber) {
        boolean isValid = false;
        if(!TextUtils.isEmpty(phoneNumber) && Patterns.PHONE.matcher(phoneNumber).matches()) {
            String phoneNumberDigitsOnly = getDigitsOnlyPhoneNumber(phoneNumber);
            if(TextUtils.isDigitsOnly(phoneNumberDigitsOnly) && phoneNumberDigitsOnly.length() == 10 && !phoneNumberDigitsOnly.startsWith("1")) {
                isValid = true;
            }
        }
        return isValid;
    }

    void validateEmail() {
        inputBox.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus) {
                    if(inputBox.getText().toString().isEmpty()) {
                        redMargin.setVisibility(GONE);
                        errorMessageView.setVisibility(GONE);
                    } else {
                        redMargin.setVisibility(GONE);
                        errorMessageView.setVisibility(GONE);
                    }
                } else {
                    if(inputBox.getText().toString().isEmpty()) {
                        redMargin.setVisibility(VISIBLE);
                        errorMessageView.setVisibility(VISIBLE);
                    } else if(inputBox.getText().toString().matches("^\\w+([\\.-]?\\w+)*@\\w+([\\.-]?\\w+)*(\\.\\w{2,3})+$")) {
                        redMargin.setVisibility(GONE);
                        errorMessageView.setVisibility(GONE);
                    } else {
                        redMargin.setVisibility(VISIBLE);
                        errorMessageView.setVisibility(VISIBLE);
                    }
                }
            }
        });
    }
}

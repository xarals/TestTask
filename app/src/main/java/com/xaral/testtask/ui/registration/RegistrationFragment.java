package com.xaral.testtask.ui.registration;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.xaral.testtask.R;
import com.xaral.testtask.api.TestAssignmentApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistrationFragment extends Fragment {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private RadioGroup positionsGroup;
    private LinearLayout photoLayout;
    private TextInputLayout photoInputLayout, nameInputLayout, emailInputLayout, phoneInputLayout;
    private TextInputEditText photoInputEditText, nameInputEditText, emailInputEditText, phoneInputEditText;
    private TextView photoHelper, nameHelper, emailHelper, phoneHelper, uploadButton, submitButton;

    private ConstraintLayout successView, errorView, errorConnectionView;
    private TextView reconnect, retry, gotIt, messageView;
    private ImageView buttonCloseSuccess, buttonCloseError;
    private Map<String, Integer> positions = new LinkedHashMap<>();

    private boolean isLoading = false;
    private boolean photoError = false;

    private String name, email, phone;
    private byte[] base64Image;

    boolean isPhotoError = false;

    /**
     * ActivityResultLauncher for handling the result of image selection.
     */
    private final ActivityResultLauncher<Intent> pickImageResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                base64Image = null;
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    updateSubmitButton(getContext());
                    if (selectedImageUri != null) {
                        try (InputStream inputStream = getActivity().getContentResolver().openInputStream(selectedImageUri)) {
                            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                            if (bitmap != null && (bitmap.getWidth() < 70 || bitmap.getHeight() < 70)) {
                                photoLayout.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.input_background_error));
                                photoInputLayout.setHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.red)));
                                photoInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.red)));
                                photoHelper.setText(R.string.photo_should_be_with_resolution_at_least_70x70px);
                                photoHelper.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
                                isPhotoError = true;
                                return;
                            }

                            int bufferSize = 1024;
                            byte[] buffer = new byte[bufferSize];

                            int len;
                            while ((len = inputStream.read(buffer)) != -1) {
                                byteBuffer.write(buffer, 0, len);
                            }
                            byte[] bytes = byteBuffer.toByteArray();

                            if (bytes.length > 5 * 1024 * 1024) {
                                bytes = null;
                                photoLayout.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.input_background_error));
                                photoInputLayout.setHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.red)));
                                photoInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.red)));
                                photoHelper.setText(R.string.size_must_not_exceed_5mb);
                                photoHelper.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
                                isPhotoError = true;
                                return;
                            }

                            isPhotoError = false;

                            phoneInputEditText.clearFocus();
                            photoLayout.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.input_background_normal));
                            photoInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.grey)));
                            photoInputLayout.setHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.blue)));
                            photoHelper.setText("Size: " + ((bytes.length * 10 / 1024) / 10.0) + "KB");
                            photoHelper.setTextColor(ContextCompat.getColor(getContext(), R.color.grey));

                            base64Image = bytes;
                            photoInputEditText.setText(selectedImageUri.getPath());
                            updateSubmitButton(getContext());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });

    @SuppressLint("ResourceType")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_registration, container, false);

        positionsGroup = root.findViewById(R.id.positionsGroup);

        photoLayout = root.findViewById(R.id.photoLayout);
        photoInputLayout = root.findViewById(R.id.photoInputLayout);
        photoInputEditText = root.findViewById(R.id.photoInputEditText);
        photoHelper = root.findViewById(R.id.photoHelper);

        nameInputLayout = root.findViewById(R.id.nameInputLayout);
        nameInputEditText = root.findViewById(R.id.nameInputEditText);
        nameHelper = root.findViewById(R.id.nameHelper);

        emailInputLayout = root.findViewById(R.id.emailInputLayout);
        emailInputEditText = root.findViewById(R.id.emailInputEditText);
        emailHelper = root.findViewById(R.id.emailHelper);

        phoneInputLayout = root.findViewById(R.id.phoneInputLayout);
        phoneInputEditText = root.findViewById(R.id.phoneInputEditText);
        phoneHelper = root.findViewById(R.id.phoneHelper);

        uploadButton = root.findViewById(R.id.uploadButton);
        submitButton = root.findViewById(R.id.submit);

        successView = root.findViewById(R.id.successView);
        errorView = root.findViewById(R.id.errorView);
        errorConnectionView = root.findViewById(R.id.errorConnectionView);

        reconnect = root.findViewById(R.id.reconnect);
        retry = root.findViewById(R.id.retry);
        gotIt = root.findViewById(R.id.gotIt);
        buttonCloseSuccess = root.findViewById(R.id.buttonCloseSuccess);
        buttonCloseError = root.findViewById(R.id.buttonCloseError);

        messageView = root.findViewById(R.id.messageView);

        gotIt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                navController.navigate(R.id.navigation_users);
            }
        });

        reconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (positionsGroup.getChildCount() == 0) {
                    setStyleButton(getContext(), reconnect, true, true);
                    getPositions(getContext());
                } else {
                    errorConnectionView.setVisibility(View.INVISIBLE);
                }
            }
        });

        buttonCloseError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                errorView.setVisibility(View.INVISIBLE);
            }
        });

        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                errorView.setVisibility(View.INVISIBLE);
            }
        });

        buttonCloseSuccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                successView.setVisibility(View.INVISIBLE);
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickImageResultLauncher.launch(intent);
            }
        });

        nameInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                name = charSequence.toString();
                updateSubmitButton(getContext());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        emailInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                email = charSequence.toString();
                updateSubmitButton(getContext());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        phoneInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                phone = charSequence.toString();
                updateSubmitButton(getContext());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        nameInputEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                updateSubmitButton(view.getContext());
                if (hasFocus) {
                    return;
                }

                name = Objects.requireNonNull(nameInputEditText.getText()).toString();
                updateSubmitButton(view.getContext());
                String message = checkName(name);
                setStyleInputLayout(view.getContext(), nameInputLayout, nameHelper, message, !message.equals(""));
            }
        });

        emailInputEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                updateSubmitButton(view.getContext());
                if (hasFocus) {
                    return;
                }

                email = Objects.requireNonNull(emailInputEditText.getText()).toString();
                updateSubmitButton(view.getContext());
                String message = checkEmail(email);
                setStyleInputLayout(view.getContext(), emailInputLayout, emailHelper, message, !message.equals(""));
            }
        });

        // Phone input filters
        phoneInputEditText.setFilters(new InputFilter[]{
                (source, start, end, dest, dstart, dend) -> {
                    for (int i = start; i < end; i++) {
                        if (!Character.isDigit(source.charAt(i))) {
                            return "";
                        }
                    }
                    return null;
                }
        });

        phoneInputEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                updateSubmitButton(view.getContext());
                if (hasFocus) {
                    return;
                }

                phone = Objects.requireNonNull(phoneInputEditText.getText()).toString().replaceAll("[^0-9]", "");
                String message = checkPhone(phone);
                setStyleInputLayout(view.getContext(), phoneInputLayout, phoneHelper, message.equals("") ? getString(R.string._38_xxx_xxx_xx_xx) : message, !message.equals(""));
            }
        });

        photoInputEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (isPhotoError)
                    return;
                if (hasFocus) {
                    photoLayout.setBackground(ContextCompat.getDrawable(v.getContext(), R.drawable.input_background_focused));
                    photoHelper.setTextColor(ContextCompat.getColor(v.getContext(), R.color.blue));
                } else {
                    photoLayout.setBackground(ContextCompat.getDrawable(v.getContext(), R.drawable.input_background_normal));
                    photoHelper.setTextColor(ContextCompat.getColor(v.getContext(), R.color.grey));
                }
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (positionsGroup.getCheckedRadioButtonId() == -1 || base64Image == null) {
                    setStyleButton(view.getContext(), submitButton, true, true);
                    return;
                }
                name = Objects.requireNonNull(nameInputEditText.getText()).toString();
                email = Objects.requireNonNull(emailInputEditText.getText()).toString();
                phone = Objects.requireNonNull(phoneInputEditText.getText()).toString().replaceAll("[^0-9]", "");

                Integer positionId;
                try {
                    RadioButton radioButton = positionsGroup.findViewById(positionsGroup.getCheckedRadioButtonId());
                    positionId = positions.get(radioButton.getText().toString());
                } catch (Exception e) {
                    setStyleButton(view.getContext(), submitButton, true, true);
                    return;
                }
                if (!checkName(name).isEmpty() || !checkEmail(email).isEmpty() || !checkPhone(phone).isEmpty() || positionId == null) {
                    setStyleButton(view.getContext(), submitButton, true, true);
                    return;
                }

                phone = "+380" + phone.substring(phone.length() - 9);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject answer = TestAssignmentApi.newUser(name, email, phone, positionId, base64Image);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        if (answer.getBoolean("success")) {
                                            successView.setVisibility(View.VISIBLE);
                                        } else {
                                            messageView.setText(answer.getString("message"));
                                            errorView.setVisibility(View.VISIBLE);
                                        }
                                    } catch (JSONException ignore) {}
                                }
                            });
                        } catch (Exception ignore) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    errorConnectionView.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    }
                }).start();
            }
        });

        updateSubmitButton(root.getContext());

        getPositions(root.getContext());

        return root;
    }

    /**
     * Fetches the positions from the API and add to the radio group.
     *
     * @param context The context
     */
    private void getPositions(Context context) {
        if (isLoading)
            return;
        isLoading = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    positions = TestAssignmentApi.getPositions();
                } catch (Exception e) {
                    isLoading = false;
                    setStyleButton(context, reconnect, true, false);
                    errorConnectionView.setVisibility(View.VISIBLE);
                    return;
                }

                if (positions == null) {
                    positions = new LinkedHashMap<>();
                    isLoading = false;
                    setStyleButton(context, reconnect, true, false);
                    errorConnectionView.setVisibility(View.VISIBLE);
                    return;
                }

                handler.post(new Runnable() {
                    @SuppressLint("ResourceType")
                    @Override
                    public void run() {
                        try {
                            for (String position : positions.keySet()) {
                                RadioButton radioButton = new RadioButton(context);
                                radioButton.setText(position);
                                ColorStateList colorStateList = ContextCompat.getColorStateList(context, R.color.blue);
                                radioButton.setButtonTintList(colorStateList);
                                radioButton.setTextSize(16);
                                radioButton.setHeight(Math.round(48 * getResources().getDisplayMetrics().density));
                                radioButton.setPaddingRelative(Math.round(12 * getResources().getDisplayMetrics().density), 0, 0, 0);
                                positionsGroup.addView(radioButton);
                            }
                            positionsGroup.check(1);
                        } catch (Exception ignore) {}
                        finally {
                            errorConnectionView.setVisibility(View.INVISIBLE);
                            setStyleButton(getContext(), reconnect, true, false);
                            isLoading = false;
                        }
                    }
                });
            }
        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    /**
     * Updates the submit button state based on form validation.
     *
     * @param context The context
     */
    private void updateSubmitButton(Context context) {
        if (positionsGroup.getCheckedRadioButtonId() == -1 || base64Image == null) {
            setStyleButton(context, submitButton, true, true);
            return;
        }
        name = Objects.requireNonNull(nameInputEditText.getText()).toString();
        email = Objects.requireNonNull(emailInputEditText.getText()).toString();
        phone = Objects.requireNonNull(phoneInputEditText.getText()).toString().replaceAll("[^0-9]", "");

        Integer positionId;
        try {
            RadioButton radioButton = positionsGroup.findViewById(positionsGroup.getCheckedRadioButtonId());
            positionId = positions.get(radioButton.getText().toString());
        } catch (Exception e) {
            setStyleButton(context, submitButton, true, true);
            return;
        }
        if (!checkName(name).isEmpty() || !checkEmail(email).isEmpty() || !checkPhone(phone).isEmpty() || positionId == null) {
            setStyleButton(context, submitButton, true, true);
            return;
        }

        setStyleButton(context, submitButton, true, false);
    }

    /**
     * Validates the name input.
     *
     * @param name The name input
     * @return Empty string if the name is valid, message otherwise
     */
    private String checkName(String name) {
        if (name.isEmpty())
            return getString(R.string.required_field);
        else if (name.length() < 2)
            return getString(R.string.the_name_must_be_at_least_2_characters);
        else if (name.length() > 60)
            return getString(R.string.the_name_must_be_no_more_than_60_characters);
        else
            return "";
    }

    /**
     * Validates the email input.
     *
     * @param email The email input
     * @return Empty string if the email is valid, message otherwise
     */
    private String checkEmail(String email) {
        if (email.isEmpty())
            return getString(R.string.required_field);

        final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
        Matcher matcher = EMAIL_PATTERN.matcher(email);

        if (!matcher.matches())
            return getString(R.string.the_email_must_be_a_valid_email_address);
        else
            return "";
    }

    /**
     * Validates the phone input.
     *
     * @param phone The phone input
     * @return Empty string if the phone is valid, message otherwise
     */
    private String checkPhone(String phone) {
        if (phone.isEmpty())
            return getString(R.string.required_field);
        else if (phone.startsWith("0") && phone.length() != 10)
            return getString(R.string.the_phone_must_be_a_valid_phone_number);
        else if (phone.startsWith("380") && phone.length() != 12)
            return getString(R.string.the_phone_must_be_a_valid_phone_number);
        else if (!phone.startsWith("0") && !phone.startsWith("380") && phone.length() != 9)
            return getString(R.string.the_phone_must_be_a_valid_phone_number);
        else
            return "";
    }

    /**
     * Sets the button style based on the loading state.
     *
     * @param context The context
     * @param button  The button to style
     * @param isPrimary Whether the button is primary
     * @param isDisabled Whether the button is disabled
     */
    private void setStyleButton(Context context, TextView button, boolean isPrimary, boolean isDisabled) {
        Drawable drawable;
        if (isDisabled && isPrimary)
            drawable = ContextCompat.getDrawable(context, R.drawable.disabled_primary_button);
        else if (isDisabled)
            drawable = ContextCompat.getDrawable(context, R.drawable.normal_secondary_button);
        else if (isPrimary)
            drawable = ContextCompat.getDrawable(context, R.drawable.primary_button_background);
        else
            drawable = ContextCompat.getDrawable(context, R.drawable.secondary_button_background);

        int colorId;
        if (isDisabled)
            colorId = ContextCompat.getColor(context, R.color.grey);
        else if (isPrimary)
            colorId = ContextCompat.getColor(context, R.color.black);
        else
            colorId = ContextCompat.getColor(context, R.color.blue);

        submitButton.setBackground(drawable);
        submitButton.setTextColor(colorId);
    }

    /**
     * Sets the input layout style based on the params.
     *
     * @param context The context
     * @param textInputLayout  The text input layout to style
     * @param inputHelper The hint to style
     * @param helperText The text for inputHelper
     * @param isError Is this helperText an error message
     */
    private void setStyleInputLayout(Context context, TextInputLayout textInputLayout, TextView inputHelper, String helperText, boolean isError) {
        if (!isError) {
            textInputLayout.setBoxStrokeColor(ContextCompat.getColor(context, R.color.blue));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                textInputLayout.setCursorColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.black)));
            }
            textInputLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.input_background));
            textInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.grey)));
            textInputLayout.setHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.blue)));
            inputHelper.setTextColor(ContextCompat.getColor(context, R.color.grey));
        } else {
            textInputLayout.setBoxStrokeColor(ContextCompat.getColor(context, R.color.red));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                textInputLayout.setCursorColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.red)));
            }
            textInputLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.input_background_error));
            textInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.red)));
            textInputLayout.setHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.red)));
            inputHelper.setTextColor(ContextCompat.getColor(context, R.color.red));
        }
        inputHelper.setText(helperText);
    }
}
package shreyas.customedittext;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CustomEditText v=(CustomEditText) findViewById(R.id.myeditfield);
        v.getInputBox().setTextSize(16);
        v.getInputBox().setMaxLines(1);

        v.getErrorMessageView().setText("User name is incorrect or not valid");
    }
}

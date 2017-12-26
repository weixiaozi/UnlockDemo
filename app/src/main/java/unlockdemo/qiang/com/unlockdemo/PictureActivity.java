package unlockdemo.qiang.com.unlockdemo;

import android.annotation.TargetApi;
import android.app.PictureInPictureParams;
import android.content.res.Configuration;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Rational;
import android.view.View;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.O)
public class PictureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
        findViewById(R.id.button_pip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PictureInPictureParams params = new PictureInPictureParams.Builder()
                        .setAspectRatio(new Rational(10, 16))
                        .build();
                enterPictureInPictureMode(params);
            }
        });
        findViewById(R.id.button_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(PictureActivity.this,"picture in picture",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
    }
}

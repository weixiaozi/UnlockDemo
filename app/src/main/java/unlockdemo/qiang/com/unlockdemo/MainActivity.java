package unlockdemo.qiang.com.unlockdemo;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.Vibrator;
import android.support.annotation.RequiresApi;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import static android.hardware.fingerprint.FingerprintManager.FINGERPRINT_ERROR_CANCELED;

public class MainActivity extends AppCompatActivity {
    private Button button_unlock_gesture;
    private Button button_unlock_finger;
    private FingerprintManagerCompat fingerprintManager;
    private Vibrator vibrator;
    private android.support.v4.os.CancellationSignal cancellationSignal;
    private CryptoObjectCreator mCryptoObjectCreator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button_unlock_gesture = findViewById(R.id.button_unlock_gesture);
        button_unlock_finger = findViewById(R.id.button_unlock_finger);
        button_unlock_gesture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        button_unlock_finger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    initFinger();
                } else {
                    Toast.makeText(MainActivity.this, "6.0以上才支持指纹解锁", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    /**
     * 创建指纹解锁需要的对象
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initFinger() {
        if (fingerprintManager == null)
            fingerprintManager = FingerprintManagerCompat.from(this);

        if (!fingerprintManager.isHardwareDetected()) {
            Toast.makeText(this, "设备不支持指纹解锁", Toast.LENGTH_SHORT).show();
            return;
        }

        KeyguardManager keyguardManager = (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
        if (!keyguardManager.isKeyguardSecure()) {
            Toast.makeText(this, "设备没有开启锁屏保护", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!fingerprintManager.hasEnrolledFingerprints()) {
            Toast.makeText(this, "设备没有录制指纹", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cancellationSignal == null)
            cancellationSignal = new android.support.v4.os.CancellationSignal();
        if (vibrator == null)
            vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);

        Toast.makeText(MainActivity.this, "请开始指纹解锁", Toast.LENGTH_SHORT).show();

        try {
            mCryptoObjectCreator = new CryptoObjectCreator(new CryptoObjectCreator.ICryptoObjectCreateListener() {
                @Override
                public void onDataPrepared(FingerprintManagerCompat.CryptoObject cryptoObject) {
                    // 如果需要一开始就进行指纹识别，可以在秘钥数据创建之后就启动指纹认证
                    fingerprintManager.authenticate(cryptoObject, 0, cancellationSignal, new FingerCallBack(MainActivity.this), null);
                }
            });

        } catch (Throwable throwable) {
        }

    }

    static class FingerCallBack extends FingerprintManagerCompat.AuthenticationCallback {
        private WeakReference<MainActivity> reference;

        public FingerCallBack(MainActivity mActivity) {
            reference = new WeakReference<>(mActivity);
        }

        @Override
        public void onAuthenticationError(int errMsgId, CharSequence errString) {
            super.onAuthenticationError(errMsgId, errString);
            //取消时回掉FINGERPRINT_ERROR_CANCELED
            if (errMsgId != FINGERPRINT_ERROR_CANCELED) {
                Toast.makeText(reference.get(), "authoentError errorCode:" + errMsgId, Toast.LENGTH_SHORT).show();
                Log.i("callback:", "authoentError errorCode:" + errMsgId);
                reference.get().cancellationSignal.cancel();
                reference.get().cancellationSignal = null;
                reference.get().vibrator.vibrate(300);
            }
        }

        @Override
        public void onAuthenticationFailed() {
            super.onAuthenticationFailed();
            Toast.makeText(reference.get(), "authoentFail", Toast.LENGTH_SHORT).show();
            Log.i("callback:", "authoentFail");
            reference.get().vibrator.vibrate(300);
        }

        @Override
        public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
            super.onAuthenticationHelp(helpMsgId, helpString);
            Log.i("callback:", "authoenthelp id：" + helpMsgId);
            Toast.makeText(reference.get(), "authoenthelp id：" + helpMsgId, Toast.LENGTH_SHORT).show();
            reference.get().vibrator.vibrate(300);
        }

        @Override
        public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
            super.onAuthenticationSucceeded(result);
            Log.i("callback:", "authentic success");
            Toast.makeText(reference.get(), "指纹认证成功了", Toast.LENGTH_SHORT).show();
            reference.get().cancellationSignal = null;

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCryptoObjectCreator != null)
            mCryptoObjectCreator.onDestroy();
        mCryptoObjectCreator = null;
    }
}

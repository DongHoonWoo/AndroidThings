package me.iot.autoledlight;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String LED_PIN_NAME = "BCM5";
    private static final String CDS_PIN_NAME = "BCM6";

    private Gpio ledGpio;
    private Gpio photoregistorGpio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PeripheralManagerService service = new PeripheralManagerService();
        try {
            // led 출력핀 설정, 초기값을 high로 하여 led를 off 상태로 유지
            ledGpio = service.openGpio(LED_PIN_NAME);
            ledGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);

            // photoregistor 설정
            // 입력, low 값이 true, falling-rising edge 둘다 callback 호출 되도록 설정함
            photoregistorGpio = service.openGpio(CDS_PIN_NAME);
            photoregistorGpio.setDirection(Gpio.DIRECTION_IN);
            photoregistorGpio.setActiveType(Gpio.ACTIVE_LOW);
            photoregistorGpio.setEdgeTriggerType(Gpio.EDGE_BOTH);
            photoregistorGpio.registerGpioCallback(photoregistorCallback);

        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            ledGpio.close();
            photoregistorGpio.unregisterGpioCallback(photoregistorCallback);
            photoregistorGpio.close();
        } catch (IOException e) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, e.toString());
            }
        } finally {
            ledGpio = null;
            photoregistorGpio = null;
        }
    }

    private GpioCallback photoregistorCallback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            try {
                Log.d(TAG, gpio + "photregistor GPIO changed - " + gpio.getValue());
                if (gpio.getValue()) {
                    if (ledGpio != null) {
                        ledGpio.setValue(false);
                    }
                } else {
                    if (ledGpio != null) {
                        ledGpio.setValue(true);
                    }
                }
            }catch (Exception e) {
                Log.d(TAG, e.toString());
            }

            return true;
        }

        @Override
        public void onGpioError(Gpio gpio, int error) {
            Log.d(TAG, gpio + "cds onGpioError : " + error);
        }
    };
}

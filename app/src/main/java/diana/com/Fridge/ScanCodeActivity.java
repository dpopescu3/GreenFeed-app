package diana.com.Fridge;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.google.zxing.Result;

import diana.com.Fridge.FridgeActivity;
import diana.com.Home.MainActivity;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScanCodeActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {



    ZXingScannerView ScannerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScannerView=new ZXingScannerView(this);
        setContentView(ScannerView);


    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void handleResult(Result result) {
        Toast.makeText(this, result.getText(), Toast.LENGTH_LONG).show();
        onBackPressed();
    }


    @Override
    protected void onResume() {
        super.onResume();
            ScannerView.setResultHandler(this);
            ScannerView.startCamera();

    }

    @Override
    protected void onPause() {
        super.onPause();
        ScannerView.stopCamera();
    }
}

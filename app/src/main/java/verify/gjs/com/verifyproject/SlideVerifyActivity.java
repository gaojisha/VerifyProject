package verify.gjs.com.verifyproject;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.InputStream;

import verify.gjs.com.verifyproject.verifyview.BanClickSeekbar;
import verify.gjs.com.verifyproject.verifyview.SlideValidateView;

public class SlideVerifyActivity extends AppCompatActivity {

    private SlideValidateView mSlideValidateView;
    private BanClickSeekbar mSeekbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide_verify);

        mSlideValidateView = findViewById(R.id.slide_view);
        mSeekbar = findViewById(R.id.seek_bar);
        imageVerify();
    }

    //list排重
    private JSONArray dealdata() {
        String data = getJson(this, "data.txt");
        JSONArray jsonArray = null, json = null;
        try {
            jsonArray = new JSONArray(data);
            json = jsonArray;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                for (int j = jsonArray.length() - 1; j > i; j--) {
                    if (jsonArray.getJSONObject(i).get("ids").equals("0")) {
                        if (jsonArray.getJSONObject(i).get("albumId").equals(json.getJSONObject(j).get("albumId"))) {
                            json.remove(i);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    //list排重
    public static JSONArray delRepeatIndexid(JSONArray array) {
        JSONArray arrayTemp = new JSONArray();
        try {
            int num = 0;
            for (int i = 0; i < array.length(); i++) {
                if (num == 0) {
                    arrayTemp.put(array.get(i));
                } else {
                    int numJ = 0;
                    for (int j = 0; j < arrayTemp.length(); j++) {
                        if (array.getJSONObject(i).getString("ids").equals("0")) {
                            if (array.getJSONObject(i).getString("albumId").equals(arrayTemp.getJSONObject(j).getString("albumId"))) {
                                arrayTemp.remove(j);
                                arrayTemp.put(array.getJSONObject(i));
                                break;
                            }
                        }
                        numJ++;
                    }
                    if (numJ - 1 == arrayTemp.length() - 1) {
                        arrayTemp.put(array.getJSONObject(i));
                    }
                }
                num++;
            }
        } catch (Exception e) {

        }
        return arrayTemp;
    }

    //读取文件json数据
    public static String getJson(Context context, String fileName) {
        String resultString = "";
        try {
            InputStream inputStream = context.getResources().getAssets().open(fileName);
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            resultString = new String(buffer, "UTF-8");
        } catch (Exception e) {
            // TODO: handle exception
        }
        return resultString;
    }

    //滑块验证监听设置
    private void imageVerify() {
        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mSlideValidateView.setSlideProgress(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mSlideValidateView.checkSlidePoint(seekBar.getProgress());
            }
        });
        mSlideValidateView.setSlideListener(new SlideValidateView.SlideListener() {
            @Override
            public void success() {
                Toast.makeText(SlideVerifyActivity.this,"success",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void error() {
                mSlideValidateView.reset();
                mSeekbar.setProgress(0);
                Toast.makeText(SlideVerifyActivity.this,"error",Toast.LENGTH_SHORT).show();
            }
        });
    }

}

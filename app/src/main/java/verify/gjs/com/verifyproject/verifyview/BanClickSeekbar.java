package verify.gjs.com.verifyproject.verifyview;

import android.content.Context;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * <pre>
 *     author  : gaojisha
 *     e-mail  : work_practical@163.com
 *     time    : 2018/08/02
 *     desc    : 滑动拼图验证 SeekBar
 *     version : 1.0
 * </pre>
 */
public class BanClickSeekbar extends AppCompatSeekBar{
    private int index = 150;
    private boolean k = true;

    public BanClickSeekbar(Context context) {
        super(context);
    }

    public BanClickSeekbar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BanClickSeekbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        //目的  验证完成后  再次滑动  seekbar不处理
        int x = (int) event.getX();
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            k = true;
            if (x - index > 20) {
                k = false;
                return true;
            }
        }
        if (event.getAction() == MotionEvent.ACTION_MOVE){
            if (!k){
                return true;
            }
        }
        return super.dispatchTouchEvent(event);
    }


}

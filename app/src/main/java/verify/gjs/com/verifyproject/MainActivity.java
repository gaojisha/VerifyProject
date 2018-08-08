package verify.gjs.com.verifyproject;

import android.content.Intent;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * <pre>
 *     author  : gaojisha
 *     e-mail  : worK_practical@163.com
 *     time    : 2018/08/02
 *     desc    : 实现图片拖动验证
 *     version : 1.0
 * </pre>
 */
public class MainActivity extends AppCompatActivity {
    private ImageView mImageStart, mImageTarget;
    private float mTargetX, mTargetY, mStartX, mStartY;
    private int mTranslateX, mTranslateY, mImgStartY, mImgStartX;
    private RelativeLayout.LayoutParams mLayoutParams,mTargetLayoutParams;
    private int dip_110;//TargetView 的高度
    private boolean isCheck;//验证是否成功

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageStart = findViewById(R.id.img_start);
        mImageTarget = findViewById(R.id.img_end);
        //保存组件初始位置参数
        mLayoutParams = (RelativeLayout.LayoutParams) mImageStart.getLayoutParams();
        //保存目标组件位置参数
        mTargetLayoutParams = (RelativeLayout.LayoutParams) mImageTarget.getLayoutParams();
        //初始化目标组件
        initView();
        initOperate();
    }

    /**
     * 初始化图片验证
     */
    private void initView() {
        //获取屏幕宽高
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        //确定TargeView的横坐标
        mTargetX = (int) (Math.random() * point.x);
        //获取TargetView的高度与宽度，均写死为110dp
        dip_110 = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,110,getResources().getDisplayMetrics());
        if(mTargetX > point.x - dip_110){
            mTargetX = point.x - dip_110;
        }
        //确定TargetView的纵坐标
        int bottomMargin = (int)Math.random() * dip_110;
        //设置TargetView 的位置参数
        mTargetLayoutParams.setMargins((int)mTargetX,0,0, dip_110);
        mImageTarget.setLayoutParams(mTargetLayoutParams);

        //设置TargetView 的动画
        ScaleAnimation mScaleAnimation = new ScaleAnimation(1.0f,1.1f,1.0f,1.1f, Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        mScaleAnimation.setRepeatMode(ScaleAnimation.REVERSE);
        mScaleAnimation.setRepeatCount(Animation.INFINITE);
        mScaleAnimation.setDuration(1000);
        mImageTarget.startAnimation(mScaleAnimation);
    }

    private void initOperate() {
        mImageStart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //图片起始位置
                        mImgStartX = mImageStart.getLeft();
                        mImgStartY = mImageStart.getTop();
                        //目标位置
                        mTargetY = mImageTarget.getTop();
                        //手指按下位置
                        mStartX = event.getRawX();
                        mStartY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                            //  不要直接用getX和getY,这两个获取的数据已经是经过处理的,容易出现图片抖动的情况
                            mTranslateX = (int) (event.getRawX() - mStartX);
                            mTranslateY = (int) (event.getRawY() - mStartY);
                            int endx = mImgStartX + mTranslateX;
                            int endy = mImgStartY + mTranslateY;
                            //组件拖动到当前位置
                            mLayoutParams.setMargins(endx, endy, 0, 0);
                            mImageStart.setLayoutParams(mLayoutParams);
                            if (endy > mTargetY && (endy + mImageStart.getHeight()) < (mTargetY + mImageTarget.getHeight()) && endx > mTargetX && (endx + mImageStart.getWidth()) < (mTargetX + mImageTarget.getWidth())) {
                                //进入目标范围之后更新状态
                                isCheck = true;
                                mImageTarget.setImageResource(R.mipmap.img_aty_verify_end_check);
                            } else if(isCheck){
                                //进到范围组件范围内  ischeck  设置为true之后，重新离开目标范围，需重置状态
                                isCheck = false;
                                mImageTarget.setImageResource(R.mipmap.img_aty_verify_end);
                            }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if(isCheck){
                            //松手时进入目标范围，并且未离开
                            Toast.makeText(MainActivity.this,"验证成功",Toast.LENGTH_SHORT).show();
                            mImageTarget.setImageResource(R.mipmap.img_aty_verify_end_check);
                            mImageStart.setEnabled(false);
                            mImageStart.setClickable(false);
                            startActivity(new Intent(MainActivity.this, SlideVerifyActivity.class));
                        } else {
                            //松手时不在目标范围内
                            Toast.makeText(MainActivity.this,"验证失败",Toast.LENGTH_SHORT).show();
                            mImageTarget.setImageResource(R.mipmap.img_aty_verify_end);
                            mLayoutParams.setMargins(mImgStartX,mImgStartY,0,0);
                            mImageStart.setLayoutParams(mLayoutParams);
                        }
                        break;
                }
                return true;
            }
        });
    }

}

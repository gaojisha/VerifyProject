package verify.gjs.com.verifyproject.verifyview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;

import verify.gjs.com.verifyproject.R;

/**
 * <pre>
 *     author  : gaojisha
 *     e-mail  : gaojisha@feinno.com
 *     time    : 2018/08/02
 *     desc    : 滑动拼图验证 ImageView
 *     version : 1.0
 * </pre>
 */
public class SlideValidateView extends AppCompatImageView {

    //    private int max_with;
//    private int max_height;
    private Paint mPaint;
    private Bitmap mBitmap;//初始化ImageView图片内容
    private Bitmap mResourceBitmap;//初始化滑块图片
    private Bitmap mSlideBitmap;//处理好宽高的滑块图片
    private Bitmap mShadeBitmap;//处理好的阴影图片
    private int mSlideWith = 0;//滑块宽度
    private int mSlideHeight = 0;//滑块高度
    private float mSlideWithScale = 0;//滑块相对于图片宽度缩放
    private int mShadeRandomX = 0;//shade  阴影坐标x
    private int mShadeRandomY = 0;//shade  阴影坐标y
    private int mSlideMoveDistance = 0;//滑块图片移动距离
    private SlideListener mSlideListener;

    //diviation 误差 设置的数值小于2 则默认是2，设置大于100 默认为100，可以设置2-100之前任何数值
    private int diviation = 2;//偏移值（2~100）
    private boolean isReset = true;//验证失败需重置

    public SlideValidateView(Context context) {
        this(context, null);
    }

    public SlideValidateView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideValidateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //获取自定义组件全部参数并初始化
        TypedArray typedArray = context.obtainStyledAttributes(R.styleable.SlideValidateView);
//        max_with = typedArray.getDimensionPixelOffset(R.styleable.SlideValidateView_max_width,0);
//        max_height = typedArray.getDimensionPixelOffset(R.styleable.SlideValidateView_max_height, 0);
        diviation = typedArray.getInteger(R.styleable.SlideValidateView_slide_diviation, 0);
        Drawable drawable = typedArray.getDrawable(R.styleable.SlideValidateView_slide_bitmap);
        //初始化SlideImage宽度占imageView宽的比例
        mSlideWithScale = typedArray.getFloat(R.styleable.SlideValidateView_slide_width_scale, 0.2f);
        //初始化divitaion
        if (diviation < 2) {
            diviation = 2;
        } else if (diviation > 100) {
            diviation = 100;
        }
        //初始化滑块图
        mResourceBitmap = initResourceBitmap(drawable);
        typedArray.recycle();
        //画笔初始化
        mPaint = new Paint();
        mPaint.setAntiAlias(true);//抗锯齿

//        if(max_with == 0 || max_height == 0){
//            Point point = new Point();
//            ((Activity)context).getWindowManager().getDefaultDisplay().getSize(point);
//            max_with = point.x;
//            max_height = point.y/2;
//        }
    }

    /**
     * 图片数据初始化
     *
     * @param drawable drawable类型图片数据
     * @return 图片bitmap数据
     */
    private Bitmap initResourceBitmap(Drawable drawable) {
        Bitmap bitmap;
        if (drawable == null) {
            bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.star_shade);
        } else {
            bitmap = drawable2Bitmap(drawable);
        }
        return bitmap;
    }

    /**
     * drawable 转 bitmap
     *
     * @param drawable drawable格式图片数据
     * @return Bitmap  转为Bitmap格式的图片数据
     */
    private Bitmap drawable2Bitmap(Drawable drawable) {
        if (drawable == null) {
            return null;
        } else if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            return bitmapDrawable.getBitmap();
        }
        //drawable 宽高
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        //获取图片质量
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
        //创建新bitmap保存图片
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        //使用canvas把drawable数据给bitmap
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isReset) {
            //获取图片内容
            mBitmap = getViewBitmap();
            if (mSlideWith == 0 && mBitmap != null && mBitmap.getWidth() != 0 && mBitmap.getHeight() != 0) {
                initSlideWH();
            }
            initShadeRandomXY();
            mShadeBitmap = Bitmap.createBitmap(mSlideWith, mSlideHeight, Bitmap.Config.ARGB_8888);
            //用指定颜色填充图片像素
            mShadeBitmap.eraseColor(Color.GRAY);
            mSlideBitmap = Bitmap.createBitmap(mBitmap, mShadeRandomX, mShadeRandomY, mSlideWith, mSlideHeight);
        }
        isReset = false;
        canvas.drawBitmap(drawImage(mShadeBitmap), mShadeRandomX, mShadeRandomY, mPaint);
        canvas.drawBitmap(drawImage(mSlideBitmap), mSlideMoveDistance, mShadeRandomY, mPaint);
    }

    private Bitmap getViewBitmap() {
        Bitmap bitmap = drawable2Bitmap(getDrawable());
        float scaleX = 1.0f;
        float scaleY = 1.0f;
        scaleX = getWidth() * 1.0f / bitmap.getWidth();
        scaleY = getHeight() * 1.0f / bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.setScale(scaleX, scaleY);
        Bitmap bitmapResult = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return bitmapResult;
    }

    /**
     * 初始化滑块的宽高
     */
    private void initSlideWH() {
        mSlideWith = (int) (mBitmap.getWidth() * mSlideWithScale);
        float scale = mSlideWith * 1.0f / mResourceBitmap.getWidth();
        mSlideHeight = (int) (mResourceBitmap.getHeight() * scale);
    }

    /**
     * 初始化滑块阴影位置
     */
    private void initShadeRandomXY() {
        mShadeRandomX = (int) (mBitmap.getWidth() / 2 + (mBitmap.getWidth() / 2) * Math.random() - mSlideWith);
        mShadeRandomY = (int) (Math.random() * (mBitmap.getHeight() - mSlideHeight));
        if (mShadeRandomX + mSlideWith > mBitmap.getWidth() || mShadeRandomY + mSlideHeight > mBitmap.getHeight()) {
            initShadeRandomXY();
            return;
        }
    }

    /**
     * 绘制图片
     *
     * @param mDrawBitmap 绘制的图片
     * @return
     */
    private Bitmap drawImage(Bitmap mDrawBitmap) {
        //绘制图片,大小缩放到mSlideWith，mSlideHeight
        Bitmap showBitmap;
        if (mResourceBitmap != null) {
            showBitmap = handleBitmap(mResourceBitmap, mSlideWith, mSlideHeight);
        } else {
            showBitmap = handleBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.star_shade), mSlideWith, mSlideHeight);
        }
        Bitmap resultBitmap = Bitmap.createBitmap(mSlideWith, mSlideHeight, Bitmap.Config.ARGB_8888);
        Paint paint = new Paint(Color.GRAY);
        paint.setAntiAlias(true);
        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(showBitmap, new Rect(0, 0, mSlideWith, mSlideHeight), new Rect(0, 0, mSlideWith, mSlideHeight), paint);
        //选择交集 去上层图片
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
        canvas.drawBitmap(mDrawBitmap, new Rect(0, 0, mSlideWith, mSlideHeight), new Rect(0, 0, mSlideWith, mSlideHeight), paint);

        return resultBitmap;
    }

    private Bitmap handleBitmap(Bitmap bitmap, int width, int height) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        float sx = (float) width / w;
        float sy = (float) height / h;
        Matrix matrix = new Matrix();
        matrix.postScale(sx, sy);
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, w,
                h, matrix, true);
        return resizeBmp;
    }


    public void setSlideProgress(int progress) {
        mSlideMoveDistance = (int) ((mBitmap.getWidth() - mSlideWith)*1.0f / 100 * progress);
        if (mSlideMoveDistance > mBitmap.getWidth() - mSlideWith) {
            mSlideMoveDistance = mBitmap.getWidth() - mSlideWith;
        }
        postInvalidate();
    }

    /**
     * 检查是否验证成功
     * @param progress seekbar位置
     */
    public void checkSlidePoint(int progress) {
        if (mSlideListener != null) {
            mSlideMoveDistance = (int) ((mBitmap.getWidth()- mSlideWith)*1.0f / 100 * progress);
            if (Math.abs(mSlideMoveDistance - mShadeRandomX) <= diviation) {
                mSlideListener.success();
            } else {
                mSlideListener.error();
            }
        }
    }

    public void reset() {
        isReset = true;
        mSlideMoveDistance = 0;
        postInvalidate();
    }

    public void setSlideListener(SlideListener slideListener) {
        this.mSlideListener = slideListener;
    }

    public interface SlideListener {
        void success();

        void error();
    }

}

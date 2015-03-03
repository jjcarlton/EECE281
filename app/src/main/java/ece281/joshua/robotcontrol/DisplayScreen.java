package ece281.joshua.robotcontrol;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.io.IOException;

/**
 * Created by Joshua on 2015-03-03.
 */
public class DisplayScreen extends View {

    Paint paint;
    int x = 10;

    public DisplayScreen(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
        paint = new Paint();
    }


    public void onDraw(Canvas canvas){
        super.onDraw(canvas);

        x += 10;

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLUE);

        Rect rect = new Rect(x, 10, x + 100, 100);

        canvas.drawRect(rect, paint);


    }

    public void moveRect(){
        x+= 10;
    }




}

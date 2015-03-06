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
    int distance;

    public DisplayScreen(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
        paint = new Paint();
    }

    public void updateDisplay(int distance){
        this.distance = distance;
        this.invalidate();
    }


    public void onDraw(Canvas canvas){

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.GREEN);
        Rect background = new Rect(0, 0, 500, 500);
        Rect foreground = new Rect(0, 0, 500, distance);

        super.onDraw(canvas);

        canvas.drawRect(background, paint);
        canvas.drawRect(foreground, paint);

    }

    public void moveRect(){
        x+= 10;
    }




}

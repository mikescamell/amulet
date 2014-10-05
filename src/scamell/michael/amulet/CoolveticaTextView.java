package scamell.michael.amulet;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class CoolveticaTextView extends TextView {

    private final Context context;

    public CoolveticaTextView(Context context) {
        super(context);
        this.context = context;
        isInEditMode();
        Typeface tfs = Typeface.createFromAsset(context.getAssets(),
                "fonts/coolvetica.ttf");
        setTypeface(tfs);
    }

    public CoolveticaTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        isInEditMode();
        Typeface tfs = Typeface.createFromAsset(context.getAssets(),
                "fonts/coolvetica.ttf");
        setTypeface(tfs);
    }

    public CoolveticaTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        isInEditMode();
        Typeface tfs = Typeface.createFromAsset(context.getAssets(),
                "fonts/coolvetica.ttf");
        setTypeface(tfs);

    }
}

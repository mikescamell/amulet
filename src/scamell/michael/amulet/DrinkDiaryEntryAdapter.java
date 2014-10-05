package scamell.michael.amulet;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DrinkDiaryEntryAdapter extends ArrayAdapter<DrinkDiaryEntry> {

    private final Activity context;
    private DrinkDiaryEntries dDE;
    private View entryView;

    public DrinkDiaryEntryAdapter(Activity context, DrinkDiaryEntries dDE) {
        super(context, R.layout.list_item_drink_diary_entry, dDE.getEntries());
        this.context = context;
        this.dDE = dDE;
    }

    public void updateView(DrinkDiaryEntries dDE) {
        this.dDE = dDE;
        notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = new ViewHolder();
        entryView = convertView;
        if (entryView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            entryView = inflater.inflate(R.layout.list_item_drink_diary_entry, parent, false);
            viewHolder.drinkNameLine = (TextView) entryView.findViewById(R.id.firstLine);
            viewHolder.dateLine = (TextView) entryView.findViewById(R.id.secondLine);
            viewHolder.unitsLine = (TextView) entryView.findViewById(R.id.units_textView);
            viewHolder.imageView = (ImageView) entryView.findViewById(R.id.icon);
            entryView.setTag(viewHolder);
        }
        ViewHolder holder = (ViewHolder) entryView.getTag();
        holder.drinkNameLine.setText(dDE.getEntry(position).drinkName);
        holder.dateLine.setText(DateAndTime.setDateAndTimeForApp(dDE.getEntry(position).date));
        holder.unitsLine.setText(context.getResources().getString(R.string.units, dDE.getEntry(position).units));
        setImage(holder.imageView, position);
        entryView.setBackgroundResource(Color.TRANSPARENT);

        entryView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dDE.getEntry(position).isSelected) {
                    dDE.getEntry(position).isSelected = false;
                }
                entryView.setBackgroundResource(Color.TRANSPARENT);
            }
        });

        return entryView;
    }

    public void setImage(ImageView imageView, int indexPos) {
        if (dDE.getEntry(indexPos).drinkType.equals("Beer") || dDE.getEntry(indexPos).drinkType.equals("Cider")) {
            imageView.setImageResource(R.drawable.ic_beer);
        } else if (dDE.getEntry(indexPos).drinkType.equals("Bottled Beer")) {
            imageView.setImageResource(R.drawable.ic_beer_bottle);
        } else if (dDE.getEntry(indexPos).drinkType.equals("Wine")) {
            imageView.setImageResource(R.drawable.ic_wine);
        } else if (dDE.getEntry(indexPos).drinkType.equals("Alcopop")) {
            imageView.setImageResource(R.drawable.ic_alcopop);
        } else if (dDE.getEntry(indexPos).drinkType.equals("Champagne")) {
            imageView.setImageResource(R.drawable.ic_champagne);
        } else if (dDE.getEntry(indexPos).drinkType.equals("Spirit")) {
            imageView.setImageResource(R.drawable.ic_spirit);
        }
    }

    /**
     * imporved list scrolling performance by hoilding refs to relevant views
     * http://www.vogella.com/tutorials/AndroidListView/article.html#adapterperformance_holder
     */
    public static class ViewHolder {
        public TextView drinkNameLine;
        public TextView dateLine;
        public TextView unitsLine;
        public ImageView imageView;
    }
}
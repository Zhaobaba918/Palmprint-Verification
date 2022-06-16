package top.sun1999;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;


public class PalmprintAdapter extends RecyclerView.Adapter<PalmprintAdapter.ViewHolder> {
    // 活动适配器，将活动卡片填充进 RecycleView 中
    private ArrayList<String> nameList;         // 活动信息
    private RecyclerOnClickListener onClickListener;  // 监听器，监听点击事件
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // 要展示的活动信息
        private TextView name;        // 活动名称

        public ViewHolder(View view, RecyclerOnClickListener listener) {
            super(view);
            // Define click listener for the ViewHolder's View
            name = view.findViewById(R.id.item_name);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClick(getAdapterPosition());
                }
            });  // 设置监听器，点击后向 Adapter 传递点击的位置
        }

        public TextView getName() {
            return name;
        }
    }

    /**
     * Initialize the data set of the Adapter.
     *
     * @param names String[] containing the data to populate views to be used
     * by RecyclerView.
     */
    public PalmprintAdapter(ArrayList<String> names, RecyclerOnClickListener onClickListener) {
        this.nameList = names;
        this.onClickListener = onClickListener;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.palmprint_item, viewGroup, false);
        return new ViewHolder(view, this.onClickListener);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // 从 data set 中获取这个位置要显示的内容
        viewHolder.getName().setText(nameList.get(position));
    }

    // Return the size of your data set (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return nameList.size();
    }

}

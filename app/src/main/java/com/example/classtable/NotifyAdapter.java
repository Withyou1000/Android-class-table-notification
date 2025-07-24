package com.example.classtable;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.classtable.bean.Notify;
import com.example.classtable.sqlite.NotifyDBHelper;

import java.util.ArrayList;
import java.util.List;

public class NotifyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_FIRST_ITEM = 0;
    private static final int TYPE_NORMAL_ITEM = 1;
    private List<Notify> list = new ArrayList<>();
    private Context context;
    private NotifyDBHelper notifyDBHelper;
    private OnItemLongClickListener onItemLongClickListener;
    private OnItemClickListener onItemClickListener;
    private SharedPreferences firstItem;

    public NotifyAdapter(Context context) {
        this.context = context;
        notifyDBHelper = NotifyDBHelper.getInstance(context);
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? TYPE_FIRST_ITEM : TYPE_NORMAL_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_FIRST_ITEM) {
            View view = inflater.inflate(R.layout.notify_first_layout, parent, false);
            return new FirstItemViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.notify_item_layout, parent, false);
            return new MyViewHolder(view);
        }
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FirstItemViewHolder) {
            // 处理 FirstItemViewHolder
            firstItem = context.getSharedPreferences("firstItem", Context.MODE_PRIVATE);
            FirstItemViewHolder firstHolder = (FirstItemViewHolder) holder;
            firstHolder.everyTime.setText(firstItem.getString("everyTime", "20 min"));
            firstHolder.switchButton.setChecked(firstItem.getInt("switch", 0) == 1);
            if (firstItem.getInt("vibrate", 1) == 1) {
                firstHolder.tvFibrate.setVisibility(View.VISIBLE);
            } else {
                firstHolder.tvFibrate.setVisibility(View.GONE);
            }
            if (firstItem.getInt("sound", 1) == 1) {
                firstHolder.tvSound.setVisibility(View.VISIBLE);
            } else {
                firstHolder.tvSound.setVisibility(View.GONE);
            }
            // 处理开关状态变化
            firstHolder.switchButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int is_switch = isChecked ? 1 : 0;
                SharedPreferences.Editor editor = firstItem.edit();
                editor.putInt("switch", is_switch);
                editor.apply();
                if (is_switch == 0) {
                    int flag = 0;
                    List<Notify> notifys = notifyDBHelper.queryall();
                    for (Notify notify : notifys) {
                        if (notify.getSwitch_on() == 1) {
                            flag = 1;
                            break;
                        }
                    }
                    if (flag == 0) {
                        context.getApplicationContext().stopService(new Intent(context.getApplicationContext(), MyService.class));
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(new Intent(context, MyService.class));
                    }
                }
            });
            firstHolder.itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.OnItemClick();
                }
            });
        } else {
            // 处理 MyViewHolder
            MyViewHolder normalHolder = (MyViewHolder) holder;
            int dataPosition = position - 1; // 调整 position
            if (dataPosition >= 0 && dataPosition < list.size()) { // 检查 position 是否有效
                Notify item = list.get(dataPosition);
                normalHolder.tvTime.setText(item.getTime());
                normalHolder.tvMessage.setText(item.getInfo());
                normalHolder.switchButton.setChecked(item.getSwitch_on() == 1);
                if (item.getIfVibrate() == 1) {
                    normalHolder.tvFibrate.setVisibility(View.VISIBLE);
                } else {
                    normalHolder.tvFibrate.setVisibility(View.GONE);
                }
                if (item.getIfSound() == 1) {
                    normalHolder.tvSound.setVisibility(View.VISIBLE);
                } else {
                    normalHolder.tvSound.setVisibility(View.GONE);
                }
                // 处理开关状态变化
                normalHolder.switchButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    int is_switch = isChecked ? 1 : 0;
                    Cursor cursor = notifyDBHelper.change_switch(item.getId(), is_switch); // 使用 item.getId()
                    if (is_switch == 0) {
                        int flag = 0;
                        while (cursor.moveToNext()) {
                            if (cursor.getInt(2) == 1) {
                                flag = 1;
                                break;
                            }
                        }
                        cursor.close();
                        if (flag == 0 && firstItem.getInt("switch", 0) == 0) {
                            context.getApplicationContext().stopService(new Intent(context.getApplicationContext(), MyService.class));
                        }
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(new Intent(context, MyService.class));
                        }
                    }
                });
                // 处理长按事件
                normalHolder.itemView.setOnLongClickListener(v -> {
                    if (onItemLongClickListener != null) {
                        onItemLongClickListener.onItemLongClick(position); // 使用 dataPosition
                    }
                    return true;
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        // 第一项始终是 FirstItemViewHolder
        return list.size() + 1;
    }

    public void setData(List<Notify> listnotifys) {
        if (listnotifys == null) {
            return;
        }
        list.clear();
        list.addAll(listnotifys);
        notifyDataSetChanged();
    }

    //使用setData会刷新全部，长按过快会没反应，单独刷新删除位置的视图
    //传入的position已经是矫正过的对于list来说
    public void removeItem(int position) {
        list.remove(position);
        notifyItemRemoved(position + 1);
        // 更新从删除位置开始的所有后续项
        int remainingItems = list.size() - position;
        notifyItemRangeChanged(position + 1, remainingItems);
    }

    public void setItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    public void setItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }

    public interface OnItemClickListener {
        void OnItemClick();
    }

    static class FirstItemViewHolder extends RecyclerView.ViewHolder {
        TextView everyTime;
        TextView tvMessage;
        Switch switchButton;
        TextView tvSound;
        TextView tvFibrate;

        // 第一个元素的 ViewHolder
        public FirstItemViewHolder(@NonNull View itemView) {
            super(itemView);
            everyTime = itemView.findViewById(R.id.notify_time);
            tvMessage = itemView.findViewById(R.id.tv_info);
            switchButton = itemView.findViewById(R.id.swtich_on);
            tvSound = itemView.findViewById(R.id.tv_sound);
            tvFibrate = itemView.findViewById(R.id.tv_fibrate);
        }
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime;
        TextView tvMessage;
        Switch switchButton;
        TextView tvSound;
        TextView tvFibrate;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.notify_time);
            tvMessage = itemView.findViewById(R.id.tv_info);
            switchButton = itemView.findViewById(R.id.swtich_on);
            tvSound = itemView.findViewById(R.id.tv_sound);
            tvFibrate = itemView.findViewById(R.id.tv_fibrate);
        }
    }
}
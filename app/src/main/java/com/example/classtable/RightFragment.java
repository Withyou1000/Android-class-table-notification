package com.example.classtable;

import static androidx.core.content.ContextCompat.startForegroundService;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.classtable.bean.Notify;
import com.example.classtable.databinding.FragmentRightBinding;
import com.example.classtable.sqlite.NotifyDBHelper;

import java.nio.channels.Channel;
import java.util.List;

public class RightFragment extends Fragment implements View.OnClickListener {
    NotifyDBHelper notifyDBHelper;
    private FragmentRightBinding binding; // 使用 ViewBinding
    private RecyclerView notifyItmesList;
    private NotifyAdapter notifyAdapter;
    private List<Notify> notifys;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRightBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        notifyDBHelper = NotifyDBHelper.getInstance(requireContext());
        notifyDBHelper.openRead();
        notifyDBHelper.openWrite();
        notifyItmesList = binding.notifyItmesList;
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        notifyItmesList.setLayoutManager(layoutManager);
        notifyAdapter = new NotifyAdapter(requireContext());
        notifyItmesList.setAdapter(notifyAdapter);
        binding.explain.setOnClickListener(this);
        binding.add.setOnClickListener(this);
        binding.explain.setOnClickListener(this);
        notifyAdapter.setItemLongClickListener(new NotifyAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(int position) {
                int id = notifys.get(position-1).getId();
                notifyDBHelper.delete_notify(id);
                notifyAdapter.removeItem(position-1);
            }
        });
        notifyAdapter.setItemClickListener(new NotifyAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick() {
                Intent intent = new Intent(getActivity(), WaterFloatActivity.class);
                startActivity(intent);
            }
        });

    }


    @Override
    public void onResume() {
        super.onResume();
      /*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE,getActivity().getPackageName());
            intent.putExtra(Settings.EXTRA_CHANNEL_ID, "jjc");
            getActivity().startActivity(intent);
        }*/
        notifys = notifyDBHelper.queryall();
        notifyAdapter.setData(notifys);
      /*  getActivity().stopService(new Intent(getActivity(),MyService.class));
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("widget_prefs", Context.MODE_PRIVATE);
        boolean isEnabled = sharedPreferences.getBoolean("widget_enabled", false);*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //通知提醒
            SharedPreferences firstItem = getActivity().getSharedPreferences("firstItem", Context.MODE_PRIVATE);
            int flag = 0;
            if (notifys.size() != 0) {
                for (Notify item : notifys) {
                    if (item.getSwitch_on() == 1) {
                        flag = 1;
                    }
                }
            }
            if (flag==1 || firstItem.getInt("switch", 0) == 1) {
                Intent serviceIntent = new Intent(getActivity(), MyService.class);
                startForegroundService(getActivity(), serviceIntent);
            }
        }
      /*  if(isEnabled) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getActivity());
            // 获取当前已添加的所有小部件 ID（数组）
            int[] widgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(
                    getActivity(), TimeWidget.class
            ));
            // 直接取第一个 ID（假设只有一个小部件）
            int widgetId = widgetIds[0];
            // 发送广播（携带 widgetId）
            Intent intent = new Intent(TimeWidget.ACTION_UPDATE);
            getActivity().sendBroadcast(intent);
        Intent intent = new Intent("fragment.jjc.notify");
        intent.setComponent(new ComponentName("com.example.classtable", "com.example.classtable.NotifyReceiver"));
        intent.setClassName("com.example.classtable", "com.example.classtable.NotifyReceiver");
        getActivity().sendBroadcast(intent);*/
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.explain) {
            Intent intent = new Intent(getActivity(), ExplainActivity.class);
            startActivity(intent);
        }
        if (v.getId() == R.id.add) {
            Intent intent = new Intent(requireContext(), AddNotifyActivity.class);
            startActivity(intent);
        }
    }
}
package com.example.wemeet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.wemeet.pojo.InputTips;

import java.util.List;

public class InputTipsAdapter extends BaseAdapter {

    private Context mContext;
    private List<InputTips> mInputTips;

    public InputTipsAdapter(Context context, List<InputTips> inputTips) {
        mContext = context;
        mInputTips = inputTips;
    }

    @Override
    public int getCount() {
        if (mInputTips != null) {
            return mInputTips.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int i) {
        if (mInputTips != null) {
            return mInputTips.get(i);
        }
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Holder holder;
        if (view == null) {
            holder = new Holder();
            view = LayoutInflater.from(mContext).inflate(R.layout.input_tips, null);
            holder.mName = (TextView) view.findViewById(R.id.search_name);
            holder.mAddress = (TextView) view.findViewById(R.id.search_address);
            view.setTag(holder);
        } else{
            holder = (Holder)view.getTag();
        }
        if(mInputTips == null){
            return view;
        }

        holder.mName.setText(mInputTips.get(i).mName);
        String address = mInputTips.get(i).mAddress;
        if(address == null || address.equals("")){
            holder.mAddress.setVisibility(View.GONE);
        }else{
            holder.mAddress.setVisibility(View.VISIBLE);
            holder.mAddress.setText(address);
        }
        return view;
    }

    static class Holder {
        TextView mName;
        TextView mAddress;
    }
}

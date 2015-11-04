package com.rmscore.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Rinaldi on 28/10/2015.
 */
public class SimpleArrayAdapter extends ArrayAdapter<String> {

    public enum eTextAlign {
        right, left, center
    }

    Context context;
    int layoutResourceId;
    String [] dataList = null;
    eTextAlign textAlign;

    private static class ItemViewHolder {
        TextView txtItem;
    }

    public SimpleArrayAdapter(Context context, int layoutResourceId, String[] dataParam, eTextAlign textAlignParam) {
        super(context, layoutResourceId, dataParam);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.dataList = dataParam;
        textAlign = textAlignParam;
    }

    public SimpleArrayAdapter(Context context, int layoutResourceId, ArrayList<String> dataParam, eTextAlign textAlignParam) {
        super(context, layoutResourceId, dataParam);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.dataList = dataParam.toArray(new String[dataParam.size()]);
        textAlign = textAlignParam;
    }

    @Override
    public View getDropDownView(int position, View cnvtView, ViewGroup prnt) {
        return getCustomView(position, cnvtView, prnt);
    }

    @Override
    public View getView(int pos, View cnvtView, ViewGroup prnt) {
        return getCustomView(pos, cnvtView, prnt);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {
        LinearLayout view = (LinearLayout) convertView;
        ItemViewHolder vh = new ItemViewHolder();

        if (view == null) {
            LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //LayoutInflater vi = ((Activity) context).getLayoutInflater();
            view = (LinearLayout) vi.inflate(layoutResourceId, null);
        }

        switch (textAlign) {
            case center:
                view.setGravity(Gravity.CENTER_VERTICAL + Gravity.CENTER);
                break;
            case left:
                view.setGravity(Gravity.CENTER_VERTICAL + Gravity.LEFT);
                break;
            case right:
                view.setGravity(Gravity.CENTER_VERTICAL + Gravity.RIGHT);
                break;
            default:
                view.setGravity(Gravity.CENTER_VERTICAL + Gravity.LEFT);
                break;
        }

        vh.txtItem = (TextView) view.getChildAt(0);

        view.setTag(vh);

        vh.txtItem.setText(dataList[position]);
        //vh.btnSend.setOnClickListener(new OnBtnSendClick(position));

        return view;
    }
}

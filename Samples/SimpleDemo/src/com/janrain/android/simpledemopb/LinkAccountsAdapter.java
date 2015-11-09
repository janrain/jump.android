package com.janrain.android.multidex.simpledemopb2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.janrain.android.multidex.simpledemopb2.LinkData;
import com.janrain.android.multidex.simpledemopb2.R;

import java.util.ArrayList;

public class LinkAccountsAdapter extends BaseAdapter {
    private static ArrayList<LinkData> linkArrayList;
    private LayoutInflater mInflater;

    public LinkAccountsAdapter(Context context, ArrayList<LinkData> results) {
        linkArrayList = results;
        mInflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return linkArrayList.size();
    }

    public Object getItem(int position) {
        return linkArrayList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.linkaccounts_listview_row, null);
            holder = new ViewHolder();
            holder.mIdentifier = (TextView) convertView.findViewById(R.id.row_profile_linkaccount_label);
            holder.mDomain = (TextView) convertView.findViewById(R.id.row_profile_identifier_label);
            holder.mVerifiedEmail = (TextView) convertView.findViewById(R.id.row_profile_email_label);
            holder.unlinkAccount = (ImageView) convertView.findViewById(R.id.row_unlink_btn);
            holder.unlinkAccount.setTag(position);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.mIdentifier.setText(linkArrayList.get(position).getIdentifier());
        holder.mVerifiedEmail.setText(linkArrayList.get(position).getVerifiedEmail());
        holder.mDomain.setText(linkArrayList.get(position).getDomainName());
        return convertView;
    }

    public static class ViewHolder {
        TextView mIdentifier;
        TextView mDomain;
        TextView mVerifiedEmail;
        ImageView unlinkAccount;
    }
}
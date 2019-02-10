package com.felind.qrs;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;


public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    List<CodeList> codeLists;
    Context context;

    public RecyclerViewAdapter(List<CodeList> codeLists, Context context) {
        this.codeLists = codeLists;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CodeList codeList = codeLists.get(position);
        holder.textView_Id.setText(String.valueOf(codeList.get_Id()));
        holder.textViewCode.setText(codeList.getCode());
        holder.textViewType.setText(codeList.getType());
        Linkify.addLinks(holder.textViewCode, Linkify.ALL);
    }

    @Override
    public int getItemCount() {
        return codeLists.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView textView_Id;
        public TextView textViewCode;
        public TextView textViewType;

        public TextView getTextView_Id() {
            return textView_Id;
        }

        public ViewHolder(View itemView) {
            super(itemView);
            textView_Id = itemView.findViewById(R.id.textView_Id);
            textViewCode = itemView.findViewById(R.id.textViewCode);
            textViewType = itemView.findViewById(R.id.textViewType);
        }
    }
}


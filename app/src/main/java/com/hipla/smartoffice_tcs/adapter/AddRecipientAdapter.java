package com.hipla.smartoffice_tcs.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.hipla.smartoffice_tcs.R;
import com.hipla.smartoffice_tcs.model.AddRecipientsModel;

import java.util.List;


public class AddRecipientAdapter extends RecyclerView.Adapter<AddRecipientAdapter.MyHolderView> {

    List<AddRecipientsModel> recipientList ;
    AddRecipientAdapter.ManageRecipientCallback manageRowCallback ;


    public AddRecipientAdapter(AddRecipientAdapter.ManageRecipientCallback callback  , List<AddRecipientsModel> recipientList){
        this.recipientList = recipientList;
        this.manageRowCallback = callback ;
    }

    @Override
    public MyHolderView onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new MyHolderView(inflater.inflate(R.layout.add_reciepients_row,parent, false));
    }

    @Override
    public void onBindViewHolder(MyHolderView holder, int position) {

        AddRecipientsModel model = recipientList.get(position);
        holder.name.setText(model.getName());
        holder.email.setText(model.getEmail());

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class MyHolderView extends RecyclerView.ViewHolder{

        public EditText name, email;
        public ImageView iv_add_recipients , iv_minus_recipients ;

        public MyHolderView(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.et_recipients_name);
            email = itemView.findViewById(R.id.et_recipients_email);

            iv_add_recipients = itemView.findViewById(R.id.iv_add_recipients);
            iv_minus_recipients = itemView.findViewById(R.id.iv_minus_recipients);

            }
    }



    public interface ManageRecipientCallback{

        public void onAddRecipient();
        public void onRemoveRecipient(int position);

    }


    public void add(){
        Log.d("jos", "add: ");
        manageRowCallback.onAddRecipient();
    }

    public void remove(int position){
        Log.d("jos", "remove: ");
        if(position>0)
            manageRowCallback.onRemoveRecipient(position);

    }
}

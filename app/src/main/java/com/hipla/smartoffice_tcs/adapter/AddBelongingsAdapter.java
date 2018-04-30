package com.hipla.smartoffice_tcs.adapter;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.hipla.smartoffice_tcs.R;
import com.hipla.smartoffice_tcs.databinding.AddBelongingRowBinding;
import com.hipla.smartoffice_tcs.model.AddBelongingsModel;

import java.util.List;

public class AddBelongingsAdapter extends RecyclerView.Adapter<AddBelongingsAdapter.MyViewHolder>  {

    List<AddBelongingsModel> belongingsList;
    AddBelongingRowBinding binding ;
    public AddBelongingsAdapter.ManageRowCallback manageRowCallback ;

    public AddBelongingsAdapter(ManageRowCallback callback ,List<AddBelongingsModel> belongingsList) {
        this.belongingsList = belongingsList ;
        this.manageRowCallback = callback;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        binding = DataBindingUtil.inflate(inflater, R.layout.add_belonging_row, parent, false);
        return  new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        AddBelongingsModel model= belongingsList.get(position);
        holder.name.setText(model.getName());
        holder.value.setText(model.getValue());

        holder.iv_add_belonging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                add();
            }
        });

        holder.iv_minus_belonging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                remove(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return belongingsList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public EditText name, value;
        public ImageView iv_add_belonging , iv_minus_belonging ;


        public MyViewHolder(AddBelongingRowBinding binding) {
            super(binding.getRoot());
           AddBelongingsAdapter.this.binding  = binding ;
           bindObject();
        }
        public void bindObject(){
            name = AddBelongingsAdapter.this.binding.etBelongingName;
            value = AddBelongingsAdapter.this.binding.etBelongingValue;
            iv_add_belonging = AddBelongingsAdapter.this.binding.ivAddBelonging;
            iv_minus_belonging = AddBelongingsAdapter.this.binding.ivMinusBelonging;
        }
    }

    public interface ManageRowCallback{

        public void onRowAdd();
        public void onRowRemove(int position);

    }
    public void add(){
        Log.d("jos", "add: ");
        manageRowCallback.onRowAdd();


    }
    public void remove(int position){
        Log.d("jos", "remove: ");
        if(position>0)
        manageRowCallback.onRowRemove(position);

    }
}

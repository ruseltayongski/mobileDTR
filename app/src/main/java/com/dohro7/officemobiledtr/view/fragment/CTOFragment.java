package com.dohro7.officemobiledtr.view.fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dohro7.officemobiledtr.R;
import com.dohro7.officemobiledtr.adapter.CtoAdapter;
import com.dohro7.officemobiledtr.model.CtoModel;
import com.dohro7.officemobiledtr.model.OfficeOrderModel;
import com.dohro7.officemobiledtr.utility.DateTimeUtility;
import com.dohro7.officemobiledtr.viewmodel.CtoViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class CTOFragment extends Fragment {

    private RecyclerView recyclerView;
    private CtoAdapter ctoAdapter;
    private CtoViewModel ctoViewModel;
    private LinearLayoutManager layoutManager;
    private boolean swipeBack;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        ctoViewModel = ViewModelProviders.of(this).get(CtoViewModel.class);
        ctoAdapter = new CtoAdapter();
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.cto_fragment_layout, container, false);
        recyclerView = view.findViewById(R.id.cto_recycler_view);
        recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public int convertToAbsoluteDirection(int flags, int layoutDirection) {
                if (swipeBack) {
                    swipeBack = false;
                    return 0;
                }
                return super.convertToAbsoluteDirection(flags, layoutDirection);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                CtoModel ctoModel = ctoViewModel.getListLiveData().getValue().get(viewHolder.getAdapterPosition());
//                if (ctoModel != null) {
//                    //ctoViewModel.deleteCto(ctoModel);
//                }
                displayDeleteWarningDialog(ctoModel);
            }
        };


        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(ctoAdapter);

        view.findViewById(R.id.fab_cto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayAddCtoDialog();
            }
        });

        ctoViewModel.getListLiveData().observe(this, new Observer<List<CtoModel>>() {
            @Override
            public void onChanged(List<CtoModel> ctoModels) {
                ctoAdapter.setList(ctoModels);
            }
        });

        ctoViewModel.getUploadMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                List<String> array_message = Arrays.asList(getContext().getResources().getStringArray(R.array.no_retry_message));
                if (!array_message.contains(s)) {
                    Snackbar snackbar = Snackbar.make(view.findViewById(R.id.root), s, Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction("RETRY", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ctoViewModel.uploadCto();
                        }
                    });
                    snackbar.show();
                    return;
                }

                Snackbar snackbar = Snackbar.make(view.findViewById(R.id.root), s, Snackbar.LENGTH_SHORT);
                snackbar.setText(s);
                snackbar.show();

            }
        });
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.absence_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.upload) {
            if (ctoViewModel.getListLiveData().getValue().size() > 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Are you sure you want to upload?");
                builder.setNegativeButton("Cancel", null);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ctoViewModel.uploadCto(); ///////
                    }
                });
                Dialog dialog = builder.create();
                dialog.show();
            } else {
                ctoViewModel.getUploadMessage().setValue("Nothing to upload");
            }


            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void displayAddCtoDialog() {
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_add_cto);
        final TextView dialogCtoFrom = dialog.findViewById(R.id.dialog_cto_from);
        final TextView dialogCtoTo = dialog.findViewById(R.id.dialog_cto_to);

        final Calendar calendarFrom = Calendar.getInstance();
        final Calendar calendarTo = Calendar.getInstance();
        dialogCtoFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatePickerDialog datePickerDialog = new DatePickerDialog(v.getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        dialogCtoFrom.setText(year + "/" + DateTimeUtility.twoDigitFormat(month + 1) + "/" + DateTimeUtility.twoDigitFormat(dayOfMonth));
                        calendarTo.set(year, month, dayOfMonth);
                        //datePickerDialog.dismiss();
                    }
                }, calendarFrom.get(Calendar.YEAR), calendarFrom.get(Calendar.MONTH), calendarFrom.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });


        dialogCtoTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatePickerDialog datePickerDialog = new DatePickerDialog(v.getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        dialogCtoTo.setText(year + "/" + DateTimeUtility.twoDigitFormat(month + 1) + "/" + DateTimeUtility.twoDigitFormat(dayOfMonth));
                        //datePickerDialog.dismiss();
                    }
                }, calendarTo.get(Calendar.YEAR), calendarTo.get(Calendar.MONTH), calendarTo.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.getDatePicker().setMinDate(calendarTo.getTimeInMillis());
                datePickerDialog.show();
            }
        });

        dialog.findViewById(R.id.dialog_btn_add_cto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(dialogCtoFrom.getText().toString().trim().isEmpty()){
                    Toast.makeText(getContext(), "Please specify date from", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(dialogCtoTo.getText().toString().trim().isEmpty()){
                    Toast.makeText(getContext(), "Please specify date to", Toast.LENGTH_SHORT).show();
                    return;
                }
                String inclusive_date = dialogCtoFrom.getText().toString() + "-" + dialogCtoTo.getText().toString();
                CtoModel ctoModel = new CtoModel(0, inclusive_date);
                ctoViewModel.insertCto(ctoModel);
                dialog.dismiss();
            }
        });

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        dialog.getWindow().setAttributes(lp);
        dialog.show();
    }

    private void displayDeleteWarningDialog(CtoModel ctoModel1) {
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_delete_warning);
        dialog.setCancelable(false);
        recyclerView.setAdapter(ctoAdapter);
        final TextView itemDescription = dialog.findViewById(R.id.dialog_delete_message2);
        final CtoModel ctoModel=ctoModel1;

        itemDescription.setText("( CTO dated " + ctoModel.inclusive_date +" )");

        dialog.findViewById(R.id.dialog_delete_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.setAdapter(ctoAdapter);
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.dialog_delete_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ctoViewModel.deleteCto(ctoModel);
                dialog.dismiss();
            }
        });
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        dialog.getWindow().setAttributes(lp);
        dialog.show();
    }
}

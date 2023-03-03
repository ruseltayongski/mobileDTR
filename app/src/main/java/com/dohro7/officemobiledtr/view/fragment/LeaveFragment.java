package com.dohro7.officemobiledtr.view.fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
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
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.dohro7.officemobiledtr.R;
import com.dohro7.officemobiledtr.adapter.LeaveAdapter;
import com.dohro7.officemobiledtr.model.LeaveModel;
import com.dohro7.officemobiledtr.utility.DateTimeUtility;
import com.dohro7.officemobiledtr.viewmodel.LeaveViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class LeaveFragment extends Fragment {
    private RecyclerView recyclerView;
    private LeaveViewModel leaveViewModel;
    private LeaveAdapter leaveAdapter;
    private LinearLayoutManager layoutManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        leaveAdapter = new LeaveAdapter();
        leaveViewModel = ViewModelProviders.of(this).get(LeaveViewModel.class);
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.absence_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.upload) {
            if (leaveViewModel.getListLiveData().getValue().size() > 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Are you sure you want to upload?");
                builder.setNegativeButton("Cancel", null);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        leaveViewModel.uploadLeave();
                    }
                });
                Dialog dialog = builder.create();
                dialog.show();
            } else {
                leaveViewModel.getUploadMessage().setValue("Nothing to upload");
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.leave_fragment_layout, container, false);
        recyclerView = view.findViewById(R.id.leave_recycler_view);
        recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

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
                LeaveModel leaveModel = leaveViewModel.getListLiveData().getValue().get(viewHolder.getAdapterPosition());
                Log.e("delete", "leave type" + leaveModel.type + "date " + leaveModel.inclusive_date);
                displayDeleteWarningDialog(leaveModel);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(leaveAdapter);

        view.findViewById(R.id.fab_leave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayAddLeaveDialog();
            }
        });

        leaveViewModel.getListLiveData().observe(this, new Observer<List<LeaveModel>>() {
            @Override
            public void onChanged(List<LeaveModel> leaveModels) {
                leaveAdapter.setList(leaveModels);
            }
        });
        leaveViewModel.getUploadMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                List<String> array_message = Arrays.asList(getContext().getResources().getStringArray(R.array.no_retry_message));
                if (!array_message.contains(s)) {
                    Snackbar snackbar = Snackbar.make(view.findViewById(R.id.root), s, Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction("RETRY", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            leaveViewModel.uploadLeave();
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

    public void displayAddLeaveDialog() {
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_add_leave);

        final Spinner dialogLeaveType = dialog.findViewById(R.id.dialog_leave_type);
        final TextView dialogLeaveFrom = dialog.findViewById(R.id.dialog_leave_from);
        final TextView dialogLeaveTo = dialog.findViewById(R.id.dialog_leave_to);

        ArrayAdapter spinnerAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item_layout);
        String[] leaveArray = getContext().getResources().getStringArray(R.array.leave_type);
        spinnerAdapter.addAll(leaveArray);
        dialogLeaveType.setAdapter(spinnerAdapter);

        final Calendar calendarFrom = Calendar.getInstance();
        final Calendar calendarTo = Calendar.getInstance();

        dialogLeaveFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatePickerDialog datePickerDialog = new DatePickerDialog(v.getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        dialogLeaveFrom.setText(year + "/" + DateTimeUtility.twoDigitFormat(month + 1) + "/" + DateTimeUtility.twoDigitFormat(dayOfMonth));
                        calendarTo.set(year, month, dayOfMonth);
                        //datePickerDialog.dismiss();
                    }
                }, calendarFrom.get(Calendar.YEAR), calendarFrom.get(Calendar.MONTH), calendarFrom.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });


        dialogLeaveTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatePickerDialog datePickerDialog = new DatePickerDialog(v.getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        dialogLeaveTo.setText(year + "/" + DateTimeUtility.twoDigitFormat(month + 1) + "/" + DateTimeUtility.twoDigitFormat(dayOfMonth));
                        //datePickerDialog.dismiss();
                    }
                }, calendarTo.get(Calendar.YEAR), calendarTo.get(Calendar.MONTH), calendarTo.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.getDatePicker().setMinDate(calendarTo.getTimeInMillis());
                datePickerDialog.show();
            }
        });

        dialog.findViewById(R.id.dialog_btn_add_leave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dialogLeaveFrom.getText().toString().trim().isEmpty()){
                    Toast.makeText(getContext(), "Please specify date from", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(dialogLeaveTo.getText().toString().trim().isEmpty()){
                    Toast.makeText(getContext(), "Please specify date to", Toast.LENGTH_SHORT).show();
                    return;
                }

                String type = dialogLeaveType.getSelectedItem().toString();
                String inclusive_date = dialogLeaveFrom.getText().toString() + " - " + dialogLeaveTo.getText().toString();

                LeaveModel leaveModel = new LeaveModel(0, type, inclusive_date);
                leaveViewModel.insertLeave(leaveModel);
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


    private void displayDeleteWarningDialog(LeaveModel leaveModel) {
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_delete_warning);
        dialog.setCancelable(false);
        recyclerView.setAdapter(leaveAdapter);
        final TextView itemDescription = dialog.findViewById(R.id.dialog_delete_message2);
        final LeaveModel leaveModel1=leaveModel;

        itemDescription.setText("( "+leaveModel.type + " dated " + leaveModel1.inclusive_date + " )");

        dialog.findViewById(R.id.dialog_delete_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.setAdapter(leaveAdapter);
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.dialog_delete_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leaveViewModel.deleteLeave(leaveModel1);
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

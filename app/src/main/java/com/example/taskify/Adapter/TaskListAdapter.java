package com.example.taskify.Adapter;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskify.Activities.AddTaskActivity;
import com.example.taskify.Activities.TaskDetailActivity;
import com.example.taskify.Model.TaskModel;
import com.example.taskify.VolleyNetworking.NetworkManager;
import com.example.taskify.R;
import com.example.taskify.VolleyNetworking.VolleyCallback;

import java.util.ArrayList;
import java.util.Collections;



public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.ViewHolder> {

    private ArrayList<TaskModel> taskDataset;
    private Context context;
    private ItemTouchHelper itemTouchHelper;
    private static final int EDIT_TASK_REQUEST_CODE = 1001;
    private NetworkManager networkManager;

    public TaskListAdapter(ArrayList<TaskModel> taskDataset, Context context) {
        this.taskDataset = taskDataset;
        this.context = context;
        this.networkManager = NetworkManager.getInstance(context);
        setupItemTouchHelper();
    }

    // Setup for handling swipe and drag & drop functionality
    private void setupItemTouchHelper() {
        itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback( ItemTouchHelper.UP | ItemTouchHelper.DOWN| ItemTouchHelper.START | ItemTouchHelper.END, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            private Drawable editIcon, deleteIcon;
            private ColorDrawable editBackground, deleteBackground;

            // Handle drag to reorder tasks
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                Log.d(TAG, "Dragging from " + fromPosition + " to " + toPosition);
                Collections.swap(taskDataset, fromPosition, toPosition);
                notifyItemMoved(fromPosition, toPosition);
                return true; // Return true as the item has been moved
            }

            // Handle swipe to edit or delete tasks
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (direction == ItemTouchHelper.RIGHT) {
                    editTask(position);
                } else if (direction == ItemTouchHelper.LEFT) {
                    deleteTask(position);
                }
            }

            // Visual feedback for swipe actions
            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                View itemView = viewHolder.itemView;
                int iconMargin = (int) context.getResources().getDimension(R.dimen.icon_margin);
                int iconSize = (int) context.getResources().getDimension(R.dimen.icon_size);

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    // Draw the backgrounds and icons only during swipe actions
                    if (dX > 0) { // Swiping right (edit)
                        editIcon = ContextCompat.getDrawable(context, R.drawable.ic_edit);
                        editBackground = new ColorDrawable(ContextCompat.getColor(context, R.color.edit_background));
                        setIconAndBackground(c, itemView, editIcon, editBackground, iconMargin, iconSize, true, dX);
                    } else if (dX < 0) { // Swiping left (delete)
                        deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete);
                        deleteBackground = new ColorDrawable(ContextCompat.getColor(context, R.color.delete_background));
                        setIconAndBackground(c, itemView, deleteIcon, deleteBackground, iconMargin, iconSize, false, dX);
                    }
                }
            }

            private void setIconAndBackground(Canvas c, View itemView, Drawable icon, ColorDrawable background, int iconMargin, int iconSize, boolean isRightSwipe, float dX) {
                int iconLeft, iconRight, iconTop, iconBottom;
                if (isRightSwipe) {
                    iconLeft = itemView.getLeft() + iconMargin;
                    iconRight = iconLeft + iconSize;
                } else {
                    iconRight = itemView.getRight() - iconMargin;
                    iconLeft = iconRight - iconSize;
                }
                iconTop = itemView.getTop() + (itemView.getHeight() - iconSize) / 2;
                iconBottom = iconTop + iconSize;

                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                background.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getRight(), itemView.getBottom());
                background.draw(c);
                icon.draw(c);
            }
        });
    }

    // Initiate editing a task
    private void editTask(int position) {
        // Launch activity for editing
        TaskModel selectedTask = taskDataset.get(position);
        Intent intent = new Intent(context, AddTaskActivity.class);
        intent.putExtra("selectedTask", selectedTask);
        ((Activity) context).startActivityForResult(intent, EDIT_TASK_REQUEST_CODE);
    }

    // Initiate deletion confirmation for a task
    private void deleteTask(int position) {
        TaskModel taskToDelete = taskDataset.get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Confirmer l'éfface?")
                .setPositiveButton("Éffacé", (dialog, id) -> networkManager.deleteTask(taskToDelete.getTaskId(), new VolleyCallback<String>() {
                    @Override
                    public void onSuccess(String response) {
                        // Remove from the adapter's dataset and notify
                        taskDataset.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, taskDataset.size());
                    }

                    @Override
                    public void onError(String message) {
                        Log.e("TaskListAdapter", "Error deleting task: " + message);
                    }
                }))
                .setNegativeButton("Annulé", (dialog, id) -> notifyItemChanged(position));
        builder.create().show();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView taskName;
        private final TextView taskDeadline;
        private final TextView taskOrder;
        public final CardView taskContainer;

        public ViewHolder(View view) {
            super(view);
            taskName = view.findViewById(R.id.taskName);
            taskDeadline = view.findViewById(R.id.taskDeadline);
            taskOrder = view.findViewById(R.id.taskOrder);
            taskContainer = view.findViewById(R.id.taskContainer);

        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_task, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        TaskModel item = taskDataset.get(position);
        viewHolder.taskName.setText(item.getTaskName());
        viewHolder.taskDeadline.setText("Date d'échance: " + item.getTaskDeadline());
        viewHolder.taskOrder.setText("Odre: " + (position + 1)); // Display the position as the order

        // Set the background color of the CardView
        if (item.getTaskColor() != null && !item.getTaskColor().isEmpty()) {
            viewHolder.taskContainer.setCardBackgroundColor(Color.parseColor(item.getTaskColor()));
        }

        viewHolder.taskContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the selected task
                int clickedPosition = viewHolder.getAdapterPosition();
                if (clickedPosition != RecyclerView.NO_POSITION) {
                    TaskModel selectedTask = taskDataset.get(clickedPosition);

                    // Fetch task details from the server using the task ID
                    networkManager.getTaskById(selectedTask.getTaskId(), new VolleyCallback<TaskModel>() {
                        @Override
                        public void onSuccess(TaskModel task) {

                            // Start a new activity to view task details with updated data
                            Intent intent = new Intent(context, TaskDetailActivity.class);
                            intent.putExtra("taskDetails", task);  // Use the task object fetched from the server
                            intent.putExtra("taskColor", task.getTaskColor());
                            context.startActivity(intent);
                        }

                        @Override
                        public void onError(String message) {

                            // Handle errors, possibly retry or show a message
                            Toast.makeText(context, "Failed to load task details: " + message, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return taskDataset.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        itemTouchHelper.attachToRecyclerView(null);
    }
}
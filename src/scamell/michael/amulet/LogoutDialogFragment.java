package scamell.michael.amulet;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

public class LogoutDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_logout_message)
                .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferencesWrapper.removeFromPrefs(getActivity(), "password");
                        SharedPreferencesWrapper.removeFromPrefs(getActivity(), "logged_in");
                        SharedPreferencesWrapper.removeFromPrefs(getActivity(), "new_login");
                        SharedPreferencesWrapper.removeFromPrefs(getActivity(), "last_task_played");
                        SharedPreferencesWrapper.removeFromPrefs(getActivity(), "last_inspection_task_time");
                        SharedPreferencesWrapper.removeFromPrefs(getActivity(), "last_sequence_task_time");
                        SharedPreferencesWrapper.removeFromPrefs(getActivity(), "task_session_units");
                        SharedPreferencesWrapper.removeFromPrefs(getActivity(), "lastDrinkAdded");
                        SharedPreferencesWrapper.removeFromPrefs(getActivity(), "unitsOfLastDrinkAdded");
                        SharedPreferencesWrapper.removeFromPrefs(getActivity(), "calibration_time_inspection_task");
                        SharedPreferencesWrapper.removeFromPrefs(getActivity(), "calibration_time_sequence_task");
                        SharedPreferencesWrapper.removeFromPrefs(getActivity(), "favourite_drinks");
                        //Try to reupload drink diary and tasks one last time before logging out
                        if (SharedPreferencesWrapper.getFromPrefs(getActivity(), "reUpload_tasks", false)) {
                            TaskEntries taskEntries = TaskEntries.createTaskEntriesFromStorage(getActivity(), "ReUploadTasks.json");
                            if (taskEntries.getNumEntries() > 0) {
                                SendTaskEntriesToServer sendTaskEntriesToServer = new SendTaskEntriesToServer(getActivity(), taskEntries);
                                sendTaskEntriesToServer.sendTaskEntriesToServer();
                            }
                        }
                        TaskEntries taskEntries1 = new TaskEntries();
                        taskEntries1.saveToStorage(getActivity(), "ReUploadTasks.json");
                        SharedPreferencesWrapper.removeFromPrefs(getActivity(), "reUpload_tasks");

                        if (SharedPreferencesWrapper.getFromPrefs(getActivity(), "reUpload_drink_diary", false)) {
                            DrinkDiaryEntries drinkDiaryEntries = DrinkDiaryEntries.createDrinkDiaryEntriesFromStorage(getActivity(), "ReUploadDrinkDiary.json");
                            if (drinkDiaryEntries.getNumEntries() > 0) {
                                SendDrinkDiaryEntriesToServer sendDrinkDiaryEntriesToServer = new SendDrinkDiaryEntriesToServer(getActivity(), drinkDiaryEntries);
                                sendDrinkDiaryEntriesToServer.sendDrinkDiaryEntriesToServer();
                            }
                        }
                        DrinkDiaryEntries drinkDiaryEntries1 = new DrinkDiaryEntries();
                        drinkDiaryEntries1.saveToStorage(getActivity(), "ReUploadDrinkDiary.json");
                        SharedPreferencesWrapper.removeFromPrefs(getActivity(), "reUpload_drink_diary");

                        SharedPreferencesWrapper.removeFromPrefs(getActivity(), "new_user");
                        SharedPreferencesWrapper.removeFromPrefs(getActivity(), "first_time_inspection_task");
                        SharedPreferencesWrapper.removeFromPrefs(getActivity(), "first_time_sequence_task");
                        //save over the drink diary entries and Task Entries so they are not shown on a new start
                        DrinkDiaryEntries dDE = new DrinkDiaryEntries();
                        dDE.saveToStorage(getActivity(), "DrinkDiaryEntries.json");
                        TaskEntries taskEntries = new TaskEntries();
                        taskEntries.saveToStorage(getActivity(), "InspectionTaskEntries.json");
                        taskEntries.saveToStorage(getActivity(), "SequenceTaskEntries.json");
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        startActivity(intent);
                        Toast.makeText(getActivity(), "You've been logged out", Toast.LENGTH_LONG).show();
                        getActivity().finish();
                    }
                })
                .setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //cancelled
                    }
                });
        return builder.create();
    }
}
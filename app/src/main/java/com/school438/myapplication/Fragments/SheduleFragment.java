package com.school438.myapplication.Fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.school438.myapplication.MainActivity;
import com.school438.myapplication.R;
import com.school438.myapplication.SchoolManager.AdapterCustomLessonsEdit;
import com.school438.myapplication.SchoolManager.DBManager;
import com.school438.myapplication.SchoolManager.Lesson;

import java.io.PushbackReader;
import java.util.ArrayList;
import java.util.concurrent.Exchanger;

public class SheduleFragment extends Fragment {

    private DBManager dbManager;
    private ArrayList<Lesson> lessonsArray;
    private ListView lessonsListView;
    private AdapterCustomLessonsEdit adapterLessons;
    private View v;
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void saveShedule() {
        PutSheduleToDBTask p = new PutSheduleToDBTask(MainActivity.CURRENT_TABLE_NAME);
        p.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_shedule, container, false);
        context = v.getContext();
        View footerView = ((LayoutInflater) v.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.list_item_button, null, false);
        lessonsListView = (ListView) v.findViewById(R.id.list_view_lessons);
        dbManager = DBManager.getInstance(v.getContext());
        lessonsArray = dbManager.getAllLessonsFromLocalDB(MainActivity.CURRENT_TABLE_NAME);
        if (lessonsArray == null) {
            adapterLessons = new AdapterCustomLessonsEdit(Lesson.getEmptyShedule(), v.getContext());
            System.out.println("EMPTY SHEDULE USED");
        } else {
            adapterLessons = new AdapterCustomLessonsEdit(
                    AdapterCustomLessonsEdit.makeSheduleForMainListView(lessonsArray), v.getContext());
            System.out.println("SHEDULE FROM DB USED");
        }
        lessonsListView.setAdapter(adapterLessons);
        lessonsListView.addFooterView(footerView);
        return v;
    }

    public void refreshListView() {
        try {
            lessonsArray = dbManager.getAllLessonsFromLocalDB(MainActivity.CURRENT_TABLE_NAME);
            adapterLessons = new AdapterCustomLessonsEdit(
                    AdapterCustomLessonsEdit.makeSheduleForMainListView(lessonsArray), v.getContext());
            lessonsListView.setAdapter(adapterLessons);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class PutSheduleToDBTask extends AsyncTask<String, Void, String> {

        ProgressDialog pDialog;
        String tableName;

        public PutSheduleToDBTask(String tableName) {
            this.tableName = tableName;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(v.getContext());
            pDialog.setMessage("Сохраняю расписание...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                dbManager.putSheduleToDB(adapterLessons.getLessons(), tableName);
                lessonsArray = dbManager.getAllLessonsFromLocalDB(tableName);
                adapterLessons = new AdapterCustomLessonsEdit
                        (AdapterCustomLessonsEdit.makeSheduleForEditor(lessonsArray), context);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            lessonsListView.setAdapter(adapterLessons);
            lessonsListView.deferNotifyDataSetChanged();
            pDialog.dismiss();
        }
    }
}
package com.felind.qrs;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;



import nl.qbusict.cupboard.QueryResultIterable;

import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;
import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class MainActivity extends AppCompatActivity {
    IntentIntegrator scan;
    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    List<CodeList> codeLists;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final DatabaseHelper dbHelper = new DatabaseHelper(this);
        final SQLiteDatabase db = dbHelper.getWritableDatabase();



        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        codeLists = new ArrayList<>();
        adapter = new RecyclerViewAdapter(codeLists, this);
        recyclerView.setAdapter(adapter);
        CardView cardView = findViewById(R.id.cardView);

        Cursor codes = cupboard().withDatabase(db).query(Code.class).orderBy( "_id DESC").getCursor();
        try {
            // Iterate Bunnys
            QueryResultIterable<Code> itr = cupboard().withCursor(codes).iterate(Code.class);
            for (Code bunny : itr) {
                // do something with bunny
                CodeList codeList = new CodeList( bunny._id,bunny.code,bunny.type);
                codeLists.add(codeList);
                adapter = new RecyclerViewAdapter(codeLists, this);
                recyclerView.setAdapter(adapter);
            }
        } finally {
            // close the cursor
            codes.close();
        }

        // Called when a user swipes left or right on a ViewHolder
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }


            // Called when a user swipes left on a ViewHolder
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                final int position = viewHolder.getAdapterPosition();
                CodeList item= codeLists.get(position);

                cupboard().withDatabase(db).delete(Code.class,item.get_Id());
                codeLists.remove(position);
                adapter.notifyItemRemoved(position);
                adapter.notifyItemRangeChanged(position, codeLists.size());
            }
        }).attachToRecyclerView(recyclerView);
        scan = new IntentIntegrator(this);


        //permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);

        //Scan Button
        Button buttonBarCodeScan = findViewById(R.id.fab);
        buttonBarCodeScan.setOnClickListener(new View.OnClickListener() {
            //@Override
            //public void onClick(View view) {
            //    scan.initiateScan();
            //}
        //});
               @Override
                public void onClick(View view) {
                    //initiate scan with our custom scan activity
                    new IntentIntegrator(MainActivity.this).setCaptureActivity(ScannerActivity.class).initiateScan();
                }
            });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        /* clear all button */
        if (id == R.id.action_clearAll) {
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Cursor codes = cupboard().withDatabase(db).query(Code.class).orderBy( "_id DESC").getCursor();
            try {
                if (codes.getCount() > 0) {
                    cupboard().withDatabase(db).delete(Code.class, null);
                    codeLists.clear();
                    adapter.notifyDataSetChanged();
                } else {
                    return true;
                }
            }finally {
                codes.close();
            }
        }
        //export to csv button
        if (id == R.id.export_all) {
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            SQLiteDatabase db;
            File exportDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
            if (!exportDir.exists())
            {
                exportDir.mkdirs();
            }
            File file = new File(exportDir, "QRS.csv");
            try
            {

                file.createNewFile();
                CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
                db = dbHelper.getWritableDatabase();
                Cursor curCSV = db.rawQuery("SELECT * FROM code",null);
                csvWrite.writeNext(curCSV.getColumnNames());
                while(curCSV.moveToNext())
                {
                    //Which column you want to export
                    String arrStr[] ={curCSV.getString(0),curCSV.getString(1), curCSV.getString(2)};
                    csvWrite.writeNext(arrStr);
                }
                csvWrite.close();
                curCSV.close();

                Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinate), "Exported to: " + file.getPath(), Snackbar.LENGTH_LONG);
                snackbar.show();
            }
            catch(Exception sqlEx)
            {
                Log.e("MainActivity", sqlEx.getMessage(), sqlEx);


            }
        }


        return super.onOptionsItemSelected(item);


    }

     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {

            if (result.getContents() == null) {
                Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinate), "Canceled", Snackbar.LENGTH_LONG);

                snackbar.show();
            } else {

                Code codeObj = new Code(result.getContents(),result.getFormatName());
                DatabaseHelper dbHelper = new DatabaseHelper(this);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                long id = cupboard().withDatabase(db).put(codeObj);
                codeLists.clear();
                adapter.notifyDataSetChanged();
                Cursor codes = cupboard().withDatabase(db).query(Code.class).orderBy( "_id DESC").getCursor();
                try {
                    // Iterate Bunnys
                    QueryResultIterable<Code> itr = cupboard().withCursor(codes).iterate(Code.class);
                    for (Code bunny : itr) {
                        // do something with bunny
                        CodeList codeList = new CodeList( bunny._id,bunny.code,bunny.type);
                        codeLists.add(codeList);
                        adapter = new RecyclerViewAdapter(codeLists, this);
                        recyclerView.setAdapter(adapter);
                    }
                } finally {
                    // close the cursor
                    codes.close();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}



package com.tabian.importexceldata;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract.Document;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import org.apache.poi.hssf.record.PageBreakRecord;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.model.Field;

public class FileUploadActivity extends AppCompatActivity {

    private static final String TAG = "FileUploadActivity";

    // Declare variables
    private String[] FilePathStrings;
    private String[] FileNameStrings;
    private File[] listFile;
    Context context;
    File file;
    String PathHolder;
    public static final String ROOT_URL = "http://192.168.0.144:4000/api/v1";

    private static String uniqueID = null;
    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";

    Button btnSDCard;

    ArrayList<String> pathHistory;
    String lastDirectory;
    int count = 0;

    ArrayList<XYValue> uploadData;

    //ListView lvInternalStorage;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //lvInternalStorage = (ListView) findViewById(R.id.lvInternalStorage);
        //btnUpDirectory = (Button) findViewById(R.id.btnUpDirectory);
        FileUploadActivity.id(getApplicationContext());
        btnSDCard = (Button) findViewById(R.id.btnViewSDCard);
        uploadData = new ArrayList<>();

        //need to check the permissions
        checkFilePermissions();

        /*lvInternalStorage.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                lastDirectory = pathHistory.get(count);
                if(lastDirectory.equals(adapterView.getItemAtPosition(i))){
                    Log.d(TAG, "lvInternalStorage: Selected a file for upload: " + lastDirectory);

                    //Execute method for reading the excel data.
                    readExcelData(lastDirectory);
                    //Toast.makeText(FileUploadActivity.this, "Select the file again", Toast.LENGTH_SHORT).show(); // second click

                }else
                {
                    count++;
                    pathHistory.add(count,(String) adapterView.getItemAtPosition(i));
                    checkInternalStorage();
                    Log.d(TAG, "lvInternalStorage: " + pathHistory.get(count));
                }
            }
        });*/

        //Goes up one directory level
       /* btnUpDirectory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(count == 0){
                    Log.d(TAG, "btnUpDirectory: You have reached the highest level directory.");
                }else{
                    pathHistory.remove(count);
                    count--;
                    checkInternalStorage();
                    Log.d(TAG, "btnUpDirectory: " + pathHistory.get(count));
                }
            }
        });*/

        //Opens the SDCard or phone memory
        btnSDCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                //intent.setType("application/excel");
                //System.out.println(" Path vale :"+PathHolder);
                //readExcelData(PathHolder);
                startActivityForResult(intent, 7);

                //count = 0;
                //pathHistory = new ArrayList<String>();

                //pathHistory.add(count,System.getenv("EXTERNAL_STORAGE"));
                //pathHistory.add(count,System.getenv("INTERNAL"));
                //Log.d(TAG, "btnSDCard: " + pathHistory.get(count));
                //checkInternalStorage();
            }
        });
        handleIntent(getIntent());




    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String appLinkAction = intent.getAction();
        Uri appLinkData = intent.getData();
        if (Intent.ACTION_VIEW.equals(appLinkAction) && appLinkData != null){
            String recipeId = appLinkData.getLastPathSegment();
            Uri appData = Uri.parse("content://com.recipe_app/recipe/").buildUpon()
                    .appendPath(recipeId).build();
            //showRecipe(appData);
        }
    }

    public synchronized static String id(Context context) {
        if (uniqueID == null) {
            SharedPreferences sharedPrefs = context.getSharedPreferences(
                    PREF_UNIQUE_ID, Context.MODE_PRIVATE);
            uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);
            if (uniqueID == null) {
                uniqueID = UUID.randomUUID().toString();
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(PREF_UNIQUE_ID, uniqueID);
                editor.commit();
            }
        }
        System.out.println("Unique ID:"+uniqueID);
        return uniqueID;
        //e5708cc2-cdad-4243-af6c-4be60e7eb645
        //e5708cc2-cdad-4243-af6c-4be60e7eb645 - subha
        //5fb6fc55-340b-4858-8497-5d8d02957200 - hari
        //f825a3e7-5d81-4c8a-b367-ad5c5b23ceeb - second subha
        //f825a3e7-5d81-4c8a-b367-ad5c5b23ceeb - rerun

    }




    /**
     *reads the excel file columns then rows. Stores data as ExcelUploadData object
     * @return
     */
    private void readExcelData(String filePath) {
        Log.d(TAG, "readExcelData: Reading Excel File.");
        BasicDBList jsonArray = new BasicDBList();
        //JSONArray jsonArray = new JSONArray();
        ArrayList arrayList = new ArrayList();
        StringBuilder sbJsonArray = new StringBuilder();
        //sbJsonArray.append("[\n");
        String arr[];
        BufferedReader reader = null;

        String output = "";
        //Gson g = new Gson();
        //FileUploadActivity p;
        /*FileUploadActivity p = g.fromJson(sbJsonArray, FileUploadActivity.class);*/
        //String str = g.toJson(sbJsonArray);
        //decarle input file
        File inputFile = new File(filePath);



        try {
            InputStream inputStream = new FileInputStream(inputFile);
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = workbook.getSheetAt(0);
            int rowsCount = sheet.getPhysicalNumberOfRows();
            FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
            StringBuilder sb = new StringBuilder();
            String sHeader[] = null;
            //outter loop, loops through rows

            for (int r = 0; r < rowsCount; r++) {
               // DBObject jObj = new BasicDBObject();
                Map<String,Object> jObj = new TreeMap<>();
                //JSONObject jObj = new JSONObject();
                if(r!=0){
                    sbJsonArray.append("{");
                    arrayList.add("{");
                }
                Row row = sheet.getRow(r);
                int cellsCount = row.getPhysicalNumberOfCells();
                if(r==0) {
                    sHeader = new String[cellsCount];
                }
                //inner loop, loops through columns
                for (int c = 0; c < cellsCount; c++) {
                    //handles if there are to many columns on the excel sheet.
                    if(c>2){
                        Log.e(TAG, "readExcelData: ERROR. Excel File Format is incorrect! " );
                        toastMessage("ERROR: Excel File Format is incorrect!");
                        break;
                    }else{
                        String value = getCellAsString(row, c, formulaEvaluator);
                        String cellInfo = "r:" + r + "; c:" + c + "; v:" + value;

                        if(r==0){
                            sHeader[c] = value ;
                        }else{
                            if(isNumeric(value)){
                                sbJsonArray.append("'"+sHeader[c]+"':"+value);
                                arrayList.add("'"+sHeader[c]+"':"+value);
                                jObj.put(sHeader[c],Double.valueOf(value));




                            }else{
                                sbJsonArray.append("'"+sHeader[c]+"':'"+value+"'");
                                arrayList.add("'"+sHeader[c]+"':'"+value+"'");
                                jObj.put(sHeader[c],value);

                            }

                            if(c!=cellsCount-1){
                                sbJsonArray.append(",");
                                arrayList.add(",");
                            }
                           // sbJsonArray.append("\n");

                        }
                        Log.d(TAG, "readExcelData: Data from row: " + cellInfo);

                    }
                }
                //sb.append("/");
                if(r!=0){
                        //Field ff = new Field(jObj);
                    DBObject db = new BasicDBObject(jObj);
                        jsonArray.add(db);
                        sbJsonArray.append("}");
                        arrayList.add("}");
                        if (r != rowsCount - 1) {
                            sbJsonArray.append(",");
                            arrayList.add(",");
                        }
                        //sbJsonArray.append("\n");

                    }

            }
            //sbJsonArray.append("]");

            Log.d(TAG, "readExcelData: STRINGBUILDER: " + sb.toString());
            System.out.println("Actual Json Array :"+jsonArray);



            System.out.println("Data from excel"+sb.toString()); //new
            System.out.println("ROW11111111 xcel"+sb);
            System.out.println("String array list Array :"+arrayList);
            System.out.println("String array :"+sbJsonArray);
            String sJsonArray = new String(sbJsonArray);
            System.out.println("String Json Array :"+sJsonArray);
            //JSONArray jArry = new JSONArray(sJsonArray);
            //System.out.println("Json Array :"+jArry);
            //System.out.println("Gson String :"+str);
            Date dDate = new Date();
            String currentDateString = DateFormat.getDateInstance(DateFormat.DEFAULT).toString();
            productList(jsonArray,currentDateString);
            //parseStringBuilder(sb);

        }catch (FileNotFoundException e) {
            Log.e(TAG, "readExcelData: FileNotFoundException. " + e.getMessage() );
        } catch (IOException e) {
            Log.e(TAG, "readExcelData: Error reading inputstream. " + e.getMessage() );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isNumeric(String str)
    {
        try
        {
            double d = Double.parseDouble(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }

    private void productlist(String productname,String quantity, String price) {

    }

    /**
     * Method for parsing imported data and storing in ArrayList<XYValue>
     */
    public void parseStringBuilder(StringBuilder mStringBuilder){
        Log.d(TAG, "parseStringBuilder: Started parsing.");

        // splits the sb into rows.
        String[] rows = mStringBuilder.toString().split(":");

        //Add to the ArrayList<XYValue> row by row
        for(int i=0; i<rows.length; i++) {
            //Split the columns of the rows
            String[] columns = rows[i].split(",");

            //use try catch to make sure there are no "" that try to parse into doubles.
            try{
                double x = Double.parseDouble(columns[0]);
                double y = Double.parseDouble(columns[1]);

                String cellInfo = "(x,y): (" + x + "," + y + ")";
                Log.d(TAG, "ParseStringBuilder: Data from row: " + cellInfo);

                //add the the uploadData ArrayList
                uploadData.add(new XYValue(x,y));

            }catch (NumberFormatException e){

                Log.e(TAG, "parseStringBuilder: NumberFormatException: " + e.getMessage());

            }
        }

        printDataToLog();
    }

    private void printDataToLog() {
        Log.d(TAG, "printDataToLog: Printing data to log...");

        for(int i = 0; i< uploadData.size(); i++){
            double x = uploadData.get(i).getX();
            double y = uploadData.get(i).getY();
            Log.d(TAG, "printDataToLog: (x,y): (" + x + "," + y + ")");
        }
        Toast.makeText(FileUploadActivity.this, "Data Uploaded Successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(FileUploadActivity.this, UserActivity.class);
        startActivity(intent);
    }

    /**
     * Returns the cell as a string from the excel file
     * @param row
     * @param c
     * @param formulaEvaluator
     * @return
     */
    private String getCellAsString(Row row, int c, FormulaEvaluator formulaEvaluator) {
        String value = "";
        try {
            Cell cell = row.getCell(c);
            CellValue cellValue = formulaEvaluator.evaluate(cell);
            switch (cellValue.getCellType()) {
                case Cell.CELL_TYPE_BOOLEAN:
                    value = ""+cellValue.getBooleanValue();
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    double numericValue = cellValue.getNumberValue();
                    if(HSSFDateUtil.isCellDateFormatted(cell)) {
                        double date = cellValue.getNumberValue();
                        SimpleDateFormat formatter =
                                new SimpleDateFormat("MM/dd/yy");
                        value = formatter.format(HSSFDateUtil.getJavaDate(date));
                    } else {
                        value = ""+numericValue;
                    }
                    break;
                case Cell.CELL_TYPE_STRING:
                    value = ""+cellValue.getStringValue();
                    break;
                default:
            }
        } catch (NullPointerException e) {

            Log.e(TAG, "getCellAsString: NullPointerException: " + e.getMessage() );
        }
        return value;
    }

    private void checkInternalStorage() {
        Log.d(TAG, "checkInternalStorage: Started.");
        try{
            if (!Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                toastMessage("No SD card found.");
            }
            else{
                // Locate the image folder in your SD Car;d
                file = new File(pathHistory.get(count));
                Log.d(TAG, "checkInternalStorage: directory path: " + pathHistory.get(count));
            }

            listFile = file.listFiles();

            // Create a String array for FilePathStrings
            FilePathStrings = new String[listFile.length];

            // Create a String array for FileNameStrings
            FileNameStrings = new String[listFile.length];

            for (int i = 0; i < listFile.length; i++) {
                // Get the path of the image file
                FilePathStrings[i] = listFile[i].getAbsolutePath();
                // Get the name image file
                FileNameStrings[i] = listFile[i].getName();
            }

            for (int i = 0; i < listFile.length; i++)
            {
                Log.d("Files", "FileName:" + listFile[i].getName());
            }
            lastDirectory = pathHistory.get(count);
            //if(lastDirectory.equals(".xlsx"){
                Log.d(TAG, "lvInternalStorage: Selected a file for upload: " + lastDirectory);

                //Execute method for reading the excel data.
                readExcelData(lastDirectory);
                //Toast.makeText(FileUploadActivity.this, "Select the file again", Toast.LENGTH_SHORT).show(); // second click

            //}
            /* else
            {
                count++;
                pathHistory.add(count,(String));
                checkInternalStorage();
                Log.d(TAG, "lvInternalStorage: " + pathHistory.get(count));
            }*/

            //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, FilePathStrings);
            //lvInternalStorage.setAdapter(adapter);

        }catch(NullPointerException e){
            Log.e(TAG, "checkInternalStorage: NULLPOINTEREXCEPTION " + e.getMessage() );
            Toast.makeText(FileUploadActivity.this, "Select the file again", Toast.LENGTH_SHORT).show();
            //readExcelData(lastDirectory);
        }
        //readExcelData(lastDirectory);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkFilePermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.READ_EXTERNAL_STORAGE");
            permissionCheck += this.checkSelfPermission("Manifest.permission.WRITE_EXTERNAL_STORAGE");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, 1001); //Any number
            }
        }else{
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub

        switch(requestCode){

            case 7:

                if(resultCode==RESULT_OK){

                    String PathHolder = data.getData().getPath();

                    Toast.makeText(FileUploadActivity.this, PathHolder , Toast.LENGTH_LONG).show();
                    readExcelData(PathHolder);

                }
                break;

        }
    }

    /**
     * customizable toast
     * @param message
     */
    private void toastMessage(String message){
        Toast.makeText(this,"Data Uploaded successfully", Toast.LENGTH_SHORT).show();
    }

    private void productList(BasicDBList items,String currentDate) {
        System.out.println("Current date:"+currentDate);
        RestAdapter adapter = new RestAdapter.Builder().setEndpoint(ROOT_URL).build();

        ProductListPostAPI api = adapter.create(ProductListPostAPI.class);
        api.productlist(

                "5b7beef0376456491446871f",
                "2018-04-22T06:32:41.026Z",
                "A",
                items,
                new Callback<Response>() {
                    @Override
                    public void success(Response result, Response response) {
                        System.out.println("In Call Back");
                        BufferedReader reader = null;
                        String output = "";
                        Log.d(TAG, "Register Response: " + response.toString());
                        try {
                            reader = new BufferedReader(new InputStreamReader(result.getBody().in()));
                            output = reader.readLine();
                            Log.d(TAG, "Output Response Product list : " + output);
                            System.out.println("Output Response Product list : "+output);
                            JSONObject jObj = new JSONObject(output);
                            boolean error = jObj.getBoolean("error");

                            if (!error) {
                                //String email_id = jObj.getJSONObject("user").getString("email");
                                //String username = jObj.getJSONObject("user").getString("name");
                                // Launch User activity
                                Intent intent = new Intent(FileUploadActivity.this, UserActivity.class);
                               // intent.putExtra("email", email_id);
                               // intent.putExtra("name", username);
                                System.out.println("output is********************************* "+output);
                                startActivity(intent);
                                finish();
                            } else {
                                String errorMsg = jObj.getString("error_msg");
                                // Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        System.out.println("Error:"+error.getMessage());
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }


        );
    }



}





























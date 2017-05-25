package com.example.user.map2;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.util.Log;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    final String dbname = "myapp";
    private String docId = "123";
    private Manager manager;
    private Database couchDb;
    Poi poi;
    String JSONString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Log.i("Status: ", "Start Couchbase App");
        if (!Manager.isValidDatabaseName(dbname)) {
            Log.i("Status: ", "Bad couchbase db name!");
            return;
        }
        createManager();
        createCouchdb();
        createDocument(docId);

        Document retrievedDocument = retrieveDocument(docId);
        updateDocument(retrievedDocument);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        deleteDocument(retrievedDocument);

    }

    public void createManager() {
        try {
            manager = new Manager(new AndroidContext(getApplicationContext()), Manager.DEFAULT_OPTIONS);
            Log.i("createManager", "Couchbase Manager created!");
        } catch (IOException e) {
            Log.i("createManager", "Failed to create Couchbase Manager!");
            return;
        }
    }

    public void createCouchdb() {
        try {
            couchDb = manager.getDatabase(dbname);
            Log.i("createCouchdb", "Couchbase Database created!");
        } catch (CouchbaseLiteException e) {
            Log.i("createCouchdb", "Failed to create Couchbase Database!");
            return;
        }
    }

    public void createDocument(String docId) {
        // create some dummy data
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Calendar calendar = GregorianCalendar.getInstance();
        String currentTimeString = dateFormatter.format(calendar.getTime());
        Poi poi = new Poi("Marker",120.0,120.0, "Marker Category", "Marker Order");

        // put those dummy data together
        Map<String, Object> docContent = new HashMap<String, Object>();
        //docContent.put("title", "Athens");
        //docContent.put("longitude", 125.0);
        //docContent.put("latitude", 125.0);
        //docContent.put("category", "bar");
        //docContent.put("order", "order");

        docContent.put("poi", poi);
        Log.i("createDocument", "docContent=" + String.valueOf(docContent));

        // create an empty document, add content and write it to the couchDb
        Document document = new Document(couchDb, docId);
        try {
            document.putProperties(docContent);
            Log.i("createDocument: ", "Document written to couchDb named " + dbname + " with ID = " + document.getId());

        } catch (CouchbaseLiteException e) {
            Log.i("createDocument: ", "Failed to write document to Couchbase database!");

        }
    }

    public Document retrieveDocument(String docId) {
        Document retrievedDocument = couchDb.getDocument(docId);
        Log.i("retrieveDocument", "retrievedDocument=" + String.valueOf(retrievedDocument.getProperties()));
        return retrievedDocument;
    }

    public void updateDocument(Document doc) {
        Map<String, Object> updatedProperties = new HashMap<String, Object>();
        updatedProperties.putAll(doc.getProperties());
        //updatedProperties.put ("message", "We're having a heat wave!");
        //updatedProperties.put ("temperature", "95");

        try {
            doc.putProperties(updatedProperties);
            Log.i("updateDocument", "updated retrievedDocument=" + String.valueOf(doc.getProperties()));
        } catch (CouchbaseLiteException e) {
            Log.i("updateDocument", "Failed to update document!");

        }
    }

    public void deleteDocument(Document doc) {
        try {
            doc.delete();
            Log.i("deleteDocument: ", "Document deleted from Couchbase database!");
        } catch (CouchbaseLiteException e) {
            Log.i("deleteDocument: ", "Failed to write delete document from Couchbase database!");
        }
    }

    private Poi retrievePoi(Database database, String docId) {

        Object poiObj  = null;
        Document retrievedDocument = database.getDocument(docId);
        Log.i("@retrievePoi MYAPP - ID:", retrievedDocument.getId()); //empty!!!

        Poi p1 = null;
        try{
            p1 = parseDocPoi(retrievedDocument);
        }
        catch (Exception e){
            Log.i("exception",e.getMessage());
        }
        Log.d("tag", poiObj.toString());
        //Retrieve the document by id
        return p1;
    }

    private Poi parseDocPoi(Document d ) {
        try{
            Object poiObj;
            Gson gson = new Gson();
            poiObj = d.getProperties().get("poi");
            String JSONString = gson.toJson(poiObj, Poi.class); //Convert the object to json string using Gson
            Poi poi = (Poi) poiObj;
            gson.fromJson(JSONString, Poi.class); //convert the json string to Poi object
            Log.i("@parseDocPoi MYAPP - JSONString:", JSONString); //empty!!!
            Log.i("getPoiFromDocument", "jsonString>>>" + poi.getCategory());
//        Log.i("getPoiFromDocument", "poi>>>" + poi.getTitle());
//        Log.i("getPoiFromDocument", "longitude>>>" + poi.getLongitude());
//        Log.i("getPoiFromDocument", "latitude>>>" + poi.getLatitude());
//        Log.i("getPoiFromDocument", "category>>>" + poi.getCategory());
//        Log.i("getPoiFromDocument", "order>>>" + poi.getOrder());
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.i("getPoiFromDocument", "jsonString>>>" + poi.getCategory());
        }


        return poi;
    }

    //from OnMapReady
    private List<Poi> retrieveAllPois(Database database, String channel) {

        List<Poi> listOfPois = null;

        // Let's find the documents that have conflicts so we can resolve them:
        Query query = database.createAllDocumentsQuery();
        query.setAllDocsMode(Query.AllDocsMode.ALL_DOCS);
        QueryEnumerator result = null;
        try {
            result = query.run();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
        for (Iterator<QueryRow> it = result; it.hasNext(); ) {

            QueryRow row = it.next();
            Log.i("MYAPP", "Query Document ID is: ", row);
            Log.i("MYAPP", "Query result is: ", row.getDocumentId());

            //p = parseDocPoi(row.getDocument());
            Log.i("@retrieveAllPois MYAPP - ID:", row.getDocument().getId());
            Log.i("@retrieveAllPois MYAPP - Title:", row.getDocument().getProperties().get("title").toString());

//            ObjectMapper mapper = new ObjectMapper();  // Usually you only need one instance of this per app.
//            try {
//                JSONString = retrieveDocument(docId).toString();
//                Map<String, Object> map = mapper.readValue(JSONString, Map.class);
//                Document document = couchDb.getDocument(docId);
//                document.putProperties(map);
//            } catch (IOException | CouchbaseLiteException ex) {
//                ex.printStackTrace();
//            }

            Poi p = parseDocPoi(row.getDocument());
            Log.i("1 - retrieveAllPois MYAPP - categ", p.getCategory().toString());
            listOfPois.add(p);
            Log.i("2  -- retrieveAllPois MYAPP - categ", p.getCategory().toString());
            listOfPois.add(p);
        }

        return listOfPois;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //String total2 = String.valueOf( poi.getLatitude());
        List<Poi> pois = retrieveAllPois(couchDb,"public");
        try{
            Log.i("+++++++++++++++++getLatitude::::::::::::::",pois.get(0).toString());
            Poi p = new Poi();
            p = retrievePoi(couchDb,"");
            p.setLatitude(poi.getLatitude());
            p.setLongitude(poi.getLongitude());
            // Add a marker in Sydney, Australia, and move the camera.
            //LatLng sydney = new LatLng(-34, 151);
            LatLng sydney = new LatLng(pois.get(0).getLatitude(), pois.get(0).getLongitude());
            mMap.addMarker(new MarkerOptions().position(sydney).title(pois.get(0).getTitle()));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        }catch (Exception e) {
            Log.i("Exception",e.getStackTrace().toString());
        }

        Log.i("Status: ", "End the App!");
    }

}
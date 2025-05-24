package com.example.worksyck;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Polygon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private static final String SAVE_LOCATION_URL = "http:/192.168.1.13/worksync/save_location.php";

    private MapView map;
    private Button btnClearAll;
    private final List<LocationItem> locationItems = new ArrayList<>();
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_maps);

        // Get user ID from intent
        userId = getIntent().getIntExtra("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
        }

        initializeViews();
        setupMap();
        setupButtonListeners();
        requestPermissionsIfNecessary(new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION
        });

        // Load saved locations from database
        new LoadLocationsTask().execute();
    }

    private void initializeViews() {
        map = findViewById(R.id.map);
        btnClearAll = findViewById(R.id.btnClearAll);
    }

    private void setupMap() {
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        IMapController mapController = map.getController();
        mapController.setZoom(15.0);
        GeoPoint startPoint = new GeoPoint(33.5138, 36.2765); // Damascus coordinates
        mapController.setCenter(startPoint);

        map.getOverlays().add(new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                showDimensionInputDialog(p, null);
                return true;
            }
        }));
    }

    private void setupButtonListeners() {
        btnClearAll.setOnClickListener(v -> showClearAllConfirmation());
    }

    private void showClearAllConfirmation() {
        if (locationItems.isEmpty()) {
            Toast.makeText(this, "لا توجد عناصر لحذفها", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("حذف الكل")
                .setMessage("هل أنت متأكد أنك تريد حذف جميع المواقع؟")
                .setPositiveButton("نعم", (dialog, which) -> clearAllLocations())
                .setNegativeButton("لا", null)
                .show();
    }

    private void clearAllLocations() {
        for (LocationItem item : locationItems) {
            map.getOverlays().remove(item.marker);
            map.getOverlays().remove(item.rectangle);
            new DeleteLocationTask().execute(item.id);
        }
        locationItems.clear();
        map.invalidate();
        Toast.makeText(this, "تم حذف جميع المواقع", Toast.LENGTH_SHORT).show();
    }

    private void showDimensionInputDialog(GeoPoint center, LocationItem existingItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(existingItem == null ? "أدخل الأبعاد" : "تعديل الأبعاد");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText inputWidth = createNumberInput("العرض (متر)");
        final EditText inputHeight = createNumberInput("الطول (متر)");

        layout.addView(inputWidth);
        layout.addView(inputHeight);

        if (existingItem != null) {
            inputWidth.setText(String.valueOf(existingItem.width));
            inputHeight.setText(String.valueOf(existingItem.height));
        }

        builder.setView(layout);
        builder.setPositiveButton("موافق", (dialog, which) ->
                processDimensionsInput(center, existingItem, inputWidth, inputHeight));
        builder.setNegativeButton("إلغاء", null);
        builder.show();
    }

    private EditText createNumberInput(String hint) {
        EditText editText = new EditText(this);
        editText.setHint(hint);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 16, 0, 16);
        editText.setLayoutParams(params);
        return editText;
    }

    private void processDimensionsInput(GeoPoint center, LocationItem existingItem,
                                        EditText inputWidth, EditText inputHeight) {
        try {
            double width = Double.parseDouble(inputWidth.getText().toString());
            double height = Double.parseDouble(inputHeight.getText().toString());

            if (width <= 0 || height <= 0) {
                Toast.makeText(this, "يجب أن تكون الأبعاد أكبر من الصفر", Toast.LENGTH_SHORT).show();
                return;
            }

            if (existingItem != null) {
                updateRectangle(existingItem, width, height);
            } else {
                addRectangleOnMap(center, width, height);
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "أدخل أرقام صحيحة", Toast.LENGTH_SHORT).show();
        }
    }

    private void addRectangleOnMap(GeoPoint center, double widthMeters, double heightMeters) {
        Polygon rectangle = createRectanglePolygon(center, widthMeters, heightMeters);
        Marker marker = createCenterMarker(center, locationItems.size() + 1);

        map.getOverlays().add(rectangle);
        map.getOverlays().add(marker);

        LocationItem item = new LocationItem(0, center, widthMeters, heightMeters, marker, rectangle);
        locationItems.add(item);

        marker.setOnMarkerClickListener((m, mv) -> {
            showMarkerOptions(item);
            return true;
        });

        // Save to database
        new SaveLocationTask().execute(item);

        map.invalidate();
    }

    private Polygon createRectanglePolygon(GeoPoint center, double widthMeters, double heightMeters) {
        double latOffset = (heightMeters / 2) / 111320f;
        double lonOffset = (widthMeters / 2) / (111320f * Math.cos(Math.toRadians(center.getLatitude())));

        GeoPoint nw = new GeoPoint(center.getLatitude() + latOffset, center.getLongitude() - lonOffset);
        GeoPoint ne = new GeoPoint(center.getLatitude() + latOffset, center.getLongitude() + lonOffset);
        GeoPoint se = new GeoPoint(center.getLatitude() - latOffset, center.getLongitude() + lonOffset);
        GeoPoint sw = new GeoPoint(center.getLatitude() - latOffset, center.getLongitude() - lonOffset);

        Polygon rectangle = new Polygon();
        rectangle.setPoints(List.of(nw, ne, se, sw));
        rectangle.setFillColor(0x220000FF);
        rectangle.setStrokeColor(0xFF0000FF);
        rectangle.setStrokeWidth(3f);
        return rectangle;
    }

    private Marker createCenterMarker(GeoPoint center, int index) {
        Marker marker = new Marker(map);
        marker.setPosition(center);
        marker.setTitle("نقطة " + index);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        return marker;
    }

    private void updateRectangle(LocationItem item, double widthMeters, double heightMeters) {
        map.getOverlays().remove(item.rectangle);
        map.getOverlays().remove(item.marker);
        locationItems.remove(item);

        // Update in database
        new UpdateLocationTask().execute(item.id, widthMeters, heightMeters);

        addRectangleOnMap(item.center, widthMeters, heightMeters);
    }

    private void showMarkerOptions(LocationItem item) {
        new AlertDialog.Builder(this)
                .setTitle("خيارات النقطة")
                .setItems(new CharSequence[]{"تعديل", "حذف"}, (dialog, which) -> {
                    if (which == 0) {
                        showDimensionInputDialog(item.center, item);
                    } else {
                        deleteItem(item);
                    }
                })
                .show();
    }

    private void deleteItem(LocationItem item) {
        map.getOverlays().remove(item.marker);
        map.getOverlays().remove(item.rectangle);
        locationItems.remove(item);

        // Delete from database
        new DeleteLocationTask().execute(item.id);

        map.invalidate();
        Toast.makeText(this, "تم حذف النقطة", Toast.LENGTH_SHORT).show();
    }

    // Database Tasks
    private class SaveLocationTask extends AsyncTask<LocationItem, Void, Integer> {
        @Override
        protected Integer doInBackground(LocationItem... items) {
            LocationItem item = items[0];
            try {
                URL url = new URL(SAVE_LOCATION_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("user_id", userId);
                jsonParam.put("latitude", item.center.getLatitude());
                jsonParam.put("longitude", item.center.getLongitude());
                jsonParam.put("width", item.width);
                jsonParam.put("height", item.height);

                OutputStream os = conn.getOutputStream();
                os.write(jsonParam.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    return jsonResponse.getInt("id");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return -1;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result != -1 && !locationItems.isEmpty()) {
                locationItems.get(locationItems.size() - 1).id = result;
                Toast.makeText(MapsActivity.this, "تم حفظ الموقع", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MapsActivity.this, "فشل في حفظ الموقع", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class LoadLocationsTask extends AsyncTask<Void, Void, List<LocationItem>> {
        @Override
        protected List<LocationItem> doInBackground(Void... voids) {
            List<LocationItem> items = new ArrayList<>();
            try {
                URL url = new URL("http://192.168.1.13/worksync/get_locations.php?user_id=" + userId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                InputStream in = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONArray jsonArray = new JSONArray(response.toString());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    int id = jsonObject.getInt("id");
                    double latitude = jsonObject.getDouble("latitude");
                    double longitude = jsonObject.getDouble("longitude");
                    double width = jsonObject.getDouble("width");
                    double height = jsonObject.getDouble("height");

                    GeoPoint center = new GeoPoint(latitude, longitude);
                    Polygon rectangle = createRectanglePolygon(center, width, height);
                    Marker marker = createCenterMarker(center, i + 1);

                    items.add(new LocationItem(id, center, width, height, marker, rectangle));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return items;
        }

        @Override
        protected void onPostExecute(List<LocationItem> result) {
            for (LocationItem item : result) {
                locationItems.add(item);
                map.getOverlays().add(item.rectangle);
                map.getOverlays().add(item.marker);

                // Set click listener for each marker
                item.marker.setOnMarkerClickListener((m, mv) -> {
                    showMarkerOptions(item);
                    return true;
                });
            }
            map.invalidate();
        }
    }

    private class UpdateLocationTask extends AsyncTask<Object, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Object... params) {
            int id = (int) params[0];
            double width = (double) params[1];
            double height = (double) params[2];

            try {
                URL url = new URL(SAVE_LOCATION_URL + "/" + id);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("width", width);
                jsonParam.put("height", height);

                OutputStream os = conn.getOutputStream();
                os.write(jsonParam.toString().getBytes());
                os.flush();
                os.close();

                return conn.getResponseCode() == HttpURLConnection.HTTP_OK;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (!success) {
                Toast.makeText(MapsActivity.this, "فشل في تحديث الموقع", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class DeleteLocationTask extends AsyncTask<Integer, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Integer... ids) {
            try {
                URL url = new URL(SAVE_LOCATION_URL + "/" + ids[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");
                return conn.getResponseCode() == HttpURLConnection.HTTP_OK;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (!success) {
                Toast.makeText(MapsActivity.this, "فشل في حذف الموقع", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "الصلاحية مطلوبة: " + permissions[i], Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
    }

    static class LocationItem {
        int id;
        final GeoPoint center;
        double width;
        double height;
        final Marker marker;
        final Polygon rectangle;

        LocationItem(int id, GeoPoint center, double width, double height, Marker marker, Polygon rectangle) {
            this.id = id;
            this.center = center;
            this.width = width;
            this.height = height;
            this.marker = marker;
            this.rectangle = rectangle;
        }
    }
}
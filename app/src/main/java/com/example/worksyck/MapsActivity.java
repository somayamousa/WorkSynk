package com.example.worksyck;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import org.osmdroid.views.overlay.Polyline;

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
    private static final String SAVE_LOCATION_URL = "http://192.168.1.13/worksync/save_location.php";
    private static final String GET_LOCATIONS_URL = "http://192.168.1.13/worksync/get_locations.php";
    private static final String DELETE_LOCATION_URL = "http://192.168.1.13/worksync/delete_location.php";
    private static final String TAG = "MapsActivity";

    private MapView map;
    private Button btnSaveLocation;
    private Button btnClearLocation;
    private LocationItem currentLocationItem;
    private int userId;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private boolean isMapCentered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_maps);

        // Get user ID from intent
        userId = getIntent().getIntExtra("user_id", -1);
        if (userId == -1) {
            showErrorAndFinish("User not authenticated");
            return;
        }

        initializeViews();
        setupMap();
        setupButtonListeners();
        requestPermissionsIfNecessary(new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION
        });
    }

    private void showErrorAndFinish(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }

    private void initializeViews() {
        map = findViewById(R.id.map);
        btnSaveLocation = findViewById(R.id.btnSaveLocation);
        btnClearLocation = findViewById(R.id.btnClearLocation);

        // Disable buttons initially
        btnSaveLocation.setEnabled(false);
        btnClearLocation.setEnabled(false);
    }

    private void setupMap() {
        try {
            map.setTileSource(TileSourceFactory.MAPNIK);
            map.setMultiTouchControls(true);

            // Set up location manager
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (!isMapCentered) {
                        centerMapOnLocation(new GeoPoint(location.getLatitude(), location.getLongitude()));
                        isMapCentered = true;
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {}

                @Override
                public void onProviderEnabled(String provider) {}

                @Override
                public void onProviderDisabled(String provider) {}
            };

            // Add map click listener
            map.getOverlays().add(new MapEventsOverlay(new MapEventsReceiver() {
                @Override
                public boolean singleTapConfirmedHelper(GeoPoint p) {
                    return false;
                }

                @Override
                public boolean longPressHelper(GeoPoint p) {
                    if (currentLocationItem != null) {
                        Toast.makeText(MapsActivity.this, "يوجد موقع محفوظ بالفعل", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    showShapeSelectionDialog(p);
                    return true;
                }
            }));

            // Get current location immediately and center map
            getCurrentLocationAndCenter();

            // Load saved locations
            new LoadLocationsTask().execute();

        } catch (Exception e) {
            Log.e(TAG, "Error setting up map", e);
            showErrorAndFinish("Failed to initialize map");
        }
    }

    private void getCurrentLocationAndCenter() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastKnownLocation == null) {
                    lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }

                if (lastKnownLocation != null) {
                    centerMapOnLocation(new GeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()));
                    isMapCentered = true;
                } else {
                    // Default to a reasonable location if no location available
                    centerMapOnLocation(new GeoPoint(24.7136, 46.6753)); // Riyadh coordinates as default
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting current location", e);
            // Default to a reasonable location
            centerMapOnLocation(new GeoPoint(24.7136, 46.6753)); // Riyadh coordinates as default
        }
    }

    private void centerMapOnLocation(GeoPoint point) {
        IMapController mapController = map.getController();
        mapController.setCenter(point);
        mapController.setZoom(18.0); // Maximum zoom level
    }

    private void setupButtonListeners() {
        btnSaveLocation.setOnClickListener(v -> saveCurrentLocation());
        btnClearLocation.setOnClickListener(v -> clearCurrentLocation());
    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        try {
            List<String> permissionsToRequest = new ArrayList<>();
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(permission);
                }
            }
            if (!permissionsToRequest.isEmpty()) {
                ActivityCompat.requestPermissions(
                        this,
                        permissionsToRequest.toArray(new String[0]),
                        REQUEST_PERMISSIONS_REQUEST_CODE
                );
            } else {
                startLocationUpdates();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error requesting permissions", e);
        }
    }

    private void startLocationUpdates() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, locationListener);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 10, locationListener);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error starting location updates", e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                startLocationUpdates();
                getCurrentLocationAndCenter();
            } else {
                Toast.makeText(this, "Location permission is required", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showShapeSelectionDialog(GeoPoint center) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("اختر الشكل");

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(50, 20, 50, 20);

            // Shape selection spinner
            Spinner shapeSpinner = new Spinner(this);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.shape_types, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            shapeSpinner.setAdapter(adapter);

            layout.addView(shapeSpinner);

            builder.setView(layout);
            builder.setPositiveButton("التالي", (dialog, which) -> {
                String selectedShape = shapeSpinner.getSelectedItem().toString();
                showDimensionInputDialog(center, selectedShape);
            });
            builder.setNegativeButton("إلغاء", null);
            builder.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing shape selection dialog", e);
            Toast.makeText(this, "فشل في عرض خيارات الشكل", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDimensionInputDialog(GeoPoint center, String shapeType) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("أدخل الأبعاد");

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(50, 20, 50, 20);

            if (shapeType.equals("مربع/مستطيل")) {
                final EditText inputWidth = createNumberInput("العرض (متر)");
                final EditText inputHeight = createNumberInput("الطول (متر)");
                layout.addView(inputWidth);
                layout.addView(inputHeight);

                builder.setView(layout);
                builder.setPositiveButton("موافق", (dialog, which) -> {
                    try {
                        double width = Double.parseDouble(inputWidth.getText().toString());
                        double height = Double.parseDouble(inputHeight.getText().toString());
                        validateAndCreateShape(center, shapeType, width, height);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "أدخل أرقام صحيحة", Toast.LENGTH_SHORT).show();
                    }
                });
            } else { // Circle
                final EditText inputRadius = createNumberInput("نصف القطر (متر)");
                layout.addView(inputRadius);

                builder.setView(layout);
                builder.setPositiveButton("موافق", (dialog, which) -> {
                    try {
                        double radius = Double.parseDouble(inputRadius.getText().toString());
                        validateAndCreateShape(center, shapeType, radius, 0);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "أدخل رقم صحيح", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            builder.setNegativeButton("إلغاء", null);
            builder.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing dimension input dialog", e);
            Toast.makeText(this, "فشل في عرض إدخال الأبعاد", Toast.LENGTH_SHORT).show();
        }
    }

    private void validateAndCreateShape(GeoPoint center, String shapeType, double dimension1, double dimension2) {
        try {
            if (dimension1 <= 0 || (shapeType.equals("مربع/مستطيل") && dimension2 <= 0)) {
                Toast.makeText(this, "يجب أن تكون الأبعاد أكبر من الصفر", Toast.LENGTH_SHORT).show();
                return;
            }

            clearCurrentLocation();
            addShapeOnMap(center, shapeType, dimension1, dimension2);
        } catch (Exception e) {
            Log.e(TAG, "Error creating shape", e);
            Toast.makeText(this, "فشل في إنشاء الشكل", Toast.LENGTH_SHORT).show();
        }
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

    private void addShapeOnMap(GeoPoint center, String shapeType, double dimension1, double dimension2) {
        try {
            Marker marker = createCenterMarker(center);
            Polyline shape = null;

            if (shapeType.equals("مربع/مستطيل")) {
                shape = createRectanglePolygon(center, dimension1, dimension2);
            } else { // Circle
                shape = createCirclePolygon(center, dimension1);
            }

            map.getOverlays().add(shape);
            map.getOverlays().add(marker);

            currentLocationItem = new LocationItem(0, center, shapeType,
                    dimension1, dimension2, dimension1, marker, shape);

            marker.setOnMarkerClickListener((m, mv) -> {
                showMarkerOptions();
                return true;
            });

            // Enable buttons
            btnSaveLocation.setEnabled(true);
            btnClearLocation.setEnabled(true);

            map.invalidate();
        } catch (Exception e) {
            Log.e(TAG, "Error adding shape to map", e);
            Toast.makeText(this, "فشل في إضافة الشكل", Toast.LENGTH_SHORT).show();
        }
    }

    private Polyline createRectanglePolygon(GeoPoint center, double widthMeters, double heightMeters) {
        double latOffset = (heightMeters / 2) / 111320f;
        double lonOffset = (widthMeters / 2) / (111320f * Math.cos(Math.toRadians(center.getLatitude())));

        GeoPoint nw = new GeoPoint(center.getLatitude() + latOffset, center.getLongitude() - lonOffset);
        GeoPoint ne = new GeoPoint(center.getLatitude() + latOffset, center.getLongitude() + lonOffset);
        GeoPoint se = new GeoPoint(center.getLatitude() - latOffset, center.getLongitude() + lonOffset);
        GeoPoint sw = new GeoPoint(center.getLatitude() - latOffset, center.getLongitude() - lonOffset);

        Polyline rectangle = new Polyline();
        rectangle.setPoints(List.of(nw, ne, se, sw, nw)); // Close the polygon
        rectangle.setColor(0xFF00FF00); // Green color
        rectangle.setWidth(3f);
        return rectangle;
    }

    private Polyline createCirclePolygon(GeoPoint center, double radiusMeters) {
        int points = 36; // Number of points to approximate circle
        List<GeoPoint> circlePoints = new ArrayList<>();

        for (int i = 0; i <= points; i++) {
            double angle = Math.PI * 2 * i / points;
            double latOffset = (radiusMeters * Math.sin(angle)) / 111320f;
            double lonOffset = (radiusMeters * Math.cos(angle)) / (111320f * Math.cos(Math.toRadians(center.getLatitude())));

            circlePoints.add(new GeoPoint(
                    center.getLatitude() + latOffset,
                    center.getLongitude() + lonOffset
            ));
        }

        Polyline circle = new Polyline();
        circle.setPoints(circlePoints);
        circle.setColor(0xFF00FF00); // Green color
        circle.setWidth(3f);
        return circle;
    }

    private Marker createCenterMarker(GeoPoint center) {
        Marker marker = new Marker(map);
        marker.setPosition(center);
        marker.setTitle("موقعي");
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        return marker;
    }

    private void showMarkerOptions() {
        try {
            new AlertDialog.Builder(this)
                    .setTitle("خيارات النقطة")
                    .setItems(new CharSequence[]{"حذف من الخريطة", "حذف نهائي", "إلغاء"}, (dialog, which) -> {
                        if (which == 0) {
                            // Remove from map only
                            clearCurrentLocation();
                        } else if (which == 1) {
                            // Delete from database and map
                            if (currentLocationItem != null && currentLocationItem.id > 0) {
                                new DeleteLocationTask().execute(currentLocationItem.id);
                                clearCurrentLocation();
                            } else {
                                clearCurrentLocation();
                            }
                        }
                    })
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing marker options", e);
            Toast.makeText(this, "فشل في عرض خيارات النقطة", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearCurrentLocation() {
        try {
            if (currentLocationItem != null) {
                removeItemFromMap(currentLocationItem);
                currentLocationItem = null;

                // Disable buttons
                btnSaveLocation.setEnabled(false);
                btnClearLocation.setEnabled(false);

                Toast.makeText(this, "تم حذف الموقع", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error clearing location", e);
            Toast.makeText(this, "فشل في حذف الموقع", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeItemFromMap(LocationItem item) {
        try {
            map.getOverlays().remove(item.marker);
            map.getOverlays().remove(item.shape);
            map.invalidate();
        } catch (Exception e) {
            Log.e(TAG, "Error removing item from map", e);
        }
    }

    private void saveCurrentLocation() {
        if (currentLocationItem == null) {
            Toast.makeText(this, "لا يوجد موقع لحفظه", Toast.LENGTH_SHORT).show();
            return;
        }

        new SaveLocationTask().execute(currentLocationItem);
    }

    private class SaveLocationTask extends AsyncTask<LocationItem, Void, Boolean> {
        @Override
        protected Boolean doInBackground(LocationItem... items) {
            if (items.length == 0) return false;

            LocationItem item = items[0];
            HttpURLConnection conn = null;
            try {
                URL url = new URL(SAVE_LOCATION_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("user_id", userId);
                jsonParam.put("latitude", item.center.getLatitude());
                jsonParam.put("longitude", item.center.getLongitude());

                // Fixed: Proper circle handling
                if (item.shapeType.equals("دائرة")) {
                    jsonParam.put("width", item.radius); // Save radius in width field
                    jsonParam.put("height", -1); // -1 indicates circle
                } else {
                    jsonParam.put("width", item.width);
                    jsonParam.put("height", item.height);
                }

                Log.d(TAG, "Sending JSON: " + jsonParam.toString());

                OutputStream os = conn.getOutputStream();
                os.write(jsonParam.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "Response Code: " + responseCode);

                // Read response (both success and error responses)
                InputStream inputStream;
                if (responseCode >= 200 && responseCode < 300) {
                    inputStream = conn.getInputStream();
                } else {
                    inputStream = conn.getErrorStream();
                }

                BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                String responseString = response.toString();
                Log.d(TAG, "Server Response: " + responseString);

                // Check if response is valid JSON
                if (responseString.trim().startsWith("{")) {
                    try {
                        JSONObject jsonResponse = new JSONObject(responseString);
                        if (jsonResponse.getBoolean("success")) {
                            item.id = jsonResponse.getInt("id");
                            return true;
                        } else {
                            Log.e(TAG, "Server error: " + jsonResponse.optString("message", "Unknown error"));
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error: " + e.getMessage());
                        Log.e(TAG, "Response was: " + responseString);
                    }
                } else {
                    Log.e(TAG, "Server returned HTML instead of JSON: " + responseString);
                }

            } catch (Exception e) {
                Log.e(TAG, "Error saving location", e);
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(MapsActivity.this, "تم حفظ الموقع بنجاح", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MapsActivity.this, "فشل في حفظ الموقع", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class LoadLocationsTask extends AsyncTask<Void, Void, LocationItem> {
        @Override
        protected LocationItem doInBackground(Void... voids) {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(GET_LOCATIONS_URL + "?user_id=" + userId);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                InputStream in = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONArray jsonArray = new JSONArray(response.toString());
                if (jsonArray.length() > 0) {
                    // Load only the first location (business rule: one location per user)
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    int id = jsonObject.getInt("id");
                    double latitude = jsonObject.getDouble("latitude");
                    double longitude = jsonObject.getDouble("longitude");
                    double width = jsonObject.getDouble("width");
                    double height = jsonObject.getDouble("height");

                    GeoPoint center = new GeoPoint(latitude, longitude);

                    // Fixed: Proper circle detection and handling
                    if (height == -1) {
                        // It's a circle
                        return new LocationItem(id, center, "دائرة", width, -1, width, null, null);
                    } else {
                        // It's a rectangle
                        return new LocationItem(id, center, "مربع/مستطيل", width, height, 0, null, null);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading locations", e);
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(LocationItem result) {
            if (result != null) {
                // Load the saved location onto the map
                if (result.shapeType.equals("دائرة")) {
                    addShapeOnMap(result.center, result.shapeType, result.radius, 0);
                } else {
                    addShapeOnMap(result.center, result.shapeType, result.width, result.height);
                }
                currentLocationItem.id = result.id;
            }
        }
    }

    private class DeleteLocationTask extends AsyncTask<Integer, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Integer... ids) {
            if (ids.length == 0) return false;

            HttpURLConnection conn = null;
            try {
                URL url = new URL(DELETE_LOCATION_URL + "?id=" + ids[0]);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

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
                    return jsonResponse.getBoolean("success");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error deleting location", e);
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(MapsActivity.this, "تم حذف الموقع نهائياً", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MapsActivity.this, "فشل في حذف الموقع", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            map.onResume();
        } catch (Exception e) {
            Log.e(TAG, "Error resuming map", e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            map.onPause();
            if (locationManager != null && locationListener != null) {
                locationManager.removeUpdates(locationListener);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error pausing map", e);
        }
    }

    static class LocationItem {
        int id;
        final GeoPoint center;
        final String shapeType;
        final double width;
        final double height;
        final double radius;
        final Marker marker;
        final Polyline shape;

        LocationItem(int id, GeoPoint center, String shapeType,
                     double width, double height, double radius,
                     Marker marker, Polyline shape) {
            this.id = id;
            this.center = center;
            this.shapeType = shapeType;
            this.width = width;
            this.height = height;
            this.radius = radius;
            this.marker = marker;
            this.shape = shape;
        }
    }
}
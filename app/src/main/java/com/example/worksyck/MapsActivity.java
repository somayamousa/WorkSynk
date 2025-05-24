package com.example.worksyck;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Polygon;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private MapView map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // تهيئة OSMDroid مع اسم المستخدم
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_maps); // يجب إنشاء هذا الملف xml فيه MapView معرف بـ id="map"

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        IMapController mapController = map.getController();
        mapController.setZoom(15.0);

        GeoPoint startPoint = new GeoPoint(33.5138, 36.2765); // نقطة البداية (مثال: دمشق)
        mapController.setCenter(startPoint);

        // استقبال أحداث النقر الطويل عبر MapEventsOverlay
        MapEventsReceiver mReceive = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                return false; // لا نفعل شيء على النقر العادي
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                // عند الضغط الطويل، نطلب من المستخدم إدخال الأبعاد
                showDimensionInputDialog(p);
                return true;
            }
        };
        map.getOverlays().add(new MapEventsOverlay(mReceive));

        // طلب صلاحيات
        requestPermissionsIfNecessary(new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION
        });
    }

    // دالة عرض مربع حوار لطلب عرض وطول المستطيل (بالمتر)
    private void showDimensionInputDialog(GeoPoint center) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("أدخل الأبعاد (بالمتر)");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);

        final EditText inputWidth = new EditText(this);
        inputWidth.setHint("العرض");
        inputWidth.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(inputWidth);

        final EditText inputHeight = new EditText(this);
        inputHeight.setHint("الطول");
        inputHeight.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(inputHeight);

        builder.setView(layout);

        builder.setPositiveButton("موافق", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    double width = Double.parseDouble(inputWidth.getText().toString());
                    double height = Double.parseDouble(inputHeight.getText().toString());
                    addRectangleOnMap(center, width, height);
                } catch (NumberFormatException e) {
                    Toast.makeText(MapsActivity.this, "الرجاء إدخال أرقام صحيحة", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("إلغاء", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    // دالة حساب ورسم المستطيل على الخريطة
    private void addRectangleOnMap(GeoPoint center, double widthMeters, double heightMeters) {
        // 1 درجة عرض تقريبا تساوي 111,320 متر
        double latOffset = (heightMeters / 2) / 111320f;

        // طول درجة الطول يعتمد على خط العرض الحالي
        double lonDegreeMeters = 111320 * Math.cos(Math.toRadians(center.getLatitude()));
        double lonOffset = (widthMeters / 2) / lonDegreeMeters;

        // حساب نقاط المستطيل (4 زوايا)
        GeoPoint nw = new GeoPoint(center.getLatitude() + latOffset, center.getLongitude() - lonOffset);
        GeoPoint ne = new GeoPoint(center.getLatitude() + latOffset, center.getLongitude() + lonOffset);
        GeoPoint se = new GeoPoint(center.getLatitude() - latOffset, center.getLongitude() + lonOffset);
        GeoPoint sw = new GeoPoint(center.getLatitude() - latOffset, center.getLongitude() - lonOffset);

        Polygon rectangle = new Polygon();
        List<GeoPoint> points = new ArrayList<>();
        points.add(nw);
        points.add(ne);
        points.add(se);
        points.add(sw);
        rectangle.setPoints(points);

        rectangle.setFillColor(0x220000FF);  // أزرق شفاف
        rectangle.setStrokeColor(0xFF0000FF); // أزرق واضح
        rectangle.setStrokeWidth(3f);

        map.getOverlays().add(rectangle);

        // إضافة علامة في مركز المستطيل
        Marker marker = new Marker(map);
        marker.setPosition(center);
        marker.setTitle("المستطيل " + (map.getOverlays().size()));
        map.getOverlays().add(marker);

        map.invalidate();
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
    public void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
    }
}

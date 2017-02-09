package com.example.bvlab.screenrecord;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class OrientationAdapter extends ArrayAdapter<String>
{
    private Map<String, String> mapOrientaion;

    public OrientationAdapter(final Context context, final int n, final Map<String, String> mapOrientaion) {
        super(context, n, new ArrayList(mapOrientaion.keySet()));
        this.setDropDownViewResource(R.layout.resolution_spinner_dropdown_item);
        this.mapOrientaion = mapOrientaion;
    }

    public static OrientationAdapter createAdapter(final Activity activity, final Configuration configuration) {
        final String string = "Portrait";
        final String string2 = "Landscape";
        final LinkedHashMap<String, String> linkedHashMap = new LinkedHashMap<String, String>();
        final Object[] array = { null };
        String s;
        if (configuration.orientation == 1) {
            s = string;
        }
        else {
            s = string2;
        }
        array[0] = s;
        linkedHashMap.put(String.format("%s (current)", array), "ORIENTATION_CURRENT");
        linkedHashMap.put(string, "ORIENTATION_PORTRAIT");
        linkedHashMap.put(string2, "ORIENTATION_LANDSCAPE");
        return new OrientationAdapter(activity, R.layout.resolution_spinner_item, linkedHashMap);
    }

    public String getOrientation(final int n) {
        Iterator<Map.Entry<String, String>> iterator = this.mapOrientaion.entrySet().iterator();
        int n2 = 0;
        while (iterator.hasNext()) {
            final Map.Entry entry = (Map.Entry)iterator.next();
            if (n2 == n) {
                return (String)(entry).getValue();
            }
            ++n2;
        }
        return "ORIENTATION_CURRENT";
    }

    public int getOrientationPosition(final String s) {
        final Iterator<Map.Entry<String, String>> iterator = this.mapOrientaion.entrySet().iterator();
        int n = 0;
        while (iterator.hasNext()) {
            if (Objects.equals(s, iterator.next().getValue())) {
                return n;
            }
            ++n;
        }
        return 0;
    }
}


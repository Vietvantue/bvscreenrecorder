package com.example.bvlab.screenrecord;

import android.app.Activity;
import android.content.Context;
import android.print.PrintAttributes;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ResolutionAdapter extends ArrayAdapter<String>
{
    private Map<String, Resolution> mapResolution;

    public ResolutionAdapter(final Context context, final int n, final Map<String, Resolution> mapResolution) {
        super(context, n, new ArrayList(mapResolution.keySet()));
        this.setDropDownViewResource(R.layout.resolution_spinner_dropdown_item);
        this.mapResolution = mapResolution;
    }

    public static ResolutionAdapter createAdapter(final Activity activity, final int n) {
        final LinkedHashMap<String, Resolution> linkedHashMap = new LinkedHashMap<String, Resolution>();
        for (final Resolution resolution : ResolutionHelper.getRecordingResolutionsForScreenSize(activity, n)) {
            linkedHashMap.put(resolution.getName(), resolution);
        }
        return new ResolutionAdapter(activity, R.layout.resolution_spinner_item, linkedHashMap);
    }

    public int getPosition(final Resolution resolution) {
        final Iterator<Map.Entry<String, Resolution>> iterator = this.mapResolution.entrySet().iterator();
        int n = 0;
        while (iterator.hasNext()) {
            if (Objects.equals(resolution, iterator.next().getValue())) {
                return n;
            }
            ++n;
        }
        return 0;
    }

    public PrintAttributes.Resolution getResolution(final int n) {
        final Iterator<Map.Entry<String, Resolution>> iterator = this.mapResolution.entrySet().iterator();
        int n2 = 0;
        while (iterator.hasNext()) {
            final Map.Entry entry = (Map.Entry)iterator.next();
            if (n2 == n) {
                return (PrintAttributes.Resolution)(entry).getValue();
            }
            ++n2;
        }
        return null;
    }
}

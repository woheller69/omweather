package org.woheller69.weather.ui.util;

import org.osmdroid.views.overlay.TilesOverlay;

public class TilesOverlayEntry {
    private TilesOverlay tilesOverlay;
    private long time;

    public TilesOverlayEntry(TilesOverlay tilesOverlay, long time) {
        this.tilesOverlay = tilesOverlay;
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TilesOverlayEntry that = (TilesOverlayEntry) o;
        return time == that.time;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(time);
    }

    public TilesOverlay getTilesOverlay() {
        return tilesOverlay;
    }

    public long getTime() {
        return time;
    }

}

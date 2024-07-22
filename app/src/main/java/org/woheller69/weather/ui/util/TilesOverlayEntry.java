package org.woheller69.weather.ui.util;

import org.osmdroid.views.overlay.TilesOverlay;

public class TilesOverlayEntry {
    private final TilesOverlay tilesOverlay;
    private final long time;

    public TilesOverlayEntry(TilesOverlay tilesOverlay, long time) {
        this.tilesOverlay = tilesOverlay;
        this.time = time;
    }

    public TilesOverlay getTilesOverlay() {
        return tilesOverlay;
    }

    public long getTime() {
        return time;
    }

}

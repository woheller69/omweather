package org.woheller69.weather.ui;

import org.woheller69.weather.R;

/**
 * This static class provides image / icon resources for the UI.
 */
public class UiResourceProvider {

    /**
     * Private constructor in order to make this class static.
     */
    private UiResourceProvider() {
    }

    /**
     * @param categoryNumber The category number. See IApiToDatabaseConversion#WeatherCategories
     *                       for details.
     * @param isDay          True if TimeStamp between sunrise and sunset
     * @return Returns the icon (resource) that belongs to the given category number.
     */
    public static int getIconResourceForWeatherCategory(int categoryNumber, boolean isDay) {
        switch (categoryNumber) {
            case 10:
                if (isDay) {
                    return R.mipmap.weather_icon_10d;
                } else {
                    return R.mipmap.weather_icon_10n;
                }
            case 20:
                if (isDay) {
                    return R.mipmap.weather_icon_20d;
                } else {
                    return R.mipmap.weather_icon_20n;
                }
            case 30:
                if (isDay) {
                    return R.mipmap.weather_icon_30d;
                } else {
                    return R.mipmap.weather_icon_30n;
                }
            case 40:
                if (isDay) {
                    return R.mipmap.weather_icon_40d;
                } else {
                    return R.mipmap.weather_icon_40n;
                }
            case 45:
                if (isDay) {
                    return R.mipmap.weather_icon_45d;
                } else {
                    return R.mipmap.weather_icon_45n;
                }
            case 50:
                if (isDay) {
                    return R.mipmap.weather_icon_50d;
                } else {
                    return R.mipmap.weather_icon_50n;
                }
            case 60:
                if (isDay) {
                    return R.mipmap.weather_icon_60d;
                } else {
                    return R.mipmap.weather_icon_60n;
                }
            case 70:
                if (isDay) {
                    return R.mipmap.weather_icon_70d;
                } else {
                    return R.mipmap.weather_icon_70n;
                }
            case 71:
                if (isDay) {
                    return R.mipmap.weather_icon_71d;
                } else {
                    return R.mipmap.weather_icon_71n;
                }
            case 72:
                if (isDay) {
                    return R.mipmap.weather_icon_72d;
                } else {
                    return R.mipmap.weather_icon_72n;
                }
            case 74:
                if (isDay) {
                    return R.mipmap.weather_icon_74d;
                } else {
                    return R.mipmap.weather_icon_74n;
                }
            case 75:
                if (isDay) {
                    return R.mipmap.weather_icon_75d;
                } else {
                    return R.mipmap.weather_icon_75n;
                }
            case 80:
                if (isDay) {
                    return R.mipmap.weather_icon_80d;
                } else {
                    return R.mipmap.weather_icon_80n;
                }
            case 81:
                if (isDay) {
                    return R.mipmap.weather_icon_81d;
                } else {
                    return R.mipmap.weather_icon_81n;
                }
            case 82:
                if (isDay) {
                    return R.mipmap.weather_icon_82d;
                } else {
                    return R.mipmap.weather_icon_82n;
                }
            case 83:
                if (isDay) {
                    return R.mipmap.weather_icon_83d;
                } else {
                    return R.mipmap.weather_icon_83n;
                }
            case 84:
                if (isDay) {
                    return R.mipmap.weather_icon_84d;
                } else {
                    return R.mipmap.weather_icon_84n;
                }
            case 85:
                if (isDay) {
                    return R.mipmap.weather_icon_85d;
                } else {
                    return R.mipmap.weather_icon_85n;
                }
            case 86:
                if (isDay) {
                    return R.mipmap.weather_icon_86d;
                } else {
                    return R.mipmap.weather_icon_86n;
                }
            case 90:
                if (isDay) {
                    return R.mipmap.weather_icon_90d;
                } else {
                    return R.mipmap.weather_icon_90n;
                }
            default:
                if (isDay) {
                    return R.mipmap.weather_icon_30d;
                } else {
                    return R.mipmap.weather_icon_30n;
                }
        }
    }

    /**
     * @param categoryNumber The category number. See IApiToDatabaseConversion#WeatherCategories
     *                       for details.
     * @param isDay          True if TimeStamp between sunrise and sunset
     * @return Returns the image resource that belongs to the given category number.
     */
    public static int getImageResourceForWeatherCategory(int categoryNumber, boolean isDay) {
        switch (categoryNumber) {
            case 10:
                if (isDay) {
                    return R.drawable.weather_image_10d;
                } else {
                    return R.drawable.weather_image_10n;
                }
            case 20:
                if (isDay) {
                    return R.drawable.weather_image_20d;
                } else {
                    return R.drawable.weather_image_20n;
                }
            case 30:
                if (isDay) {
                    return R.drawable.weather_image_30d;
                } else {
                    return R.drawable.weather_image_30n;
                }
            case 40:
                if (isDay) {
                    return R.drawable.weather_image_40d;
                } else {
                    return R.drawable.weather_image_40n;
                }
            case 45:
                if (isDay) {
                    return R.drawable.weather_image_45d;
                } else {
                    return R.drawable.weather_image_45n;
                }
            case 50:
                if (isDay) {
                    return R.drawable.weather_image_50d;
                } else {
                    return R.drawable.weather_image_50n;
                }
            case 60:
                if (isDay) {
                    return R.drawable.weather_image_60d;
                } else {
                    return R.drawable.weather_image_60n;
                }
            case 70:
                if (isDay) {
                    return R.drawable.weather_image_70d;
                } else {
                    return R.drawable.weather_image_70n;
                }
            case 71:
                if (isDay) {
                    return R.drawable.weather_image_71d;
                } else {
                    return R.drawable.weather_image_71n;
                }
            case 72:
                if (isDay) {
                    return R.drawable.weather_image_72d;
                } else {
                    return R.drawable.weather_image_72n;
                }
            case 74:
                if (isDay) {
                    return R.drawable.weather_image_74d;
                } else {
                    return R.drawable.weather_image_74n;
                }
            case 75:
                if (isDay) {
                    return R.drawable.weather_image_75d;
                } else {
                    return R.drawable.weather_image_75n;
                }
            case 80:
                if (isDay) {
                    return R.drawable.weather_image_80d;
                } else {
                    return R.drawable.weather_image_80n;
                }
            case 81:
                if (isDay) {
                    return R.drawable.weather_image_81d;
                } else {
                    return R.drawable.weather_image_81n;
                }
            case 82:
                if (isDay) {
                    return R.drawable.weather_image_82d;
                } else {
                    return R.drawable.weather_image_82n;
                }
            case 83:
                if (isDay) {
                    return R.drawable.weather_image_83d;
                } else {
                    return R.drawable.weather_image_83n;
                }
            case 84:
                if (isDay) {
                    return R.drawable.weather_image_84d;
                } else {
                    return R.drawable.weather_image_84n;
                }
            case 85:
                if (isDay) {
                    return R.drawable.weather_image_85d;
                } else {
                    return R.drawable.weather_image_85n;
                }
            case 86:
                if (isDay) {
                    return R.drawable.weather_image_86d;
                } else {
                    return R.drawable.weather_image_86n;
                }
            case 90:
                if (isDay) {
                    return R.drawable.weather_image_90d;
                } else {
                    return R.drawable.weather_image_90n;
                }
            default:
                if (isDay) {
                    return R.drawable.weather_image_30d;
                } else {
                    return R.drawable.weather_image_30n;
                }
        }
    }

}

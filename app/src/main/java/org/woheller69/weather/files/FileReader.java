package org.woheller69.weather.files;

import org.woheller69.weather.database.City;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides functionality for reading files.
 */
public class FileReader {

    /**
     * @param is The input stream of the file to read.
     * @return Returns a list of City instances.
     * @throws IOException In case the file cannot be read this exception will be thrown.
     */
    public List<City> readCitiesFromFile(InputStream is) throws IOException {
        List<City> cities = new ArrayList<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        // Skip the first line as it contains headings
        String line = br.readLine();
        while ((line = br.readLine()) != null) {
            // id, name, country code, lon, lat
            String[] fields = line.split("\t");

            int id = (int) Float.parseFloat(fields[0]);
            String name = fields[1];
            String countryCode = fields[2];
            float lon = Float.parseFloat(fields[3]);
            float lat = Float.parseFloat(fields[4]);
            cities.add(new City(id, name, countryCode, lon, lat));
        }
        br.close();
        return cities;
    }
}

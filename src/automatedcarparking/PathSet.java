/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automatedcarparking;

import automatedcarparking.graphics.Util;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.joml.Vector2f;

/**
 *
 * @author nikki
 */
public class PathSet {
    
    private final List<Vector2f[]> paths;
    
    public static PathSet load(String path) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(Util.findResource(path)))) {
            ArrayList<Vector2f[]> paths = new ArrayList<>();
            for (int i = 0; ; ++i) {
                String line = reader.readLine();
                if (line == null) break;
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] pairList = line.split(";");
                Vector2f[] arr = new Vector2f[pairList.length];
                for (int j = 0; j < pairList.length; ++j) {
                    String[] pair = pairList[j].split(",");
                    try {
                        float x = Float.parseFloat(pair[0]);
                        float y = Float.parseFloat(pair[1]);
                        arr[j] = new Vector2f(x, y);
                    } catch (NumberFormatException ex) {
                        throw new IOException(ex);
                    }
                }
                paths.add(arr);
            }
            if (paths.isEmpty())
                throw new IOException("Error loading path set: no paths in file");
            return new PathSet(paths);
        }
    }
    
    private PathSet(List<Vector2f[]> paths) {
        this.paths = paths;
    }
    
    public int numPaths() {
        return paths.size();
    }
    
    public Vector2f[] getPath(int i) {
        return paths.get(i);
    }
    
    public int numPoints(int path) {
        return getPath(path).length;
    }
    
    public Vector2f getPoint(int path, int index) {
        return getPath(path)[index];
    }
    
}

package spaceboys.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Hazards {

    private Map<String, List<Hazard>> data;

    public Hazards(){
        data = new HashMap<>();
    }

    public void put(String layer, List<Hazard> layerHazards){
        data.put(layer,layerHazards);
    }

    public List<Hazard> get(String layer){
        return data.get(layer);
    }
}

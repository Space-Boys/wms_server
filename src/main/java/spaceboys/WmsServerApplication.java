package spaceboys;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import spaceboys.api.Hazard;
import spaceboys.api.Hazards;
import spaceboys.resources.ImageResource;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import java.nio.file.Files;

public class WmsServerApplication extends Application<WmsServerConfiguration> {



    public static void main(final String[] args) throws Exception {
        new WmsServerApplication().run(args);
    }

    @Override
    public String getName() {
        return "spaceboys_wms_server";
    }

    @Override
    public void initialize(final Bootstrap<WmsServerConfiguration> bootstrap) {
        bootstrap.addBundle(new AssetsBundle("/assets", "/", "index.html"));
    }

    @Override
    public void run(final WmsServerConfiguration configuration,
                    final Environment environment) throws IOException {

        environment.jersey().setUrlPattern("/wms/*");

        final FilterRegistration.Dynamic cors =
                environment.servlets().addFilter("CORS", CrossOriginFilter.class);

        // Configure CORS parameters
        cors.setInitParameter("allowedOrigins", "*");
        cors.setInitParameter("allowedHeaders", "X-Requested-With,Content-Type,Accept,Origin");
        cors.setInitParameter("allowedMethods", "OPTIONS,GET,PUT,POST,DELETE,HEAD");

        // Add URL mapping
        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");

        // DO NOT pass a preflight request to down-stream auth filters
        // unauthenticated preflight requests should be permitted by spec
        cors.setInitParameter(CrossOriginFilter.CHAIN_PREFLIGHT_PARAM, Boolean.FALSE.toString());

        Hazards hazards = readHazards();
        environment.jersey().register(new ImageResource(hazards));
    }

    private Hazards readHazards() throws IOException {
        Hazards hazards = new Hazards();

        List<String> lines = Files.readAllLines(Paths.get("hazards.tsv"));
        for (String line : lines){
            try {
                String[] splits = line.split("\t");
                String layer = splits[0];
                if (layer.startsWith("#")){
                    continue;
                }
                String date = splits[1];
                double x = Double.parseDouble(splits[2]);
                double y = Double.parseDouble(splits[3]);
                double size = Double.parseDouble(splits[4]);
                double intensity = Double.parseDouble(splits[5]);
                Hazard hazard = new Hazard();
                hazard.setX(x);
                hazard.setY(y);
                hazard.setDate(date);
                hazard.setIntensity(intensity);
                hazard.setSize(size);

                hazard.setR(Integer.parseInt(splits[6]));
                hazard.setG(Integer.parseInt(splits[7]));
                hazard.setB(Integer.parseInt(splits[8]));

                List<Hazard> layerHazards = hazards.get(layer);
                if (layerHazards == null) {
                    layerHazards = new ArrayList<>();
                    hazards.put(layer, layerHazards);
                }
                layerHazards.add(hazard);
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        return hazards;
    }

}

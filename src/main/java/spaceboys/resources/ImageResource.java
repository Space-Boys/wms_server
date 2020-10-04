package spaceboys.resources;

import spaceboys.api.Coordinates;
import spaceboys.api.Hazard;
import spaceboys.api.Hazards;

import javax.imageio.ImageIO;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Path("/image")
public class ImageResource {
    private Hazards hazards;

    public ImageResource(Hazards hazards){
        this.hazards = hazards;
    }

    @GET
    public Response getImage(@QueryParam("DEBUG") String req_debug,
                             @QueryParam("TIME") String req_time,
                             @QueryParam("WIDTH") String req_width,
                             @QueryParam("HEIGHT") String req_height,
                             @QueryParam("SRS") String req_srs,
                             @QueryParam("LAYERS") String req_layers,
                             @QueryParam("BBOX") String req_bbox) throws IOException {

        boolean debug = req_debug!=null;
        //boolean debug = true;
        int imageWidth = Integer.parseInt(req_width);
        int imageHeight = Integer.parseInt(req_height);

        BufferedImage bi = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D ig2 = bi.createGraphics();

        Coordinates displayBox = new Coordinates(req_bbox);

        Font font = new Font("TimesRoman", Font.BOLD, 30);
        ig2.setFont(font);

        FontMetrics fontMetrics = ig2.getFontMetrics();

        int stringHeight = fontMetrics.getAscent();
        ig2.setPaint(Color.darkGray);

        if (debug) {
            // Print Time
            String message = req_time;
            int stringWidth = fontMetrics.stringWidth(message);
            ig2.drawString(message, (imageWidth - stringWidth) / 2, imageHeight / 2 + stringHeight / 4);

            // Print BBOX
            message = req_bbox;
            stringWidth = fontMetrics.stringWidth(message);
            ig2.drawString(message, (imageWidth - stringWidth) / 2, imageHeight / 2 + 6 * (stringHeight / 4));

            double x_avg = (displayBox.getX1() + displayBox.getX2()) / 2.0;
            int c1 = (int) (((x_avg + 180.0) / 360.0) * 255);

            double y_avg = (displayBox.getY1() + displayBox.getY2()) / 2.0;
            int c2 = (int) (((y_avg + 180.0) / 360.0) * 255);

            Color coordinatesColor = new Color(100, c1, c2);
            ig2.setPaint(coordinatesColor);
            message = displayBox.getX1() + " -> " + displayBox.getX2() + "  |  " + displayBox.getY1() + " -> " + displayBox.getY2();
            stringWidth = fontMetrics.stringWidth(message);
            ig2.drawString(message, (imageWidth - stringWidth) / 2, imageHeight / 2 + 12 * (stringHeight / 4));

            // Draw edge border
            ig2.setPaint(Color.gray);
            ig2.drawRect(0, 0, imageWidth - 1, imageHeight - 1);
        }

        // Draw hazards
        List<Hazard> layerHazards = hazards.get(req_layers);
        if (layerHazards!=null){
            for (Hazard hazard : layerHazards){
                if (req_time.startsWith(hazard.getDate())) {

                    double boxWidth = displayBox.getX2() - displayBox.getX1();
                    double hazardBoxX = (hazard.getX() - displayBox.getX1()) * imageWidth / boxWidth;
                    double hazardBoxWidth = (hazard.getSize()) * imageWidth / boxWidth;

                    double boxHeight = displayBox.getY2() - displayBox.getY1();
                    double hazardBoxY = (displayBox.getY2() - hazard.getY()) * imageHeight / boxHeight;
                    double hazardBoxHeight = (hazard.getSize()) * imageHeight / boxHeight;

                    //ig2.setPaint(new Color((float) (hazard.getIntensity()), 0.1f, 0.1f));
                    ig2.setPaint(new Color((int) (hazard.getR()*hazard.getIntensity()), (int) (hazard.getG()*hazard.getIntensity()), (int) (hazard.getB()*hazard.getIntensity())));
                    //ig2.fillOval((int) (hazardBoxX - hazardBoxWidth / 2), (int) (hazardBoxY - hazardBoxHeight / 2), (int) hazardBoxWidth, (int) hazardBoxHeight);
                    ig2.fillRect((int) (hazardBoxX - hazardBoxWidth / 2), (int) (hazardBoxY - hazardBoxHeight / 2), (int) hazardBoxWidth, (int) hazardBoxHeight);
                }
            }
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(bi,"png", os);
        InputStream fis = new ByteArrayInputStream(os.toByteArray());

        MediaType  mediaType = new MediaType("image", "png");

        return Response.ok(fis)
                .type(mediaType)
                .build();
    }
}
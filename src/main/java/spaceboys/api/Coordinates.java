package spaceboys.api;

public class Coordinates {

    private double x1;
    private double x2;
    private double y1;
    private double y2;

    public Coordinates(String bbox){
        String[] splits = bbox.split(",");
        x1=Double.parseDouble(splits[0]);
        y1=Double.parseDouble(splits[1]);
        x2=Double.parseDouble(splits[2]);
        y2=Double.parseDouble(splits[3]);
    }

    public double getX1() {
        return x1;
    }

    public void setX1(double x1) {
        this.x1 = x1;
    }

    public double getX2() {
        return x2;
    }

    public void setX2(double x2) {
        this.x2 = x2;
    }

    public double getY1() {
        return y1;
    }

    public void setY1(double y1) {
        this.y1 = y1;
    }

    public double getY2() {
        return y2;
    }

    public void setY2(double y2) {
        this.y2 = y2;
    }
}

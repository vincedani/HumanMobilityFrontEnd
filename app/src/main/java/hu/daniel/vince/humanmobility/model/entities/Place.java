package hu.daniel.vince.humanmobility.model.entities;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Unique;

/**
 * Created by Daniel Vince (vinced@inf.u-szeged.hu) on 2017-07-17.
 */

@Entity
public class Place {

    // region Members

    @Id(autoincrement = true)
    private Long id;

    private String type;

    @Unique
    private String title;

    private double latitude;

    private double longitude;

    private int radius;

    // endregion

    // region Constructors

    public Place(String type, String title, double latitude, double longitude, int radius) {
        setType(type);
        setTitle(title);
        setLongitude(longitude);
        setLatitude(latitude);
        setRadius(radius);
    }

    @Generated(hash = 198498444)
    public Place(Long id, String type, String title, double latitude, double longitude, int radius) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
    }

    @Generated(hash = 1170019414)
    public Place() {
    }

    // endregion

    // region Getters

    public Long getId() {
        return this.id;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getRadius() {
        return radius;
    }

    // endregion

    // region Setters

    public void setId(Long id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setLatitude(double latitude) throws IllegalArgumentException {
        if(latitude < 0 || latitude > 90)
            throw new IllegalArgumentException("The Latitude value should be between 0 and 90.");

        this.latitude = latitude;
    }

    public void setLongitude(double longitude) throws IllegalArgumentException {
        if(longitude < 0 || longitude > 90)
            throw new IllegalArgumentException("The Longitude value should be between 0 and 90.");

        this.longitude = longitude;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    // endregion

    // region JSON

    public String toJson() {
        return
                "{" +
                        "Type: " + "\"" + type + "\"," +
                        "Title:" + "\"" + title + "\"," +
                        "Latitude:" + "\"" + latitude + "\"," +
                        "Longitude:" + "\"" + longitude + "\"," +
                        "Radius:" + "\"" + radius + "\"," +
                "}";
    }

    // endregion
}

package hu.daniel.vince.humanmobility.model.converters;

import hu.daniel.vince.humanmobility.model.typeHelpers.PlaceType;

public class PlaceTypeConverter {
    public static PlaceType convertToPlaceType(String dbValue) {
            return PlaceType.valueOf(dbValue);
    }

    public static String convertToDbType(PlaceType type) {
        return type.toString();
    }
}

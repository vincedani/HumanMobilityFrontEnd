package hu.daniel.vince.humanmobility.model.converters;

import org.greenrobot.greendao.converter.PropertyConverter;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Created by Daniel Vince (vinced@inf.u-szeged.hu) on 2017-07-25.
 */

public class JodaTimeConverter implements PropertyConverter<DateTime, Long> {

    DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMddHHmmssSSS");

    @Override
    public DateTime convertToEntityProperty(Long databaseValue) {
        return formatter.parseDateTime(databaseValue.toString());
    }

    @Override
    public Long convertToDatabaseValue(DateTime entityProperty) {
        return Long.parseLong(formatter.print(entityProperty));
    }
}

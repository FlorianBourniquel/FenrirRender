package sample.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class TimestampDeserializer implements JsonDeserializer<Calendar>
{
    @Override
    public Calendar deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException
    {
        long time = Long.parseLong(json.getAsString());
        time = time * 1000;
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTimeInMillis(time);
        return calendar;
    }
}
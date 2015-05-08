package org.kidinov.justweight.util;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orm.query.Condition;
import com.orm.query.Select;

import org.kidinov.justweight.model.Weight;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by akid on 17/04/15.
 */
public class DbHelper {
    private static final String TAG = "DbHelper";

    public static Weight getTodaysRecord() {
        return Select.from(Weight.class)
                .where(Condition.prop("date").eq(EnvUtil.getLocalFromString(EnvUtil.getFormattedDate(System.currentTimeMillis())).getTime())).first();
    }

    public static Weight getRecordByDate(Long date) {
        return Select.from(Weight.class).where(Condition.prop("date").eq(date)).first();
    }

    public static List<Weight> getAllRecords() {
        return Select.from(Weight.class).orderBy("date").list();
    }

    public static List<Weight> getRecordsBetweenDates(long start, long end) {
        start--;
        end++;
        return Select.from(Weight.class).where(Condition.prop("date").gt(start), Condition.prop("date").lt(end)).orderBy("date").list();
    }

    public static List<Weight> getRecordsBetweenDatesDesc(long start, long end) {
        start--;
        end++;
        return Select.from(Weight.class).where(Condition.prop("date").gt(start), Condition.prop("date").lt(end)).orderBy("date desc").list();
    }

    public static Long getFirstDate() {
        return Select.from(Weight.class).orderBy("date").first().getDate();
    }

    public static Long getLastDate() {
        return Select.from(Weight.class).orderBy("date desc").first().getDate();
    }

    public static void fillTestData() {
        Weight.deleteAll(Weight.class);
//        Calendar c = Calendar.getInstance();
//        int lastWeight = 600;
//        for (int i = 0; i < 30; i++) {
//            c.setTime(new Date());
//            c.add(Calendar.DATE, -i);
//            c.set(Calendar.MILLISECOND, 0);
//            c.set(Calendar.SECOND, 0);
//            c.set(Calendar.MINUTE, 0);
//            c.set(Calendar.HOUR, 0);
//            lastWeight = (lastWeight + ((int) (Math.random() * i))) / 2;
//            new Weight(c.getTimeInMillis(), lastWeight, "kg").save();
//        }

        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        new Weight(c.getTimeInMillis(), 696, "kg").save();
        c.add(Calendar.DATE, -1);
        new Weight(c.getTimeInMillis(), 703, "kg").save();
        c.add(Calendar.DATE, -1);
        new Weight(c.getTimeInMillis(), 700, "kg").save();
        c.add(Calendar.DATE, -1);
        new Weight(c.getTimeInMillis(), 713, "kg").save();
        c.add(Calendar.DATE, -1);
        new Weight(c.getTimeInMillis(), 715, "kg").save();
        c.add(Calendar.DATE, -1);
        new Weight(c.getTimeInMillis(), 722, "kg").save();
        c.add(Calendar.DATE, -1);
        new Weight(c.getTimeInMillis(), 727, "kg").save();
        c.add(Calendar.DATE, -1);
        new Weight(c.getTimeInMillis(), 729, "kg").save();
        c.add(Calendar.DATE, -1);
        new Weight(c.getTimeInMillis(), 736, "kg").save();
        c.add(Calendar.DATE, -1);
        new Weight(c.getTimeInMillis(), 745, "kg").save();
        c.add(Calendar.DATE, -1);
        new Weight(c.getTimeInMillis(), 750, "kg").save();
    }

    public static int exportAllDataToFile(String path) {
        Gson g = new Gson();
        List<Weight> weights = getAllRecords();
        if (weights == null || weights.isEmpty()) {
            return -1;
        }

        for (Weight w : weights) {
            w.setId(null);
        }
        return FileHelper.writeTextToFile(path, g.toJson(weights));
    }

    public static int importDataFromFile(String path) {
        Gson g = new Gson();
        String s = FileHelper.readTextFromFile(path);
        Type listType = new TypeToken<ArrayList<Weight>>() {
        }.getType();
        List<Weight> weights;
        try {
            weights = g.fromJson(s, listType);
        } catch (Exception e) {
            Log.e(TAG, "", e);
            return -1;
        }
        if (weights == null || weights.isEmpty()) {
            Log.d(TAG, String.format("nothing have read from JSON = %s", s));
            return -1;
        }

        Weight.deleteAll(Weight.class);
        for (Weight newWeight : weights) {
            newWeight.setId(null);
            newWeight.save();
        }
        return 1;
    }
}

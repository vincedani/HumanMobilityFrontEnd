package hu.daniel.vince.humanmobility.model.handlers.database;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.io.File;
import java.util.List;

import hu.daniel.vince.humanmobility.model.converters.JodaTimeConverter;
import hu.daniel.vince.humanmobility.model.entities.ActivityLevel;
import hu.daniel.vince.humanmobility.model.entities.ActivityLevelDao;
import hu.daniel.vince.humanmobility.model.entities.ApplicationUser;
import hu.daniel.vince.humanmobility.model.entities.ApplicationUserDao;
import hu.daniel.vince.humanmobility.model.entities.BugReport;
import hu.daniel.vince.humanmobility.model.entities.BugReportDao;
import hu.daniel.vince.humanmobility.model.entities.DaoMaster;
import hu.daniel.vince.humanmobility.model.entities.DaoSession;
import hu.daniel.vince.humanmobility.model.entities.Location;
import hu.daniel.vince.humanmobility.model.entities.LocationDao;
import hu.daniel.vince.humanmobility.model.entities.Overview;
import hu.daniel.vince.humanmobility.model.entities.OverviewDao;
import hu.daniel.vince.humanmobility.model.entities.Place;
import hu.daniel.vince.humanmobility.model.entities.PlaceDao;

/**
 * Created by Daniel Vince (vinced@inf.u-szeged.hu) on 2017-07-25.
 */

public class DatabaseHandler {

    // region Members

    private static final String DATABASE_NAME = "humanMobility-db";
    private static final String LOG_TAG = "DatabaseHandler";

    private static volatile DatabaseHandler instance;
    private static volatile DaoSession daoSession;

    private ActivityLevelDao activityDao;
    private ApplicationUserDao userDao;
    private BugReportDao bugReportDao;
    private LocationDao locationDao;
    private OverviewDao overviewDao;
    private PlaceDao placeDao;

    private JodaTimeConverter jodaTimeConverter;

    // endregion

    // region Constructors

    private DatabaseHandler(final Context context) {
        if(daoSession == null) {
            File path = new File(Environment.getExternalStorageDirectory(),
                    "humanMobility/database/" + DATABASE_NAME);
            path.getParentFile().mkdirs();

            DaoMaster.DevOpenHelper helper =
                    new DaoMaster.DevOpenHelper(context, path.getAbsolutePath(), null);
            org.greenrobot.greendao.database.Database db = helper.getWritableDb();

            DaoMaster daoMaster = new DaoMaster(db);
            daoSession = daoMaster.newSession();
        }

        activityDao = daoSession.getActivityLevelDao();
        bugReportDao = daoSession.getBugReportDao();
        locationDao = daoSession.getLocationDao();
        overviewDao = daoSession.getOverviewDao();
        placeDao = daoSession.getPlaceDao();
        userDao = daoSession.getApplicationUserDao();

        jodaTimeConverter = new JodaTimeConverter();
    }

    // endregion

    // region Instance

    public static DatabaseHandler getInstance(final Context context) {
        if(instance == null)
            synchronized (DatabaseHandler.class) {
                if(instance == null)
                    instance = new DatabaseHandler(context);
            }

        return instance;
    }


    // endregion

    // region Insert entities

    public boolean addLocation(Location location) {
        boolean hasError = false;

        if(location.getAccuracy() > 200)
            location.setError(true);

        if(location.getError())
            hasError = true;


        try {
            locationDao.insert(location);
            addOverviewElement(hasError);
            return true;
        } catch (Exception e) {
            Log.e(LOG_TAG,
                    String.format("addLocation exception, message: %s", e.getMessage()));
            return false;
        }
    }

    public boolean addPlace(Place place) {
        try {
            placeDao.insert(place);
            return true;
        } catch (Exception e) {
            Log.e(LOG_TAG,
                    String.format("addPlace exception, message: %s", e.getMessage()));
            return false;
        }

    }

    public boolean addPlaces(List<Place> places) {

        if(places.isEmpty())
            return true;

        for(Place place : places) {
            if(place.getId() != null)
                return false;
        }

        try {
            placeDao.insertInTx(places);
            return true;
        } catch (Exception e) {
            Log.e(LOG_TAG,
                    String.format("addPlaces exception, message: %s", e.getMessage()));
            return false;
        }
    }

    public boolean addBugReport(String message) {
        try {
            BugReport report = new BugReport(message);
            bugReportDao.insert(report);
            return true;
        } catch (Exception e) {
            Log.e(LOG_TAG,
                    String.format("addBugReport exception, message: %s", e.getMessage()));
            return false;
        }
    }

    private void addOverviewElement(boolean hasError) {
        Overview todayOverview = getOverview();

        // Update
        if(todayOverview != null) {
            if(!hasError) {
                todayOverview.setSuccess(todayOverview.getSuccess() + 1);
            } else {
                todayOverview.setError(todayOverview.getError() + 1);
            }
        // Create
        } else {
            todayOverview = new Overview();
            LocalDate today = DateTime.now().toLocalDate();
            DateTime startOfToday = today.toDateTimeAtStartOfDay(DateTime.now().getZone());

            todayOverview.setDate(startOfToday);

            if(!hasError)
                todayOverview.setSuccess(1);
            else
                todayOverview.setError(1);
        }

        try {
            overviewDao.insertOrReplace(todayOverview);
        } catch (Exception e) {
            Log.e(LOG_TAG,
                    String.format("addOverview exception, message: %s", e.getMessage()));
        }

    }

    public boolean addOrUpdateUser(final ApplicationUser user) {
        ApplicationUser entity = getUser();

        if(entity != null) {
            if(!entity.getUserName().equals(user.getUserName()))
                return false;

            // Update token
            if(!entity.getToken().equals(user.getToken()))
                entity.setToken(user.getToken());

        } else {
            entity = new ApplicationUser(user.getUserName(), user.getToken());
        }

        entity.setIsLoggedIn(user.getIsLoggedIn());

        try {
            userDao.insertOrReplace(entity);
            return true;
        } catch (Exception e) {
            Log.e(LOG_TAG,
                    String.format("addUser exception, message: %s", e.getMessage()));
            return false;
        }
    }

    public void addActivityLevel(DateTime timeStamp, float x, float y, float z) {
        try {
            Long dbValue = jodaTimeConverter.convertToDatabaseValue(timeStamp);
            activityDao.insertOrReplace(new ActivityLevel(dbValue, x, y, z));
        } catch (Exception e) {
            Log.e(LOG_TAG,
                    String.format("addActivityLevel exception, message: %s", e.getMessage()));
        }
    }

    // endregion

    // region Read entities

    public ApplicationUser getUser() {
        return userDao.queryBuilder().unique();
    }

    public List<Location> getLocations() {
        return locationDao.loadAll();
    }

    public List<Place> getPlaces() {
        return placeDao.loadAll();
    }

    public List<BugReport> getBugReports() {
        return bugReportDao.loadAll();
    }

    public List<Overview> getOverviews() {
        return overviewDao.queryBuilder().orderDesc(OverviewDao.Properties.Date).list();
    }

    private Overview getOverview() {
        LocalDate today = DateTime.now().toLocalDate();
        DateTime startOfToday = today.toDateTimeAtStartOfDay(DateTime.now().getZone());

        Long dbValue = jodaTimeConverter.convertToDatabaseValue(startOfToday);

        return overviewDao.queryBuilder()
                .where(OverviewDao.Properties.Date.eq(dbValue))
                .unique();
    }

    public List<ActivityLevel> getActivityLevels() {
        return activityDao.queryBuilder()
                .where(ActivityLevelDao.Properties.IsDeleted.eq(false))
                .limit(5000)
                .list();
    }

    // endregion

    // region Delete entities

    public void deleteLocations(List<Location> locations) {
        locationDao.deleteInTx(locations);
    }

    public void deletePlace(Long placeId) {
        placeDao.deleteByKey(placeId);
    }

    public void deleteBugReports(List<BugReport> reports) {
        bugReportDao.deleteInTx(reports);
    }

    public void deleteActivityLevels(List<ActivityLevel> levels) {
        for (int i = 0; i < levels.size(); i++) {
            levels.get(i).setIsDeleted(true);
        }
        activityDao.insertOrReplaceInTx(levels);
    }

    public void cleanDatabase() {
        DaoMaster.dropAllTables(daoSession.getDatabase(), true);
        DaoMaster.createAllTables(daoSession.getDatabase(), true);
    }

    public void clearCache() {
        daoSession.clear();
    }

    // endregion
}

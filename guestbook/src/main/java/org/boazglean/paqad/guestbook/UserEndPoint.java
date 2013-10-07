package org.boazglean.paqad.guestbook;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import lombok.Data;
import org.boazglean.paqad.guestbook.dto.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.UUID;

/**
 * User: mpower
 * Date: 10/6/13
 * Time: 8:11 PM
 */
@Data
@Path("user")
@Produces({"application/json", "application/xml"})
public class UserEndPoint {

    public static final Logger logger = LoggerFactory.getLogger(UserEndPoint.class);

    UserService userService = null;
    DatastoreService datastore = null;

    @GET
    public UserDto getCurrentUser() {
        UserDto dto =  new UserDto();
        User user = getUserService().getCurrentUser();
        if (user != null) {
            dto.setEmail(user.getEmail());
            dto.setNickname(user.getNickname());
            Query query = new Query("Host").setFilter(new Query.FilterPredicate("externalId", Query.FilterOperator.EQUAL, user.getUserId()));
            Entity host = getDatastore().prepare(query).asSingleEntity();
            if (host != null) {
                String uuidRaw = null;
                try {
                    uuidRaw = host.getProperty("id").toString();
                    dto.setId(UUID.fromString(uuidRaw));
                } catch (IllegalArgumentException e) {
                    logger.error("Failed to get user id, id is not a uuid: {}", uuidRaw);
                }
            }
            dto.setSignOutUrl(getUserService().createLogoutURL("placeholder"));

        }
        else {
            //Put in a place holder for the return url
            dto.setSignInUrl(getUserService().createLoginURL("placeholder"));
        }
        return dto;
    }

    @POST
    public UserDto addCurrentUser() {
        UUID uuid = null;
        User user = getUserService().getCurrentUser();
        //We should figure out how not to double pump on the database
        Query query = new Query("Host").setFilter(new Query.FilterPredicate("externalId", Query.FilterOperator.EQUAL, user.getUserId()));
        Entity host = getDatastore().prepare(query).asSingleEntity();
        if (host == null) {
            if (user != null) {
                uuid = UUID.randomUUID();
                host = new Entity("Host");
                host.setProperty("id", uuid.toString());
                host.setProperty("externalId", user.getUserId());
                host.setProperty("email", user.getEmail());
                host.setProperty("nickname", user.getNickname());

                getDatastore().put(host);
            }
        }
        return getCurrentUser();
    }

    public UserService getUserService() {
        if (userService == null) {
            //poor man's lazy loading
            userService = UserServiceFactory.getUserService();
        }

        return userService;
    }

    public DatastoreService getDatastore() {
        if (datastore == null) {
            //poor man's lazy loading
            datastore = DatastoreServiceFactory.getDatastoreService();
        }
        return datastore;
    }
}

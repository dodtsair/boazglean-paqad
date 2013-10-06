package org.boazglean.paqad.guestbook;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import lombok.Getter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

/**
 * User: mpower
 * Date: 10/6/13
 * Time: 9:07 AM
 */
public class GuestBookServlet extends HttpServlet{

    UserService userService = null;
    DatastoreService datastore = null;

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        User author = getUserService().getCurrentUser();
        //Replace with constant
        String guestbook = req.getParameter("guestbookName");
        //Replace with constant
        String body = req.getParameter("content");
        Date date = new Date();
        UUID host = null;
        try {
            //Replace with constant
             host = UUID.fromString(req.getParameter("host"));
        }
        catch(IllegalArgumentException e) {
            //Replace with constant
            host = UUID.fromString("00000000-0000-0000-0000-000000000000");
        }

        createPosting(guestbook, author, body, date, host);

        resp.sendRedirect("/guestbook.jsp?guestbookName=" + guestbook + "&host=" + host.toString());
    }

    public void createPosting(String guestbook, User author, String body, Date date, UUID host) {
        //TODO replace with constant
        Key hostKey = KeyFactory.createKey("Host", host.toString());
        Key guestbookKey = KeyFactory.createKey(hostKey, "Guestbook", guestbook);
        Entity posting = new Entity("Posting", guestbookKey);
        //Really so what happens when you set an object as a property?
        //Does it have to be a specific family of objects? serializable? conventional?
        posting.setProperty("author", author);
        posting.setProperty("body", body);
        posting.setProperty("date", date);

        getDatastore().put(posting);
    }

    public UserService getUserService() {
        if(userService == null) {
            //poor man's lazy loading
            userService = UserServiceFactory.getUserService();
        }

        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public DatastoreService getDatastore() {
        if(datastore == null) {
            //poor man's lazy loading
            datastore = DatastoreServiceFactory.getDatastoreService();
        }
        return datastore;
    }

    public void setDatastore(DatastoreService datastore) {
        this.datastore = datastore;
    }

}

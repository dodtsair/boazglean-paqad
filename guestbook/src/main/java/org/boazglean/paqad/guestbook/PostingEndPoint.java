package org.boazglean.paqad.guestbook;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import lombok.Data;
import org.boazglean.paqad.guestbook.dto.PostingDto;
import org.boazglean.paqad.guestbook.dto.PostingsDto;
import org.boazglean.paqad.guestbook.dto.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import java.util.*;

/**
 * User: mpower
 * Date: 10/6/13
 * Time: 8:11 PM
 */
@Data
@Path("posting")
@Produces({"application/json", "application/xml"})
@Consumes({"application/json", "application/xml"})
public class PostingEndPoint {

    public static final Logger logger = LoggerFactory.getLogger(PostingEndPoint.class);

    UserService userService = null;
    DatastoreService datastore = null;

    @GET
    public PostingsDto getPostings(@QueryParam("host") String host,@QueryParam("guestbook") String guestbook) {
        return getPostings(new PostingDto(null, null, null, guestbook, host));
    }

    public PostingsDto getPostings(PostingDto dto) {
        if(dto.getGuestbook() == null || dto.getGuestbook().isEmpty()) {
            throw new IllegalArgumentException("Guestbook must not be empty");
        }
        if(dto.getHost() == null || dto.getHost().isEmpty()) {
            dto.setHost("00000000-0000-0000-0000-000000000000");
        }
        Key hostKey = KeyFactory.createKey("Host", dto.getHost());
        Key guestbookKey = KeyFactory.createKey(hostKey, "Guestbook", dto.getGuestbook());
        // Run an ancestor query to ensure we see the most up-to-date
        // view of the Greetings belonging to the selected Guestbook.
        Query query = new Query("Posting", guestbookKey).addSort("date", Query.SortDirection.DESCENDING);
        List<Entity> postings = getDatastore().prepare(query).asList(FetchOptions.Builder.withLimit(5));
        List<PostingDto> dtos = new ArrayList<PostingDto>(5);
        if(postings != null) {
            for(Entity entity: postings) {
                dtos.add(convertPostingEntity(entity, dto));
            }
        }

        return new PostingsDto(dtos);
    }

    public PostingDto convertPostingEntity(Entity entity, PostingDto general) {
        Object postingIdRaw = entity.getProperty("posting_id");
        Object authorRaw = entity.getProperty("author");
        Object bodyRaw = entity.getProperty("body");

        UUID postingId = postingIdRaw != null ? UUID.fromString(postingIdRaw.toString()) : null;
        String author = authorRaw != null ? authorRaw.toString() : null;
        String body = bodyRaw != null ? bodyRaw.toString() : null;

        logger.info("Dto source, posting_id: {}, author: {}, body: {}", postingId ,author, body);
        PostingDto dto = new PostingDto(postingId, author, body, general.getGuestbook(), general.getHost());
        return dto;
    }


    @POST
    public PostingDto addPosting(PostingDto dto) {
        User user = getUserService().getCurrentUser();
        Date date = new Date();
        dto.setId(UUID.randomUUID());
        UUID host = UUID.fromString(dto.getHost());
        createPosting(dto.getId(), dto.getGuestbook(), user, dto.getBody(), date, host);
        return dto;
    }

    public void createPosting(UUID postingId, String guestbook, User author, String body, Date date, UUID host) {
        //TODO replace with constant
        Key hostKey = KeyFactory.createKey("Host", host.toString());
        Key guestbookKey = KeyFactory.createKey(hostKey, "Guestbook", guestbook);
        Entity posting = new Entity("Posting", guestbookKey);
        //Really so what happens when you set an object as a property?
        //Does it have to be a specific family of objects? serializable? conventional?
        posting.setProperty("posting_id", postingId.toString());
        posting.setProperty("author", author);
        posting.setProperty("body", body);
        posting.setProperty("date", date);

        getDatastore().put(posting);
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

package org.boazglean.paqad.guestbook;

import org.testng.annotations.Test;

import java.util.UUID;

/**
 * User: mpower
 * Date: 10/6/13
 * Time: 2:22 PM
 */
public class Sandbox {


    @Test
    public void testUuid() {
        UUID uuid;
        uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");
        System.out.println("Uuid in string:" + uuid.toString());
    }
}

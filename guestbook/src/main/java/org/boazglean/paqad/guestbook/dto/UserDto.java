package org.boazglean.paqad.guestbook.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.UUID;

/**
 * User: mpower
 * Date: 10/6/13
 * Time: 8:55 PM
 */
@Data
@XmlRootElement(name = "User")
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    UUID id;
    String email;
    String nickname;
    String signInUrl;
    String signOutUrl;
}

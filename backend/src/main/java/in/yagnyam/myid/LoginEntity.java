package in.yagnyam.myid;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(staticName = "of")
@Setter
@Getter
@ToString
@Slf4j
@Entity
public class LoginEntity {

    @Id
    private String session;

    private String authToken;
}

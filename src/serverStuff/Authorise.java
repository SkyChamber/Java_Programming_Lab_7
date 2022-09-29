package serverStuff;

import java.io.Serializable;

public class Authorise implements Serializable {
    public Authorise(String login, String password){
        this.login = login;
        this.password = password;
    }

    private String login;
    private String password;

    public String getLogin(){
        return login;
    }
    public String getPassword(){
        return password;
    }
}

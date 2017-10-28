package com.JustinThyme.justinthymer.models.forms;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Set;

@Entity
public class User {

    @Id
    @GeneratedValue
    private int id;

    @NotNull
    //regex pattern prevents empty string but allows spaces within the string
    @Pattern(regexp="(.|\\s)*\\S(.|\\s)*", message="Name must not be empty.")
    public String username;

    @NotNull
    @Size(min=6, message="Passwords must be at least six characters.")
    private String password;


    //note NotNull ? or keep optional IF user wants updates
    //note needs to be string for twillio
    //note something like this ?
    //@Pattern(regexp="([2-9][0-8][0-9])[2-9][0-9]{2}-[0-9]{4}", message="Not a valid number")
    @Pattern(regexp = "[(][2-9][0-8][0-9][)][2-9][0-9]{2}-[0-9]{4}", message="Not a valid number, use (XXX)XXX-XXXX format")
    private String phoneNumber;

    @NotNull
    private Seed.Area area;

    private long sessionId;

    @NotNull
    private boolean loggedIn;


    @ManyToMany
    @JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    private Set<Role>roles;

    public Set<Role> getRoles() {
        return roles;
    }


    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }


    public User(String username, String password, Set<Role> roles, Seed.Area area, String phoneNumber, Long sessionId, Boolean loggedIn) {
        this.username = username;
        this.password = password;
        this.roles = roles;
        this.area = area;
        this.phoneNumber = phoneNumber;
        this.sessionId = sessionId;
        this.loggedIn = loggedIn;

    }

    public User() { }


    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Seed.Area getArea() {
        return area;
    }

    public void setArea(Seed.Area area) {
        this.area = area;
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }





    }


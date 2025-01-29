package com.basic.oauth.dto;

import java.util.Date;

import com.basic.oauth.entity.User;

public class CreateUserRequest {
    private String name;
    private String mobile;
    private String email;
    private String addressline1;
    private String addressline2;
    private String addressline3;
    private String city;
    private String state;
    private Date birthdate;
    private String gender;
    private String usertype;
    private String username;
    private String password;
    private String status = "ACTIVE"; // default value
    
    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getUsertype() { return usertype; }
    public void setUsertype(String usertype) { this.usertype = usertype; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    // Convert DTO to Entity
    public User toEntity() {
        User user = new User();
        user.setName(this.name);
        user.setMobile(this.mobile);
        user.setEmail(this.email);
        user.setAddressline1(this.addressline1);
        user.setAddressline2(this.addressline2);
        user.setAddressline3(this.addressline3);
        user.setCity(this.city);
        user.setState(this.state);
        user.setBirthdate(this.birthdate);
        user.setGender(this.gender);
        user.setUsertype(this.usertype);
        user.setUsername(this.username);
        user.setPassword(this.password);
        user.setStatus(this.status);
        return user;
    }
}
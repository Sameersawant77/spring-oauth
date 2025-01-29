package com.basic.oauth.entity;

import java.io.Serializable;
import java.util.Date;

import org.hibernate.annotations.*;

import jakarta.persistence.*;
import jakarta.persistence.Table;

@Entity
@Table(
    name = "AUTH_USER",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"username", "status"})}
)
@DynamicInsert
@DynamicUpdate
public class User implements Serializable {
    
    private static final long serialVersionUID = 7256750113502032717L;
    
    @Id
    @Column(name = "userId", length = 19)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_generator")
    @SequenceGenerator(name = "user_generator", initialValue = 1, allocationSize = 1)
    private Long id;
    
    @Column(length = 500)
    private String name;
    
    @Column(length = 15)
    private String mobile;
    
    @Column(length = 320)
    private String email;
    
    @Column(length = 60)
    private String addressline1;
    
    @Column(length = 60)
    private String addressline2;
    
    @Column(length = 60)
    private String addressline3;
    
    @Column(length = 100)
    private String city;
    
    @Column(length = 100)
    private String state;
    
    @Column(columnDefinition = "TIMESTAMP")
    private Date birthdate;
    
    @Column(length = 20)
    private String gender;
    
    @Column(length = 100)
    private String usertype;
    
    @Column(length = 100)
    private String username;
    
    @Column(length = 100)
    private String password;
    
    @Column(length = 10)
    private String vendorcode;
    
    @Column(length = 10)
    private String agentcode;
    
    @Column(columnDefinition = "TIMESTAMP")
    private Date lastlogintime;
    
    @Column(length = 60)
    private String status;
    
    @Column(length = 60)
    private String nextstatus;
    
    private Long referenceid;
    
    @Column(name = "version")
    private Long version;
    
    private String sessionid;
    
    @Column(name = "DEFAULT_MENU", length = 40)
    private String defaultmenuapp;
    
    @Column(length = 20)
    private String clientid;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsertype() {
        return usertype;
    }

    public void setUsertype(String usertype) {
        this.usertype = usertype;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAddressline1() {
		return addressline1;
	}

	public void setAddressline1(String addressline1) {
		this.addressline1 = addressline1;
	}

	public String getAddressline2() {
		return addressline2;
	}

	public void setAddressline2(String addressline2) {
		this.addressline2 = addressline2;
	}

	public String getAddressline3() {
		return addressline3;
	}

	public void setAddressline3(String addressline3) {
		this.addressline3 = addressline3;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Date getBirthdate() {
		return birthdate;
	}

	public void setBirthdate(Date birthdate) {
		this.birthdate = birthdate;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getVendorcode() {
		return vendorcode;
	}

	public void setVendorcode(String vendorcode) {
		this.vendorcode = vendorcode;
	}

	public String getAgentcode() {
		return agentcode;
	}

	public void setAgentcode(String agentcode) {
		this.agentcode = agentcode;
	}

	public Date getLastlogintime() {
		return lastlogintime;
	}

	public void setLastlogintime(Date lastlogintime) {
		this.lastlogintime = lastlogintime;
	}

	public String getNextstatus() {
		return nextstatus;
	}

	public void setNextstatus(String nextstatus) {
		this.nextstatus = nextstatus;
	}

	public Long getReferenceid() {
		return referenceid;
	}

	public void setReferenceid(Long referenceid) {
		this.referenceid = referenceid;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public String getSessionid() {
		return sessionid;
	}

	public void setSessionid(String sessionid) {
		this.sessionid = sessionid;
	}

	public String getDefaultmenuapp() {
		return defaultmenuapp;
	}

	public void setDefaultmenuapp(String defaultmenuapp) {
		this.defaultmenuapp = defaultmenuapp;
	}

	public String getClientid() {
		return clientid;
	}

	public void setClientid(String clientid) {
		this.clientid = clientid;
	}
   
}
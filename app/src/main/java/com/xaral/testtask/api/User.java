package com.xaral.testtask.api;


/**
 * Represents a user with personal and positional details.
 */
public class User {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String position;
    private Integer positionId;
    private Long registrationTime;
    private String photo;

    public User(Long id, String name, String email, String phone, String position, Integer positionId, Long registrationTime, String photo) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.position = position;
        this.positionId = positionId;
        this.registrationTime = registrationTime;
        this.photo = photo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        String newPhone = "+38";
        String tempPhone = phone;
        if (phone.startsWith("+")) {
            tempPhone = phone.substring(3);
        } else {
            tempPhone = phone.substring(2);
        }
        newPhone += " (" + tempPhone.substring(0, 3) + ") " + tempPhone.substring(3, 6) + " " + tempPhone.substring(6, 8) + " " + tempPhone.substring(8, 10);
        return newPhone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Integer getPositionId() {
        return positionId;
    }

    public void setPositionId(Integer positionId) {
        this.positionId = positionId;
    }

    public Long getRegistrationTime() {
        return registrationTime;
    }

    public void setRegistrationTime(Long registrationTime) {
        this.registrationTime = registrationTime;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }
}

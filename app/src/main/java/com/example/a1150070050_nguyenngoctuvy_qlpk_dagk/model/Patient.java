package com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.model;

public class Patient {
    private int id;
    private String fullName;
    private String dob;
    private String gender;
    private String phone;
    private String address;

    public Patient(int id, String fullName, String dob, String gender, String phone, String address) {
        this.id = id;
        this.fullName = fullName;
        this.dob = dob;
        this.gender = gender;
        this.phone = phone;
        this.address = address;
    }

    public int getId() { return id; }
    public String getFullName() { return fullName; }
    public String getDob() { return dob; }
    public String getGender() { return gender; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }

    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setDob(String dob) { this.dob = dob; }
    public void setGender(String gender) { this.gender = gender; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setAddress(String address) { this.address = address; }
}

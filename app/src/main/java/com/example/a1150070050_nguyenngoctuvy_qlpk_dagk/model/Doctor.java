package com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.model;

public class Doctor {
    private int id;
    private String fullname;
    private String specialty;
    private String phone;

    public Doctor(int id, String fullname, String specialty, String phone) {
        this.id = id;
        this.fullname = fullname;
        this.specialty = specialty;
        this.phone = phone;
    }

    public int getId() { return id; }
    public String getFullname() { return fullname; }
    public String getSpecialty() { return specialty; }
    public String getPhone() { return phone; }

    public void setFullname(String fullname) { this.fullname = fullname; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
    public void setPhone(String phone) { this.phone = phone; }
}

package com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

/** Model Patient khớp API: Id, FullName, Dob(yyyy-MM-dd), Gender, Phone, Address */
public class Patient {
    private int id;
    private String fullName;
    private String dob;      // yyyy-MM-dd hoặc null
    private String gender;   // M/F/O hoặc null
    private String phone;    // có thể null
    private String address;  // có thể null

    public Patient() {}

    public Patient(int id, String fullName, String dob, String gender, String phone, String address) {
        this.id = id;
        this.fullName = fullName;
        this.dob = dob;
        this.gender = gender;
        this.phone = phone;
        this.address = address;
    }

    // ===== getters & setters =====
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    // ===== JSON helpers =====
    /** Parse 1 bệnh nhân từ JSON trả về của API */
    public static Patient fromJson(JSONObject o) {
        Patient p = new Patient();
        p.id       = o.optInt("id");
        p.fullName = o.optString("fullName", "");
        p.dob      = o.isNull("dob") ? null : o.optString("dob", null); // server trả "yyyy-MM-dd" hoặc null
        p.gender   = o.isNull("gender") ? null : o.optString("gender", null);
        p.phone    = o.isNull("phone") ? null : o.optString("phone", null);
        p.address  = o.isNull("address") ? null : o.optString("address", null);
        return p;
    }

    /** JSON body cho POST */
    public JSONObject toCreateJson() throws JSONException {
        JSONObject j = new JSONObject();
        j.put("fullName", fullName == null ? "" : fullName);
        if (dob != null && !dob.isEmpty()) j.put("dob", dob);
        j.put("gender", gender == null ? "" : gender);
        j.put("phone", phone == null ? "" : phone);
        j.put("address", address == null ? "" : address);
        return j;
    }

    /** JSON body cho PUT */
    public JSONObject toUpdateJson() throws JSONException {
        JSONObject j = toCreateJson();
        j.put("id", id);
        return j;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Patient)) return false;
        Patient patient = (Patient) o;
        return id == patient.id;
    }
    @Override public int hashCode() { return Objects.hash(id); }

    @Override public String toString() {
        return "Patient{" + "id=" + id + ", name='" + fullName + '\'' + '}';
    }
}

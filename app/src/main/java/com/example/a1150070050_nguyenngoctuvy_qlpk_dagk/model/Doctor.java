package com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

/** Model Doctor khớp API: Id, FullName, Specialty, Phone */
public class Doctor {
    private int id;
    private String fullname;
    private String specialty;
    private String phone;   // có thể null

    public Doctor() {}

    public Doctor(int id, String fullname, String specialty, String phone) {
        this.id = id;
        this.fullname = fullname;
        this.specialty = specialty;
        this.phone = phone;
    }

    // ===== getters & setters =====
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }

    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    // ===== JSON helpers (tiện cho gọi API) =====
    /** Parse 1 bác sĩ từ JSON trả về của API */
    public static Doctor fromJson(JSONObject o) {
        Doctor d = new Doctor();
        d.id        = o.optInt("id");
        d.fullname  = o.optString("fullName", "");
        d.specialty = o.optString("specialty", "");
        d.phone     = o.isNull("phone") ? null : o.optString("phone", null);
        return d;
    }

    /** JSON body cho POST */
    public JSONObject toCreateJson() throws JSONException {
        JSONObject j = new JSONObject();
        j.put("fullName", fullname == null ? "" : fullname);
        j.put("specialty", specialty == null ? "" : specialty);
        j.put("phone", phone == null ? "" : phone);
        return j;
    }

    /** JSON body cho PUT */
    public JSONObject toUpdateJson() throws JSONException {
        JSONObject j = toCreateJson();
        j.put("id", id);
        return j;
    }

    // chỉ so sánh theo id để tiện cập nhật list
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Doctor)) return false;
        Doctor doctor = (Doctor) o;
        return id == doctor.id;
    }
    @Override public int hashCode() { return Objects.hash(id); }

    @Override public String toString() {
        return "Doctor{" + "id=" + id + ", name='" + fullname + '\'' + '}';
    }
}

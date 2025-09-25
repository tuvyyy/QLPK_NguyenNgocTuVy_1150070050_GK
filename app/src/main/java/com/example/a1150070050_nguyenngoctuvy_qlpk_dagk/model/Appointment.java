package com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.model;

public class Appointment {
    private int id;
    private int patientId;
    private int doctorId;
    private int serviceId;
    private String appointmentDate; // ISO: 2025-09-25T09:00:00
    private String status;

    // display
    private String patientName;
    private String doctorName;
    private String serviceName;

    public Appointment(int id, int patientId, int doctorId, int serviceId,
                       String appointmentDate, String status,
                       String patientName, String doctorName, String serviceName) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.serviceId = serviceId;
        this.appointmentDate = appointmentDate;
        this.status = status;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.serviceName = serviceName;
    }

    public int getId() { return id; }
    public int getPatientId() { return patientId; }
    public int getDoctorId() { return doctorId; }
    public int getServiceId() { return serviceId; }
    public String getAppointmentDate() { return appointmentDate; }
    public String getStatus() { return status; }
    public String getPatientName() { return patientName; }
    public String getDoctorName() { return doctorName; }
    public String getServiceName() { return serviceName; }

    public void setStatus(String status) { this.status = status; }
}

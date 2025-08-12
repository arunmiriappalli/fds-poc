package com.example.fds.model;

import jakarta.persistence.*;

@Entity
@Table(name = "devices")
public class Device {
    @Id
    @Column(name = "device_id")
    public String deviceId;

    @Column(name = "reg_lat")
    public Double regLat;

    @Column(name = "reg_lon")
    public Double regLon;
}

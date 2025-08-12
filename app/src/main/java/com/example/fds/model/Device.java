package com.example.fds.model;

import jakarta.persistence.*;

@Entity
@Table(name = "devices")
public class Device {
    @Id
    @Column(name = "device_id")
    public String deviceId;
}

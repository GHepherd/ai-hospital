package com.gjh.xiaozi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gjh.xiaozi.entity.Appointment;
import org.springframework.stereotype.Service;

public interface AppointmentService extends IService<Appointment> {
    Appointment checkExist(Appointment appointment);
}

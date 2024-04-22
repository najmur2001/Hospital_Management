package com.hospital.demo.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hospital.demo.dto.AppointmentResponseDto;
import com.hospital.demo.dto.CommanApiResponse;
import com.hospital.demo.dto.UpdateAppointmentRequest;
import com.hospital.demo.entity.Appointment;
import com.hospital.demo.entity.User;
import com.hospital.demo.exception.AppointmentNotFoundException;
import com.hospital.demo.service.AppointmentService;
import com.hospital.demo.service.UserService;
import com.hospital.demo.utility.Constants.AppointmentStatus;
import com.hospital.demo.utility.Constants.ResponseCode;



@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class AppointmentController {

    Logger LOG = LoggerFactory.getLogger(AppointmentController.class);

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private UserService userService;

    @PostMapping("/api/appointment/patient/add")
    
    public ResponseEntity<?> addAppointment(@RequestBody Appointment appointment) {
        LOG.info("Received request to add patient appointment");

        CommanApiResponse response = new CommanApiResponse();

        if (appointment == null || appointment.getPatientId() == 0) {
            response.setResponseCode(ResponseCode.FAILED.value());
            response.setResponseMessage("Failed to add patient appointment");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        appointment.setDate(LocalDate.now().toString());
        appointment.setStatus(AppointmentStatus.NOT_ASSIGNED_TO_DOCTOR.value());

        Appointment addedAppointment = appointmentService.addAppointment(appointment);

        if (addedAppointment != null) {
            response.setResponseCode(ResponseCode.SUCCESS.value());
            response.setResponseMessage("Appointment Added");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            response.setResponseCode(ResponseCode.FAILED.value());
            response.setResponseMessage("Failed to add Appointment");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/api/appointment/all")
    public ResponseEntity<?> getAllAppointments() {
        LOG.info("Received request for getting ALL Appointments !!!");

        List<Appointment> appointments = appointmentService.getAllAppointment();
        List<AppointmentResponseDto> response = new ArrayList<>();

        for (Appointment appointment : appointments) {
            AppointmentResponseDto a = new AppointmentResponseDto();
            populateAppointmentResponseDto(appointment, a);
            response.add(a);
        }

        LOG.info("Response sent!!!");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/appointment/id")
    public ResponseEntity<?> getAppointmentById(@RequestParam("appointmentId") int appointmentId) {
        LOG.info("Received request for getting Appointment by id !!!");

        Appointment appointment = appointmentService.getAppointmentById(appointmentId);

        if (appointment == null) {
            throw new AppointmentNotFoundException();
        }

        AppointmentResponseDto a = new AppointmentResponseDto();
        populateAppointmentResponseDto(appointment, a);

        LOG.info("Response sent!!!");
        return ResponseEntity.ok(a);
    }

    @GetMapping("/api/appointment/patient/id")
    public ResponseEntity<?> getAppointmentsByPatientId(@RequestParam("patientId") int patientId) {
        LOG.info("Received request for getting ALL Appointments by patient Id !!!");

        List<Appointment> appointments = appointmentService.getAppointmentByPatientId(patientId);
        List<AppointmentResponseDto> response = new ArrayList<>();

        for (Appointment appointment : appointments) {
            AppointmentResponseDto a = new AppointmentResponseDto();
            populateAppointmentResponseDto(appointment, a);
            response.add(a);
        }

        LOG.info("Response sent!!!");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/appointment/doctor/id")
    public ResponseEntity<?> getAppointmentsByDoctorId(@RequestParam("doctorId") int doctorId) {
        LOG.info("Received request for getting ALL Appointments by doctor Id !!!");

        List<Appointment> appointments = appointmentService.getAppointmentByDoctorId(doctorId);
        List<AppointmentResponseDto> response = new ArrayList<>();

        for (Appointment appointment : appointments) {
            AppointmentResponseDto a = new AppointmentResponseDto();
            populateAppointmentResponseDto(appointment, a);
            response.add(a);
        }

        LOG.info("Response sent!!!");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/appointment/admin/assign/doctor")
    
    public ResponseEntity<?> assignAppointmentToDoctor(@RequestBody UpdateAppointmentRequest request) {
        LOG.info("Received request to assign appointment to doctor");

        CommanApiResponse response = new CommanApiResponse();

        if (request == null || request.getAppointmentId() == 0 || request.getDoctorId() == 0) {
            response.setResponseCode(ResponseCode.FAILED.value());
            response.setResponseMessage("Invalid request parameters");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        Appointment appointment = appointmentService.getAppointmentById(request.getAppointmentId());

        if (appointment == null) {
            throw new AppointmentNotFoundException();
        }

        appointment.setDoctorId(request.getDoctorId());
        appointment.setStatus(AppointmentStatus.ASSIGNED_TO_DOCTOR.value());

        Appointment updatedAppointment = appointmentService.addAppointment(appointment);

        if (updatedAppointment != null) {
            response.setResponseCode(ResponseCode.SUCCESS.value());
            response.setResponseMessage("Successfully Assigned Appointment to doctor");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            response.setResponseCode(ResponseCode.FAILED.value());
            response.setResponseMessage("Failed to assign");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/appointment/doctor/update")
    
    public ResponseEntity<?> updateAppointmentByDoctor(@RequestBody UpdateAppointmentRequest request) {
        LOG.info("Received request to update appointment by doctor");

        CommanApiResponse response = new CommanApiResponse();

        if (request == null || request.getAppointmentId() == 0) {
            response.setResponseCode(ResponseCode.FAILED.value());
            response.setResponseMessage("Invalid request parameters");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        Appointment appointment = appointmentService.getAppointmentById(request.getAppointmentId());

        if (appointment == null) {
            throw new AppointmentNotFoundException();
        }

        appointment.setPrescription(request.getPrescription());
        appointment.setStatus(request.getStatus());

        if (request.getStatus().equals(AppointmentStatus.TREATMENT_DONE.value())) {
            appointment.setPrice(request.getPrice());
        }

        Appointment updatedAppointment = appointmentService.addAppointment(appointment);

        if (updatedAppointment != null) {
            response.setResponseCode(ResponseCode.SUCCESS.value());
            response.setResponseMessage("Updated Treatment Status");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            response.setResponseCode(ResponseCode.FAILED.value());
            response.setResponseMessage("Failed to update");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/appointment/patient/update")
   
    public ResponseEntity<?> updateAppointmentByPatient(@RequestBody UpdateAppointmentRequest request) {
        LOG.info("Received request to update appointment by patient");

        CommanApiResponse response = new CommanApiResponse();

        if (request == null || request.getAppointmentId() == 0) {
            response.setResponseCode(ResponseCode.FAILED.value());
            response.setResponseMessage("Invalid request parameters");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        Appointment appointment = appointmentService.getAppointmentById(request.getAppointmentId());

        if (appointment == null) {
            throw new AppointmentNotFoundException();
        }

        appointment.setStatus(request.getStatus());
        Appointment updatedAppointment = appointmentService.addAppointment(appointment);

        if (updatedAppointment != null) {
            response.setResponseCode(ResponseCode.SUCCESS.value());
            response.setResponseMessage("Updated Treatment Status");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            response.setResponseCode(ResponseCode.FAILED.value());
            response.setResponseMessage("Failed to update");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void populateAppointmentResponseDto(Appointment appointment, AppointmentResponseDto a) {
        User patient = userService.getUserById(appointment.getPatientId());
        a.setPatientContact(patient.getContact());
        a.setPatientId(patient.getId());
        a.setPatientName(patient.getFirstName() + " " + patient.getLastName());

        if (appointment.getDoctorId() != 0) {
            User doctor = userService.getUserById(appointment.getDoctorId());
            a.setDoctorContact(doctor.getContact());
            a.setDoctorName(doctor.getFirstName() + " " + doctor.getLastName());
            a.setDoctorId(doctor.getId());
            a.setPrescription(appointment.getPrescription());

            if (appointment.getStatus().equals(AppointmentStatus.TREATMENT_DONE.value())) {
                a.setPrice(String.valueOf(appointment.getPrice()));
            } else {
                a.setPrice(AppointmentStatus.TREATMENT_PENDING.value());
            }
        } else {
            a.setDoctorContact(AppointmentStatus.NOT_ASSIGNED_TO_DOCTOR.value());
            a.setDoctorName(AppointmentStatus.NOT_ASSIGNED_TO_DOCTOR.value());
            a.setDoctorId(0);
            a.setPrice(AppointmentStatus.NOT_ASSIGNED_TO_DOCTOR.value());
            a.setPrescription(AppointmentStatus.NOT_ASSIGNED_TO_DOCTOR.value());
        }

        a.setStatus(appointment.getStatus());
        a.setProblem(appointment.getProblem());
        a.setDate(appointment.getDate());
        a.setAppointmentDate(appointment.getAppointmentDate());
        a.setId(appointment.getId());
    }
}

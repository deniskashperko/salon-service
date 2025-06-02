package com.example.salon_service.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestDTO {
    private String date;
    private String time;
    private String procedureName;
    private UserDTO master;
    private String status;
    private String feedback;

    public RequestDTO(Request request) {
        this.date = request.getDate().toString();
        this.time = request.getTime().toString();
        this.procedureName = request.getProcedure().getName();
        this.master = new UserDTO(request.getMaster());
        this.status = request.getStatus();
        this.feedback = request.getFeedback();
    }
}

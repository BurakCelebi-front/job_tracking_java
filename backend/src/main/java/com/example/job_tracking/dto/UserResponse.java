package com.example.job_tracking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class UserResponse {
    private Long id;
    private String fullname;
    private String email;

    public UserResponse(String fullname, String email) {
        this.fullname = fullname;
        this.email = email;
    }
 
    public String getFullName() {
        return fullname;
    }
 
    public String getEmail() {
        return email;
    }
}
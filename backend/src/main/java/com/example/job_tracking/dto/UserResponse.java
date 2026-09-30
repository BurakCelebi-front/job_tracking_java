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

    public UserResponse(String fullName, String email) {
        this.fullName = fullName;
        this.email = email;
    }
 
    public String getFullName() {
        return fullName;
    }
 
    public String getEmail() {
        return email;
    }
}

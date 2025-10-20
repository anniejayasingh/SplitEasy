package com.expensesharingapp.spliteasy.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupRequest {
	
	@NotBlank(message = "Group name is required")
    private String name;
    
    private String desc;
}

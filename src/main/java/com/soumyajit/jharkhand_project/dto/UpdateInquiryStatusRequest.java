package com.soumyajit.jharkhand_project.dto;

import com.soumyajit.jharkhand_project.entity.PropertyInquiry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInquiryStatusRequest {

    private PropertyInquiry.InquiryStatus status;
    private String adminNotes;
}

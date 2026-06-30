package com.group1.recruitment.controller;

import com.group1.recruitment.dto.response.ApplicationDetailResponse;
import com.group1.recruitment.entity.Application;
import com.group1.recruitment.service.ApplicationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/application")
public class ApplicationController {

    private ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/{id}")
    public String detail (@PathVariable Long id, Model model) {
        try {
            ApplicationDetailResponse currentApplication = applicationService.getById(id);
            model.addAttribute("currentApplication", currentApplication);
        } catch (Throwable exception) {
            model.addAttribute("errorMsg", exception.getMessage());
        }
        return "application/detail";
    }
}

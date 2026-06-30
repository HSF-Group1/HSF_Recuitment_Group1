package com.group1.recruitment.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UIExampleController {

    @GetMapping("/")
    public String index() {
        return "redirect:/examples/dashboard";
    }

    @GetMapping("/examples/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("breadcrumbActive", "Dashboard");
        return "examples/dashboard";
    }
}

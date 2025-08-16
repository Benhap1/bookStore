package com.example.book.service.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * A simple controller for handling requests to the application's home page.
 * <p>
 * This controller is responsible for mapping the root URL ("/") and the "/home" URL
 * to the main landing page of the web application. Its only purpose is to ensure

 * that users visiting the site's entry points are directed to the correct view.
 */
@Controller
public class HomeController {

    /**
     * Handles GET requests for the application's root ("/") and "/home" paths.
     * <p>
     * This method is mapped to multiple URL patterns. It simply returns the
     * logical view name of the home page template, which will then be resolved
     * by the view resolver (e.g., Thymeleaf) to render the final HTML page.
     *
     * @return The view name for the home page ("home").
     */
    @GetMapping({"/", "/home"})
    public String home() {
        return "home";
    }
}

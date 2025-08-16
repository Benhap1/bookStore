package com.example.book.service.controller;

import com.example.book.service.exception.AlreadyExistException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.example.book.service.dto.ClientCreateRequestDTO;
import com.example.book.service.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


/**
 * Controller responsible for handling user authentication and registration requests.
 * <p>
 * This controller manages the web pages for user login and new client registration.
 * It does not handle the actual login process itself (which is managed by Spring Security's
 * formLogin mechanism), but it provides the necessary endpoints to display the forms
 * and process the registration data submitted by new users.
 */
@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final ClientService clientService;

    /**
     * Handles GET requests for the login page.
     * <p>
     * Simply returns the name of the Thymeleaf template that renders the login form.
     * The actual authentication is handled by Spring Security's filter chain.
     *
     * @return The view name for the login page ("auth/login").
     */
    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    /**
     * Handles GET requests for the user registration page.
     * <p>
     * Prepares the model with an empty {@link ClientCreateRequestDTO} object, which
     * is used by Thymeleaf to bind form fields.
     *
     * @param model The {@link Model} object to which the form-backing object is added.
     * @return The view name for the registration page ("auth/register").
     */
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("client", new ClientCreateRequestDTO());
        return "auth/register";
    }

    /**
     * Handles POST requests to process the new client registration form.
     * <p>
     * This method performs validation on the submitted data. If validation fails,
     * it returns the user to the registration form to display errors. If validation
     * succeeds, it delegates the creation of the new client to the {@link ClientService}.
     * It also handles potential exceptions, such as an attempt to register with an
     * email that already exists.
     * <p>
     * Upon successful registration, it redirects the user to the login page with a
     * success message.
     *
     * @param clientDto        The data transfer object populated with form data, marked with {@code @Valid} to trigger validation.
     * @param bindingResult    The object that holds the results of the validation and any binding errors.
     * @param redirectAttributes Used to add flash attributes (like a success message) that survive the redirect.
     * @return A redirect string to the login page on success, or the view name of the registration page on failure.
     */
    @PostMapping("/register")
    public String registerNewClient(@Valid @ModelAttribute("client") ClientCreateRequestDTO clientDto,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes) {

        // If validation annotations (e.g., @NotBlank, @Email) fail, return to the form.
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            // Delegate the business logic of creating a client to the service layer.
            clientService.addClient(clientDto);

            // Add a success message that will be displayed on the login page after redirecting.
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Please log in.");

            // Redirect to the login page to prevent form resubmission on refresh.
            return "redirect:/auth/login";
        } catch (AlreadyExistException e) {
            // If the service throws an exception (e.g., email already exists),
            // add a global error to the binding result.
            bindingResult.reject("", "An account with this email already exists.");

            // Return to the registration form to display the error.
            return "auth/register";
        }
    }
}
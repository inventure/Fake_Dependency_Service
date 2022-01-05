package co.tala.example.api.immunization_decider.swagger

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping


@Controller
class SwaggerRedirectController {
    @RequestMapping("/")
    fun redirect(): String {
        return "redirect:/swagger-ui.html"
    }
}

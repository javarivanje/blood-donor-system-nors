package com.bds.swagger;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "mlsbace",
                        email = "mls.bctc@gmail.com"
                ),
                version = "0.0.1",
                title = "OpenApi Blood Donor System - mlsbace",
                description = "OpenApi documentation for Blood Donor system Application " +
                        "used for tracking blood donations, " +
                        "available blood units  and blood donation events",
                license = @License(
                        name = "Blood Donor System License",
                        url = "mls.bctc@gmail.com"
                ),
                termsOfService = "Under my terms ONLY"
        ),
        servers = {
                @Server(
                        description = "Local ENV",
                        url = "http://localhost:8088"
                )
        })
public class OpenApiConfig {
}

package com.sgp.servico_usuarios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ServicoUsuariosApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServicoUsuariosApplication.class, args);
	}

}

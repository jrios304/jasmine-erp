package com.jasmine.erp.web.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.jasmine.erp.clientes.service.conf.ClienteServiceConf;
import com.jasmine.erp.configuracion.service.conf.ConfiguracionServiceConf;

@Configuration
@Import({ ClienteServiceConf.class, ConfiguracionServiceConf.class})
@ComponentScan({ "com.jasmine.erp.web" })
public class JamineErpWebConfig {

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
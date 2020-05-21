package com.jasmine.erp.web.converter;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.jsf.FacesContextUtils;

import com.jasmine.erp.clientes.service.ClienteServiceInterface;
import com.jasmine.erp.clientes.service.dto.ClienteDTO;

/**
 * Conversor para la entidad Cliente
 * @author jrios
 *
 */
@FacesConverter("clienteConverter")
public class ClienteConverter implements Converter{
	
	@Autowired
	ClienteServiceInterface clienteService;

	@PostConstruct
	public void init() {
		FacesContextUtils
    	.getRequiredWebApplicationContext(FacesContext.getCurrentInstance())
    	.getAutowireCapableBeanFactory().autowireBean(this);
	}

	public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
		if (value != null && value.trim().length() > 0) {
			try {
				return clienteService.buscarCliente(Long.parseLong(value));
			} catch (NumberFormatException e) {
				throw new ConverterException(
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "Conversion Error", "Este campo es requerido"));
			}
		} else {
			return null;
		}
	}

	public String getAsString(FacesContext fc, UIComponent uic, Object object) {
		if (object != null) {
			return String.valueOf(((ClienteDTO) object).getId());
		} else {
			return null;
		}
	}
}

package com.jasmine.erp.web.usuario;

import java.io.IOException;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

/**
 * Bean - controler que permite administrar el ingreso a la app
 * @author jeff
 * @version 1.0
 */
@ManagedBean
@ViewScoped
public class LoginBean {
	
	public static final String MESSAGE_ERROR = "Error";

	/**
	 * Permite redireccionar a otra vista
	 * @param url
	 */
	public void redirectUrl(String url) {
		try {
			FacesContext.getCurrentInstance().getExternalContext().redirect(url);
		} catch (IOException e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, MESSAGE_ERROR, e.getMessage()));
		}
	}
}

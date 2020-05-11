package com.jasmine.erp.web.clientes;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.commons.lang.WordUtils;
import org.primefaces.PrimeFaces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.jsf.FacesContextUtils;

import com.jasmine.erp.clientes.service.ClienteServiceInterface;
import com.jasmine.erp.clientes.service.dto.ClienteDTO;
import com.jasmine.erp.configuracion.service.ParametroServiceInterface;
import com.jasmine.erp.configuracion.service.dto.ParametroDTO;

/**
 * Bean - controller, permite administracion de la pagina de gestion clientes
 * @author jeff rios
 * @version 1.0
 */
@ManagedBean
@ViewScoped
public class GestionClientesBean implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String MESSAGE_ERROR = "Error";
	public static final String MESSAGE_EXITO = "Solicitud realizada con éxito";
	
	@Autowired
	ClienteServiceInterface clienteService;
	@Autowired
	ParametroServiceInterface parametroService;
	
	private ClienteDTO clienteDTO;
	private Long idCiudad;
	private List<ParametroDTO> listaCiudades;
	
	@PostConstruct
	public void init() {
		FacesContextUtils.getRequiredWebApplicationContext(FacesContext.getCurrentInstance())
		.getAutowireCapableBeanFactory().autowireBean(this);
		this.clienteDTO = new ClienteDTO();
		this.listaCiudades = new ArrayList<>();
	}
	
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
	
	
	public void abrirPopUpNuevo() {
		listaCiudades = parametroService.obtenerListaPorNombre("CIUDAD");
		PrimeFaces context = PrimeFaces.current();
		context.executeScript("PF('popUpNuevo').show()");
	}
	
	public void cerrarPopUpNuevo() {
		listaCiudades = parametroService.obtenerListaPorNombre("CIUDAD");
		PrimeFaces context = PrimeFaces.current();
		context.executeScript("PF('popUpNuevo').hide()");
	}
	
	public void nuevo() {
		this.clienteDTO.setFechaCreacion(LocalDate.now());
		this.clienteDTO.setNombre(WordUtils.capitalizeFully((this.clienteDTO.getNombre())));
		this.clienteDTO.setCiudad(parametroService.buscarParametroPorID(idCiudad));
		this.clienteDTO = this.clienteService.actualizarCliente(this.clienteDTO);
		if(this.clienteDTO != null) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, MESSAGE_EXITO, "Se ha creado el nuevo cliente."));
			limpiarFormularioCliente();
			cerrarPopUpNuevo();
		}else {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, MESSAGE_ERROR, "Ocurrió un error insertando el cliente."));
		}
	}
	
	public void limpiarFormularioCliente() {
		this.clienteDTO = new ClienteDTO();
		this.idCiudad = null;
	}

	/**
	 * @return the clienteDTO
	 */
	public ClienteDTO getClienteDTO() {
		return clienteDTO;
	}


	/**
	 * @param clienteDTO the clienteDTO to set
	 */
	public void setClienteDTO(ClienteDTO clienteDTO) {
		this.clienteDTO = clienteDTO;
	}

	/**
	 * @return the listaCiudades
	 */
	public List<ParametroDTO> getListaCiudades() {
		return listaCiudades;
	}

	/**
	 * @param listaCiudades the listaCiudades to set
	 */
	public void setListaCiudades(List<ParametroDTO> listaCiudades) {
		this.listaCiudades = listaCiudades;
	}

	/**
	 * @return the idCiudad
	 */
	public Long getIdCiudad() {
		return idCiudad;
	}

	/**
	 * @param idCiudad the idCiudad to set
	 */
	public void setIdCiudad(Long idCiudad) {
		this.idCiudad = idCiudad;
	}
}

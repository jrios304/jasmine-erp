package com.jasmine.erp.web.clientes;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
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
import com.jasmine.erp.clientes.service.dto.ClienteBusquedaDTO;
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
	private ClienteDTO clienteEliminar;
	private ClienteBusquedaDTO clienteBusqueda;
	private Date fechaCreacionFiltro;
	private Long idCiudad;
	private List<ParametroDTO> listaCiudades;
	private List<ParametroDTO> listaCiudadesFiltro;
	private List<ClienteDTO> listaClientes;
	private boolean isEditando;
	
	@PostConstruct
	public void init() {
		FacesContextUtils.getRequiredWebApplicationContext(FacesContext.getCurrentInstance())
		.getAutowireCapableBeanFactory().autowireBean(this);
		this.clienteDTO = new ClienteDTO();
		this.clienteEliminar = new ClienteDTO();
		this.clienteBusqueda = new ClienteBusquedaDTO();
		this.listaCiudades = new ArrayList<>();
		this.actualizarListaCiudadesFiltro();
	}
	
	public void actualizarListaCiudadesFiltro() {
		this.listaCiudadesFiltro = clienteService.obtenerCiudadesFiltro();
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
	
	/**
	 * Permite abrir el popUp de edicion o de agregar
	 */
	public void abrirPopUpNuevo(boolean editando) {
		if(!editando) {
			this.limpiarFormularioCliente();
			this.setEditando(false);
		}
		
		listaCiudades = parametroService.obtenerListaPorNombre("CIUDAD");
		PrimeFaces context = PrimeFaces.current();
		context.executeScript("PF('popUpNuevo').show()");
	}
	
	/**
	 * Permite cerrar el popUop de edicion
	 */
	public void cerrarPopUpNuevo() {
		listaCiudades = parametroService.obtenerListaPorNombre("CIUDAD");
		PrimeFaces context = PrimeFaces.current();
		context.executeScript("PF('popUpNuevo').hide()");
	}
	
	public void abrirPopUpConfirmacion(ClienteDTO clienteEliminar) {
		this.clienteEliminar = clienteEliminar;
		PrimeFaces context = PrimeFaces.current();
		context.executeScript("PF('popUpConfirmacion').show()");
	}
	
	/**
	 * Permite crear un nuevo cliente
	 */
	public void nuevo() {
		String mensaje = "editado";
		if (!isEditando) {
			this.clienteDTO.setFechaCreacion(LocalDate.now());
			mensaje = "creado";
		}
		this.clienteDTO.setNombre(WordUtils.capitalizeFully((this.clienteDTO.getNombre())));
		this.clienteDTO.setCiudad(parametroService.buscarParametroPorID(idCiudad));
		this.clienteDTO = this.clienteService.actualizarCliente(this.clienteDTO);
		if (this.clienteDTO != null) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, MESSAGE_EXITO, "Se ha " + mensaje + " el  cliente."));
			this.limpiarFormularioCliente();
			this.cerrarPopUpNuevo();
			this.obtenerClientesFiltro();
			this.actualizarListaCiudadesFiltro();
		} else {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					MESSAGE_ERROR, "Ocurrió un error insertando el cliente."));
		}
	}
	
	/**
	 * Permite limpiar el formulario de clientes
	 */
	public void limpiarFormularioCliente() {
		this.clienteDTO = new ClienteDTO();
		this.idCiudad = null;
	}
	
	/**
	 * Metodos para las listas de filtros
	 * @param query
	 * @return
	 */
	public List<String> autocompleteNombre(String query){
		return clienteService.obtenerNombresFiltro(query);
	}
	public List<String> autocompleteEmail(String query){
		return this.clienteService.obtenerCorreosFiltro(query);
	}
	public List<String> autocompleteTelefono(String query){
		return this.clienteService.obtenerTelefonosFiltro(query);
	}
	
	/**
	 * Permite obtener el listado de clientes de acuerdo a los filtros de busqueda
	 */
	public void obtenerClientesFiltro() {
		this.clienteBusqueda.setFechaCreacion(this.fechaCreacionFiltro != null ? 
				this.convertDateToLocalDate(this.fechaCreacionFiltro) : null);
		this.listaClientes = clienteService.obtenerClientesFiltro(clienteBusqueda);
	}
	
	/**
	 * Permite limpiar la lista de filtros y la tabla de clientes
	 */
	public void limpiarFiltros() {
		this.clienteBusqueda = new ClienteBusquedaDTO();
		this.fechaCreacionFiltro = null;
		this.obtenerClientesFiltro();
	}

	/**
	 * Permite convertir un date a localdate
	 * @param fecha
	 * @return
	 */
	public LocalDate convertDateToLocalDate(Date fecha) {
		return fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}
	
	/**
	 * Permite iniciar la edicion de un cliente
	 * @param idCliente
	 */
	public void editar(Long idCliente) {
		this.clienteDTO = clienteService.buscarCliente(idCliente);
		this.idCiudad =this.clienteDTO.getCiudad().getId();
		this.setEditando(true);
		abrirPopUpNuevo(this.isEditando);
	}
	
	/**
	 * Permite eliminar un cliente de la base de datos
	 */
	public void eliminarCliente() {
		try {
			clienteService.eliminarCliente(this.clienteEliminar);
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, MESSAGE_EXITO, "Se ha eliminado el cliente."));
			this.clienteEliminar = new ClienteDTO();
			this.obtenerClientesFiltro();
			this.actualizarListaCiudadesFiltro();
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					MESSAGE_ERROR, "Ocurrió un error eliminando el cliente."));
		}
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

	/**
	 * @return the listaCiudadesFiltro
	 */
	public List<ParametroDTO> getListaCiudadesFiltro() {
		return listaCiudadesFiltro;
	}

	/**
	 * @param listaCiudadesFiltro the listaCiudadesFiltro to set
	 */
	public void setListaCiudadesFiltro(List<ParametroDTO> listaCiudadesFiltro) {
		this.listaCiudadesFiltro = listaCiudadesFiltro;
	}

	/**
	 * @return the listaClientes
	 */
	public List<ClienteDTO> getListaClientes() {
		return listaClientes;
	}

	/**
	 * @param listaClientes the listaClientes to set
	 */
	public void setListaClientes(List<ClienteDTO> listaClientes) {
		this.listaClientes = listaClientes;
	}

	/**
	 * @return the clienteBusqueda
	 */
	public ClienteBusquedaDTO getClienteBusqueda() {
		return clienteBusqueda;
	}

	/**
	 * @param clienteBusqueda the clienteBusqueda to set
	 */
	public void setClienteBusqueda(ClienteBusquedaDTO clienteBusqueda) {
		this.clienteBusqueda = clienteBusqueda;
	}

	/**
	 * @return the fechaCreacionFiltro
	 */
	public Date getFechaCreacionFiltro() {
		return fechaCreacionFiltro;
	}

	/**
	 * @param fechaCreacionFiltro the fechaCreacionFiltro to set
	 */
	public void setFechaCreacionFiltro(Date fechaCreacionFiltro) {
		this.fechaCreacionFiltro = fechaCreacionFiltro;
	}

	/**
	 * @return the isEditando
	 */
	public boolean isEditando() {
		return isEditando;
	}

	/**
	 * @param isEditando the isEditando to set
	 */
	public void setEditando(boolean isEditando) {
		this.isEditando = isEditando;
	}

	/**
	 * @return the clienteEliminar
	 */
	public ClienteDTO getClienteEliminar() {
		return clienteEliminar;
	}

	/**
	 * @param clienteEliminar the clienteEliminar to set
	 */
	public void setClienteEliminar(ClienteDTO clienteEliminar) {
		this.clienteEliminar = clienteEliminar;
	}
}

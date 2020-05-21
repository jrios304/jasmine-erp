package com.jasmine.erp.web.clientes;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.primefaces.PrimeFaces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.jsf.FacesContextUtils;

import com.jasmine.erp.clientes.service.CitaServiceInterface;
import com.jasmine.erp.clientes.service.ClienteServiceInterface;
import com.jasmine.erp.clientes.service.dto.CitaBusquedaDTO;
import com.jasmine.erp.clientes.service.dto.CitaDTO;
import com.jasmine.erp.clientes.service.dto.ClienteDTO;
import com.jasmine.erp.configuracion.service.ParametroServiceInterface;
import com.jasmine.erp.configuracion.service.dto.ParametroDTO;

/**
 * Bean - controller, permite administracion de la pagina de gestion citas
 * @author jeff rios
 * @version 1.0
 */
@ManagedBean
@ViewScoped
public class GestionCitasBean implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String MESSAGE_ERROR = "Error";
	public static final String MESSAGE_EXITO = "Solicitud realizada con éxito";
	
	private CitaDTO citaDTO;
	private CitaDTO citaEliminar;
	private CitaBusquedaDTO filtro;
	private Long idTipoServicio;
	private Long idSede;
	private List<ParametroDTO> listaTipoServicio;
	private List<ParametroDTO> listaSedes;
	private List<ParametroDTO> listaTipoServicioFiltro;
	private List<ParametroDTO> listaSedesFiltro;
	private List<CitaDTO> listaCitas;
	private boolean isEditando;
	
	@Autowired
	ClienteServiceInterface clienteService;
	@Autowired
	ParametroServiceInterface parametroService;
	@Autowired
	CitaServiceInterface citaService;
	
	@PostConstruct
	public void init() {
		FacesContextUtils.getRequiredWebApplicationContext(FacesContext.getCurrentInstance())
		.getAutowireCapableBeanFactory().autowireBean(this);
		this.citaDTO = new CitaDTO();
		this.filtro = new CitaBusquedaDTO();
		this.actualizarListasFiltro();
		this.listaCitas = new ArrayList<>();
	}
	
	/**
	 * Permite actualizar las listas de los filtros
	 */
	private void actualizarListasFiltro(){
		this.listaTipoServicioFiltro = this.citaService.obtenerListaTipoServicioFiltro();
		this.listaSedes = this.citaService.obtenerListaSedeFiltro();
	}
	
	/**
	 * Permite crear o editar una cita
	 */
	public void nueva() {
		String mensaje;
		
		if(isEditando) {
			mensaje = "editado";
		}else {
			mensaje = "creado";
		}
		
		this.citaDTO.setTipoServicio(parametroService.buscarParametroPorID(this.idTipoServicio));
		this.citaDTO.setSede(parametroService.buscarParametroPorID(this.idSede));
		this.citaDTO.setCodigo(this.obtenerCodigoCita());
		this.citaDTO = citaService.actualizarCita(this.citaDTO);
		
		if(this.citaDTO != null) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, MESSAGE_EXITO, "Se ha "+mensaje+ " la cita."));
			this.limpiarFormularioCita();
			this.cerrarPopUpNueva();
			this.actualizarListasFiltro();
			this.buscarCitasFiltro();
		}else {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					MESSAGE_ERROR, "Ocurrió un error agendando la cita."));
		}	
	}
	
	/**
	 * Permite inicar con la edicion de una cita, cargando el registro
	 * @param idCita
	 */
	public void editar(Long idCita) {
		this.citaDTO = citaService.buscarCita(idCita);
		this.idSede = this.citaDTO.getSede().getId();
		this.idTipoServicio = this.citaDTO.getTipoServicio().getId();
		this.setEditando(true);	
		abrirPopUpNueva(true);
	}
	
	/**
	 * Permite eliminar una cita de la BD
	 */
	public void eliminarCita() {
		try {
			this.citaService.eliminarCita(this.citaEliminar);
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, MESSAGE_EXITO, "Se ha eliminado la cita."));
			this.citaEliminar = new CitaDTO();
			this.actualizarListasFiltro();
			this.buscarCitasFiltro();
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, MESSAGE_ERROR, "Ocurrió un error eliminando la cita."));
		}	
	}
	
	/**
	 * Genera un codigo para la cita a crear
	 * @return
	 */
	private String obtenerCodigoCita() {
		return new SimpleDateFormat("yyMMddHHmm").format(this.citaDTO.getFechaHora());	
	}
	
	/**
	 * Permite darle formato a una fecha dada
	 * @param fecha
	 * @return
	 */
	public String darFormatoFechaHora(Date fecha) {
		return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(fecha);
	}
	
	/**
	 * Validador para la fecha minima de la cita
	 * @return
	 */
	public Date fechaMinCita() {
		return new Date();
	}
	/**
	 * Permite limpiar los campos del formulario de citas
	 */
	public void limpiarFormularioCita() {
		this.citaDTO = new CitaDTO();
		this.idSede = null;
		this.idTipoServicio = null;
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
	 * Permite obtener un listado de clientes de acuerdo a una parte de su nombre
	 * @param query
	 * @return
	 */
	public List<ClienteDTO> autocompleteClientes(String query){
		return clienteService.obtenerClientesAutocomplete(query);
	}
	
	/**
	 * Autompletes listas filtro
	 * @param query
	 * @return
	 */
	public List<String> autocompleteClientesFiltro(String query){
		return citaService.obtenerListaClienteFiltro(query);
	}
	public List<String> autocompleteCodigosFiltro(String query){
		return citaService.obtenerListaCodigoFiltro(query);
	}
	
	/**
	 * Permite obtener el listado de citas con los criterios de busqueda
	 */
	public void buscarCitasFiltro() {
		this.listaCitas = citaService.obtenerCitasFiltro(this.filtro);
	}
	
	/**
	 * Permite limpiar la lista de filtros y la tabla de citas
	 */
	public void limpiarFiltros() {
		this.filtro = new CitaBusquedaDTO();
		this.buscarCitasFiltro();
	}
	
	/**
	 * Permite abrir el popUp de edicion o de agregar
	 */
	public void abrirPopUpNueva(boolean editando) {
		if(!editando) {
			this.limpiarFormularioCita();
			this.setEditando(false);
		}
		this.listaTipoServicio = parametroService.obtenerListaPorNombre("TIPO_SERVICIO");
		this.listaSedes = parametroService.obtenerListaPorNombre("SEDE");
		PrimeFaces context = PrimeFaces.current();
		context.executeScript("PF('popUpNuevo').show()");
	}
	
	/**
	 * Permite cerrar el popUop de edicion
	 */
	public void cerrarPopUpNueva() {
		PrimeFaces context = PrimeFaces.current();
		context.executeScript("PF('popUpNuevo').hide()");
	}
	
	public void abrirPopUpConfirmacion(CitaDTO citaEliminar) {
		this.citaEliminar = citaEliminar;
		PrimeFaces context = PrimeFaces.current();
		context.executeScript("PF('popUpConfirmacion').show()");
	}

	/**
	 * @return the citaDTO
	 */
	public CitaDTO getCitaDTO() {
		return citaDTO;
	}

	/**
	 * @param citaDTO the citaDTO to set
	 */
	public void setCitaDTO(CitaDTO citaDTO) {
		this.citaDTO = citaDTO;
	}

	/**
	 * @return the filtro
	 */
	public CitaBusquedaDTO getFiltro() {
		return filtro;
	}

	/**
	 * @param filtro the filtro to set
	 */
	public void setFiltro(CitaBusquedaDTO filtro) {
		this.filtro = filtro;
	}

	/**
	 * @return the idTipoServicio
	 */
	public Long getIdTipoServicio() {
		return idTipoServicio;
	}

	/**
	 * @param idTipoServicio the idTipoServicio to set
	 */
	public void setIdTipoServicio(Long idTipoServicio) {
		this.idTipoServicio = idTipoServicio;
	}

	/**
	 * @return the idSede
	 */
	public Long getIdSede() {
		return idSede;
	}

	/**
	 * @param idSede the idSede to set
	 */
	public void setIdSede(Long idSede) {
		this.idSede = idSede;
	}

	/**
	 * @return the listaTipoServicio
	 */
	public List<ParametroDTO> getListaTipoServicio() {
		return listaTipoServicio;
	}

	/**
	 * @param listaTipoServicio the listaTipoServicio to set
	 */
	public void setListaTipoServicio(List<ParametroDTO> listaTipoServicio) {
		this.listaTipoServicio = listaTipoServicio;
	}

	/**
	 * @return the listaSedes
	 */
	public List<ParametroDTO> getListaSedes() {
		return listaSedes;
	}

	/**
	 * @param listaSedes the listaSedes to set
	 */
	public void setListaSedes(List<ParametroDTO> listaSedes) {
		this.listaSedes = listaSedes;
	}

	/**
	 * @return the listaTipoServicioFiltro
	 */
	public List<ParametroDTO> getListaTipoServicioFiltro() {
		return listaTipoServicioFiltro;
	}

	/**
	 * @param listaTipoServicioFiltro the listaTipoServicioFiltro to set
	 */
	public void setListaTipoServicioFiltro(List<ParametroDTO> listaTipoServicioFiltro) {
		this.listaTipoServicioFiltro = listaTipoServicioFiltro;
	}

	/**
	 * @return the listaSedesFiltro
	 */
	public List<ParametroDTO> getListaSedesFiltro() {
		return listaSedesFiltro;
	}

	/**
	 * @param listaSedesFiltro the listaSedesFiltro to set
	 */
	public void setListaSedesFiltro(List<ParametroDTO> listaSedesFiltro) {
		this.listaSedesFiltro = listaSedesFiltro;
	}

	/**
	 * @return the listaCitas
	 */
	public List<CitaDTO> getListaCitas() {
		return listaCitas;
	}

	/**
	 * @param listaCitas the listaCitas to set
	 */
	public void setListaCitas(List<CitaDTO> listaCitas) {
		this.listaCitas = listaCitas;
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
	 * @return the citaEliminar
	 */
	public CitaDTO getCitaEliminar() {
		return citaEliminar;
	}

	/**
	 * @param citaEliminar the citaEliminar to set
	 */
	public void setCitaEliminar(CitaDTO citaEliminar) {
		this.citaEliminar = citaEliminar;
	}
}

package com.mitocode.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.mitocode.dto.ConsultaListaExamenDTO;
import com.mitocode.dto.ConsultaResumenDTO;
import com.mitocode.dto.FiltroConsultaDTO;
import com.mitocode.exception.ModeloNotFoundException;
import com.mitocode.model.Archivo;
import com.mitocode.model.Consulta;
import com.mitocode.service.IArchivoService;
import com.mitocode.service.IConsultaService;

@RestController
@RequestMapping("/consultas")
public class ConsultaController {

	@Autowired
	private IConsultaService service;
	
	@Autowired
	private IArchivoService serviceArchivo;
	
	@GetMapping
	public ResponseEntity<List<Consulta>> listar() throws Exception{
		return new ResponseEntity<>(service.listar(), HttpStatus.OK);		
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<Consulta> listarPorId(@PathVariable("id") Integer id) throws Exception{
		Consulta obj = service.listarPorId(id);
		
		if(obj == null) {
			throw new ModeloNotFoundException("ID NO ENCONTRADO " + id);
		}
		return new ResponseEntity<>(obj, HttpStatus.OK); 		
	}
	
	@PostMapping
	public ResponseEntity<Consulta> registrar(@Valid @RequestBody ConsultaListaExamenDTO dto) throws Exception {
		//return new ResponseEntity<>(service.registrar(obj), HttpStatus.CREATED); //201 		
		 
		Consulta obj = service.registrarTransaccional(dto);
		//localhost:8080/consultas/1
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(obj.getIdConsulta()).toUri();
		return ResponseEntity.created(location).build();
	}
	
	@PutMapping
	public ResponseEntity<Consulta> modificar(@Valid @RequestBody Consulta consulta) throws Exception {
		return new ResponseEntity<>(service.modificar(consulta), HttpStatus.OK);		
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable("id") Integer id) throws Exception{		
		service.eliminar(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
	
	@GetMapping("/hateoas/{id}")
	public EntityModel<Consulta> listarHateoasPorId(@PathVariable("id") Integer id) throws Exception{
		Consulta obj = service.listarPorId(id);
		
		if(obj == null) {
			throw new ModeloNotFoundException("ID NO ENCONTRADO " + id);
		}
		
		EntityModel<Consulta> recurso = EntityModel.of(obj);
		//localhost:8080/consultas/hateoas/1
		WebMvcLinkBuilder link1 = linkTo(methodOn(this.getClass()).listarHateoasPorId(id));
		WebMvcLinkBuilder link2 = linkTo(methodOn(this.getClass()).listarHateoasPorId(id));
		recurso.add(link1.withRel("consulta-recurso1"));
		recurso.add(link2.withRel("consulta-recurso2"));
		
		return recurso;		
	}
	
	
	@GetMapping("/buscar")	
	public ResponseEntity<List<Consulta>> buscarFecha(@RequestParam(value = "fecha") String fecha) {		
		List<Consulta> consultas = new ArrayList<>();	
		
		consultas = service.buscarFecha(LocalDateTime.parse(fecha));						
		
		return new ResponseEntity<List<Consulta>>(consultas, HttpStatus.OK);
	}
	
	@PostMapping("/buscar/otros")
	public ResponseEntity<List<Consulta>> buscarOtro(@RequestBody FiltroConsultaDTO filtro) {		
		List<Consulta> consultas = new ArrayList<>();
		
		consultas = service.buscar(filtro);			
		
		return new ResponseEntity<List<Consulta>>(consultas, HttpStatus.OK);
	}
	
	@GetMapping(value = "/listarResumen")
	public ResponseEntity<List<ConsultaResumenDTO>> listarResumen() {
		List<ConsultaResumenDTO> consultas = new ArrayList<>();
		consultas = service.listarResumen();
		return new ResponseEntity<List<ConsultaResumenDTO>>(consultas, HttpStatus.OK);
	}
	
	@GetMapping(value = "/generarReporte", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE) //APPLICATION_PDF_VALUE
	public ResponseEntity<byte[]> generarReporte() {
		byte[] data = null;
		data = service.generarReporte();
		return new ResponseEntity<byte[]>(data, HttpStatus.OK);
	}
	
	@PostMapping(value = "/guardarArchivo", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	public ResponseEntity<Integer> guardarArchivo(@RequestParam("adjunto") MultipartFile file) throws IOException {
		//@RequestPart("medico") Medico medico
		
		int rpta = 0;
		
		Archivo ar = new Archivo();
		ar.setFiletype(file.getContentType());
		ar.setFilename(file.getOriginalFilename());
		ar.setValue(file.getBytes());
		
		rpta = serviceArchivo.guardar(ar);

		return new ResponseEntity<Integer>(rpta, HttpStatus.OK);
		
	}
	
	@GetMapping(value = "/leerArchivo/{idArchivo}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public ResponseEntity<byte[]> leerArchivo(@PathVariable("idArchivo") Integer idArchivo) throws IOException {
				
		byte[] arr = serviceArchivo.leerArchivo(idArchivo); 

		return new ResponseEntity<byte[]>(arr, HttpStatus.OK);
	}
	
}

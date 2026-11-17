/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import ln.LnDonacion;
import modelo.RegistroDonacion;
import modelo.Usuario;

/**
 * Controller de historial/miHistorial (HU-5.2 y HU-5.3). Solo consulta, vía LnDonacion.
 */
@Named
@RequestScoped
public class HistorialController {

    @EJB
    private LnDonacion lnDonacion;

    @Inject
    private SesionController sesion;

    private List<RegistroDonacion> donaciones = new ArrayList<>();
    private RegistroDonacion ultima;
    private Date proximaFecha;
    private boolean exclusionPermanente;
    private boolean puedeDonar;
    private long diasDesdeUltima;
    private int porcentaje;

    // f:viewAction de la vista. Se verifica la sesión aquí porque los f:viewAction corren
    // antes que el preRenderView de la plantilla (igual que en PerfilController).
    public void cargar() throws IOException {
        sesion.verificar();
        if (FacesContext.getCurrentInstance().getResponseComplete()) {
            return;
        }
        Usuario u = sesion.getUsuario();
        donaciones = lnDonacion.obtenerHistorial(u);               // HU-5.2
        ultima = lnDonacion.obtenerElegibilidad(u);                // HU-5.3
        proximaFecha = lnDonacion.obtenerProximaFecha(ultima);
        exclusionPermanente = lnDonacion.verificarExclusion(u);
        puedeDonar = lnDonacion.esElegible(u);
        diasDesdeUltima = lnDonacion.diasDesdeUltimaDonacion(ultima);
        porcentaje = lnDonacion.porcentajeRecuperacion(ultima);
    }

    public List<RegistroDonacion> getDonaciones() { return donaciones; }
    public RegistroDonacion getUltima() { return ultima; }
    public Date getProximaFecha() { return proximaFecha; }
    public boolean isExclusionPermanente() { return exclusionPermanente; }
    public boolean isPuedeDonar() { return puedeDonar; }
    public long getDiasDesdeUltima() { return diasDesdeUltima; }
    public int getPorcentaje() { return porcentaje; }
    public boolean isSinDonaciones() { return donaciones == null || donaciones.isEmpty(); }
}
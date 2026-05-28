package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.EntregaChecklistItem;
import com.concesionaria.app.domain.EntregaUnidad;
import com.concesionaria.app.repository.EntregaChecklistItemRepository;
import com.concesionaria.app.service.dto.EntregaChecklistItemDTO;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EntregaChecklistManager {

    private static final List<ChecklistTemplate> CHECKLIST_DEFAULT = List.of(
        new ChecklistTemplate("DOC_VEHICULO", "Documentacion del vehiculo", true),
        new ChecklistTemplate("CEDULA_CONSTANCIA", "Cedula/constancia", true),
        new ChecklistTemplate("MANUAL_PROPIETARIO", "Manual del propietario", true),
        new ChecklistTemplate("SEGUNDA_LLAVE", "Segunda llave", true),
        new ChecklistTemplate("SEGURO_VIGENTE", "Seguro vigente", true),
        new ChecklistTemplate("PATENTE_COLOCADA", "Patente colocada", true),
        new ChecklistTemplate("LAVADO_REALIZADO", "Lavado realizado", true),
        new ChecklistTemplate("PDI", "Inspeccion previa/PDI", true),
        new ChecklistTemplate("KIT_SEGURIDAD", "Kit de seguridad", true),
        new ChecklistTemplate("ACCESORIOS_ENTREGADOS", "Accesorios entregados", false)
    );

    private final EntregaChecklistItemRepository checklistItemRepository;

    public EntregaChecklistManager(EntregaChecklistItemRepository checklistItemRepository) {
        this.checklistItemRepository = checklistItemRepository;
    }

    public void crearChecklistInicial(EntregaUnidad entrega) {
        for (ChecklistTemplate template : CHECKLIST_DEFAULT) {
            EntregaChecklistItem item = new EntregaChecklistItem();
            item.setEntregaUnidad(entrega);
            item.setCodigo(template.codigo());
            item.setDescripcion(template.descripcion());
            item.setObligatorio(template.obligatorio());
            item.setCompletado(false);
            checklistItemRepository.save(item);
        }
    }

    public List<EntregaChecklistItem> obtenerChecklist(Long entregaId) {
        return checklistItemRepository.findAllByEntregaUnidadIdOrderByIdAsc(entregaId);
    }

    public void actualizarChecklist(List<EntregaChecklistItem> actuales, List<EntregaChecklistItemDTO> inputs) {
        for (EntregaChecklistItem actual : actuales) {
            for (EntregaChecklistItemDTO input : inputs) {
                if (actual.getId().equals(input.getId())) {
                    actual.setCompletado(Boolean.TRUE.equals(input.getCompletado()));
                    actual.setObservaciones(input.getObservaciones());
                }
            }
        }
        checklistItemRepository.saveAll(actuales);
    }

    private record ChecklistTemplate(String codigo, String descripcion, boolean obligatorio) {}
}


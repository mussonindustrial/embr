package com.mussonindustrial.ignition.embr.periscope.resources.editor.components

import com.inductiveautomation.ignition.client.util.gui.HeaderLabel
import com.inductiveautomation.ignition.client.util.gui.ValidatedTextField
import com.mussonindustrial.ignition.embr.periscope.i18n
import com.mussonindustrial.ignition.embr.periscope.resources.ComponentAttributes
import com.mussonindustrial.ignition.embr.periscope.resources.editor.SimpleDocumentListener
import javax.swing.JCheckBox
import javax.swing.JPanel
import javax.swing.JTextField
import net.miginfocom.swing.MigLayout

class ComponentAttributesPanel(
    private val saved: ComponentAttributes,
    resourceName: String,
    palettePaths: List<List<String>>,
    private val findDuplicate: (id: String) -> String?,
    private val onChanged: () -> Unit,
) : JPanel(MigLayout("ins 12, fillx", "[grow 50, fill]16[grow 50, fill]")) {

    private val idField = JTextField(saved.id)
    private val nameField = JTextField(saved.name)
    private val palettePathField = PalettePathField(palettePaths, saved.palettePath, onChanged)
    private val hiddenBox = JCheckBox(i18n("component.hidden"), saved.hidden)

    private val validatedIdField =
        object : ValidatedTextField(idField) {
            override fun validate(text: String?): String? {
                return idError(text.orEmpty().trim())
            }
        }

    val attributes: ComponentAttributes
        get() {
            val id = idField.text.trim()
            return ComponentAttributes(
                id = if (id.isEmpty() || idError(id) == null) id else saved.id,
                name = nameField.text.trim(),
                palettePath = palettePathField.palettePath,
                hidden = hiddenBox.isSelected,
            )
        }

    val hasInvalidId: Boolean
        get() = idError(idField.text.trim()) != null

    init {
        idField.toolTipText = i18n("component.id.tooltip")
        nameField.toolTipText = i18n("component.name.tooltip")
        nameField.putClientProperty("JTextField.placeholderText", resourceName)
        palettePathField.toolTipText = i18n("component.palette-path.tooltip")
        hiddenBox.toolTipText = i18n("component.hidden.tooltip")

        add(HeaderLabel(i18n("component.id")))
        add(HeaderLabel(i18n("component.name")), "wrap 0")
        add(validatedIdField)
        add(nameField, "wrap 12")
        add(HeaderLabel(i18n("component.palette-path")))
        add(HeaderLabel(i18n("component.visibility")), "wrap 0")
        add(palettePathField)
        add(hiddenBox)

        listOf(idField, nameField).forEach {
            it.document.addDocumentListener(SimpleDocumentListener { onChanged() })
        }
        hiddenBox.addActionListener { onChanged() }
    }

    private fun idError(id: String): String? {
        if (id.isEmpty()) {
            return i18n("component.error.missingId")
        }
        if (!ComponentAttributes.isValidId(id)) {
            return i18n("component.error.invalidId", id)
        }

        val duplicate = findDuplicate(id) ?: return null
        return i18n("component.error.existingId", id, duplicate)
    }
}

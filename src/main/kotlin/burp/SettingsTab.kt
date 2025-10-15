package burp

import java.awt.*
import java.awt.event.KeyEvent
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.Document

class SettingsTab(private val settings: Settings) : JPanel(BorderLayout()) {

    private val tabs = JTabbedPane()

    // --- General (Settings) ---
    private val listenField = JTextField()
    private val hexClientHelloField = JTextField()
    private val fingerprintCombo = JComboBox<String>()
    private val timeoutSpinner = JSpinner(SpinnerNumberModel(10, 1, 300, 1)) // 1..300 s
    private val applyBtn = JButton("Apply")
    private val resetBtn = JButton("Reset")

    // --- Advanced ---
    private val useInterceptedCheck = JCheckBox("Use intercepted TLS fingerprint")
    private val interceptAddrField = JTextField()
    private val burpAddrField = JTextField()
    private val applyAdvBtn = JButton("Apply")
    private val resetAdvBtn = JButton("Reset")

    init {
        border = EmptyBorder(8, 8, 8, 8)
        add(tabs, BorderLayout.CENTER)

        tabs.add("Settings", buildGeneralPanel())
        tabs.add("Advanced", buildAdvancedPanel())

        loadFromSettings()
        wireEvents()
        updateDirty(false)

        // Keyboard: Ctrl/Cmd+Enter -> Apply on current tab
        val applyAction = object : AbstractAction() {
            override fun actionPerformed(e: java.awt.event.ActionEvent?) {
                if (tabs.selectedIndex == 0) applyGeneral() else applyAdvanced()
            }
        }
        val im = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
        val km = actionMap
        val key = KeyStroke.getKeyStroke(
            KeyEvent.VK_ENTER,
            Toolkit.getDefaultToolkit().menuShortcutKeyMaskEx
        )
        im.put(key, "apply")
        km.put("apply", applyAction)
    }

    // region UI builders

    private fun buildGeneralPanel(): JPanel {
        val p = JPanel()
        p.layout = BoxLayout(p, BoxLayout.Y_AXIS)

        p.add(formRow("Listen address:", listenField,
            "Local address the proxy listens on, e.g. 127.0.0.1:8083. Requires extension reload.")
        )
        p.add(formRow("Hex Client Hello:", hexClientHelloField,
            "Custom ClientHello as hex; leave empty to auto-detect.")
        )
        p.add(formRow("Fingerprint:", fingerprintCombo,
            "Choose a predefined TLS fingerprint.")
        )
        p.add(formRow("HTTP timeout (s):", timeoutSpinner,
            "Max time to establish the connection.")
        )

        p.add(buttonRow(applyBtn, resetBtn))
        p.add(Box.createVerticalGlue())
        return p
    }

    private fun buildAdvancedPanel(): JPanel {
        val p = JPanel()
        p.layout = BoxLayout(p, BoxLayout.Y_AXIS)

        p.add(checkRow(useInterceptedCheck, "Prefer the fingerprint from the last intercepted TLS handshake."))
        p.add(formRow("Intercept proxy address:", interceptAddrField,
            "Where your client should point (host:port). Requires extension reload.")
        )
        p.add(formRow("Burp proxy address:", burpAddrField,
            "Burp’s upstream proxy (host:port), if used.")
        )
        p.add(buttonRow(applyAdvBtn, resetAdvBtn))
        p.add(Box.createVerticalGlue())
        return p
    }

    private fun formRow(labelText: String, comp: JComponent, tooltip: String? = null): JPanel {
        val row = JPanel(GridBagLayout())
        val c = GridBagConstraints().apply {
            insets = Insets(4, 4, 4, 4)
            anchor = GridBagConstraints.WEST
        }
        val label = JLabel(labelText).also {
            it.labelFor = comp
            it.toolTipText = tooltip
        }
        comp.toolTipText = tooltip

        c.gridx = 0; c.gridy = 0; c.weightx = 0.0; c.fill = GridBagConstraints.NONE
        row.add(label, c)
        c.gridx = 1; c.weightx = 1.0; c.fill = GridBagConstraints.HORIZONTAL
        row.add(comp, c)
        return row
    }

    private fun checkRow(check: JCheckBox, tooltip: String? = null): JPanel {
        val row = JPanel(FlowLayout(FlowLayout.LEFT, 8, 0))
        check.toolTipText = tooltip
        row.add(check)
        return row
    }

    private fun buttonRow(vararg buttons: JButton): JPanel {
        val row = JPanel(FlowLayout(FlowLayout.LEFT, 8, 4))
        buttons.forEach(row::add)
        return row
    }

    // endregion

    // region wiring & state

    private fun wireEvents() {
        applyBtn.addActionListener { applyGeneral() }
        resetBtn.addActionListener { loadFromSettings(); updateDirty(false) }

        applyAdvBtn.addActionListener { applyAdvanced() }
        resetAdvBtn.addActionListener { loadFromSettings(); updateDirty(false) }

        // mark dirty when anything changes
        fun Document.watch() = addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) = updateDirty(true)
            override fun removeUpdate(e: DocumentEvent?) = updateDirty(true)
            override fun changedUpdate(e: DocumentEvent?) = updateDirty(true)
        })
        listenField.document.watch()
        hexClientHelloField.document.watch()
        interceptAddrField.document.watch()
        burpAddrField.document.watch()
        timeoutSpinner.addChangeListener { updateDirty(true) }
        fingerprintCombo.addActionListener { updateDirty(true) }
        useInterceptedCheck.addItemListener { updateDirty(true) }
    }

    private fun loadFromSettings() {
        listenField.columns = 24
        interceptAddrField.columns = 24
        burpAddrField.columns = 24
        hexClientHelloField.columns = 24

        listenField.text = settings.spoofProxyAddress
        hexClientHelloField.text = settings.hexClientHello.orEmpty()

        fingerprintCombo.removeAllItems()
        settings.fingerprints.forEach { fingerprintCombo.addItem(it) }
        fingerprintCombo.selectedItem = settings.fingerprint

        timeoutSpinner.value = settings.httpTimeout.coerceIn(1, 300)

        interceptAddrField.text = settings.interceptProxyAddress
        burpAddrField.text = settings.burpProxyAddress
        useInterceptedCheck.isSelected = settings.useInterceptedFingerprint
    }

    private fun updateDirty(dirty: Boolean) {
        listOf(applyBtn, resetBtn, applyAdvBtn, resetAdvBtn).forEach { it.isEnabled = dirty }
    }

    // endregion

    // region apply + validation

    private fun applyGeneral() {
        val errs = mutableListOf<String>()
        validateHostPort(listenField.text)?.let { errs += "Listen address: $it" }
        validateHexOrEmpty(hexClientHelloField.text)?.let { errs += "Hex Client Hello: $it" }
        val timeout = (timeoutSpinner.value as? Int) ?: 10
        if (timeout !in 1..300) errs += "HTTP timeout must be between 1 and 300 seconds."

        if (errs.isNotEmpty()) {
            JOptionPane.showMessageDialog(this, errs.joinToString("\n"), "Invalid settings", JOptionPane.ERROR_MESSAGE)
            return
        }

        settings.spoofProxyAddress = listenField.text.trim()
        settings.hexClientHello = hexClientHelloField.text.trim().ifEmpty { null }
        settings.fingerprint = (fingerprintCombo.selectedItem as? String) ?: settings.fingerprint
        settings.httpTimeout = timeout
        updateDirty(false)
    }

    private fun applyAdvanced() {
        val errs = mutableListOf<String>()
        validateHostPort(interceptAddrField.text)?.let { errs += "Intercept proxy address: $it" }
        validateHostPort(burpAddrField.text)?.let { errs += "Burp proxy address: $it" }

        if (errs.isNotEmpty()) {
            JOptionPane.showMessageDialog(this, errs.joinToString("\n"), "Invalid settings", JOptionPane.ERROR_MESSAGE)
            return
        }

        settings.interceptProxyAddress = interceptAddrField.text.trim()
        settings.burpProxyAddress = burpAddrField.text.trim()
        settings.useInterceptedFingerprint = useInterceptedCheck.isSelected
        updateDirty(false)
    }

    private fun validateHostPort(input: String): String? {
        val trimmed = input.trim()
        val m = HOST_PORT.matchEntire(trimmed) ?: return "use host:port (e.g., 127.0.0.1:8080)."
        val port = m.groupValues[2].toInt()
        return if (port in 1..65535) null else "port must be 1–65535."
    }

    private fun validateHexOrEmpty(input: String): String? {
        val v = input.trim()
        if (v.isEmpty()) return null
        if (!HEX.matches(v)) return "hex only (0-9a-f), no spaces."
        if (v.length % 2 != 0) return "must have an even number of hex characters."
        return null
    }

    companion object {
        private val HOST_PORT = Regex("""([^:\s]+):(\d{1,5})""")
        private val HEX = Regex("^[0-9a-fA-F]+$")
    }

    // --- ITab (Burp) ---
    override fun getTabCaption(): String = "Settings"
    override fun getUiComponent(): Component = this
}
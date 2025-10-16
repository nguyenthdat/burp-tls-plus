package burp

import burp.api.montoya.ui.settings.SettingsPanel
import java.awt.*
import java.awt.event.KeyEvent
import javax.swing.*
import javax.swing.border.Border
import javax.swing.border.EmptyBorder
import javax.swing.border.TitledBorder
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.Document


/**
 * Compact, readable Settings UI for Burp.
 * - Titled sections (Proxy, TLS, Network)
 * - Inline help text (smaller, muted)
 * - Consistent two-column form grid
 * - Inline validation (field border + help text)
 * - Reasonable max width, scrollable when needed
 */
class SettingsPanel(private val settings: Settings) : JPanel(BorderLayout()), SettingsPanel {

    // --- General controls ---
    private val listenField = JTextField()
    private val hexClientHelloField = JTextField()
    private val fingerprintCombo = JComboBox<String>()
    private val timeoutSpinner = JSpinner(SpinnerNumberModel(30, 1, 300, 1))
    private val applyBtn = JButton("Apply")
    private val resetBtn = JButton("Reset")

    // --- Advanced controls ---
    private val useInterceptedCheck = JCheckBox("Use intercepted TLS fingerprint")
    private val interceptAddrField = JTextField()
    private val burpAddrField = JTextField()
    private val applyAdvBtn = JButton("Apply")
    private val resetAdvBtn = JButton("Reset")

    // keep original borders for validation toggling
    private val originalBorders = mutableMapOf<JComponent, Border?>()

    init {
        border = EmptyBorder(8, 8, 8, 8)

        val tabs = JTabbedPane()
        tabs.addTab("Settings", makeScrollable(buildSettingsPanel()))
        tabs.addTab("Advanced", makeScrollable(buildAdvancedPanel()))
        add(tabs, BorderLayout.CENTER)

        loadFromSettings()
        wireEvents()
        updateDirty(false)

        // Keyboard: Ctrl/Cmd+Enter -> Apply on active tab
        val applyAction = object : AbstractAction() {
            override fun actionPerformed(e: java.awt.event.ActionEvent?) {
                if (tabs.selectedIndex == 0) applyGeneral() else applyAdvanced()
            }
        }
        val im = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
        val km = actionMap
        val key = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, Toolkit.getDefaultToolkit().menuShortcutKeyMaskEx)
        im.put(key, "apply"); km.put("apply", applyAction)
    }

    // region Builders

    private fun buildSettingsPanel(): JComponent {
        val content = verticalBox()
        content.add(section("Proxy") {
            formRow(
                label = "Listen address:",
                field = listenField,
                help = "Local address the TLS+ proxy listens on (host:port). Requires extension reload.")
        })
        content.add(section("TLS") {
            formRow(
                label = "Hex Client Hello:",
                field = hexClientHelloField,
                help = "Provide a hex-encoded ClientHello. Leave empty to auto-detect.")
            formRow(
                label = "Fingerprint:",
                field = fingerprintCombo,
                help = "Choose predefined TLS JA3-like fingerprint.")
        })
        content.add(section("Network") {
            formRow(
                label = "HTTP timeout (s):",
                field = timeoutSpinner,
                help = "Connection establishment timeout (1–300 seconds).")
        })
        content.add(buttonStrip(applyBtn, resetBtn))
        return wrapCentered(content)
    }

    private fun buildAdvancedPanel(): JComponent {
        val content = verticalBox()
        content.add(section("Fingerprint") {
            val row = JPanel(FlowLayout(FlowLayout.LEFT, 8, 0))
            row.add(useInterceptedCheck)
            row.add(helpLabel("Use fingerprint captured from last intercepted TLS handshake."))
            row.alignmentX = Component.LEFT_ALIGNMENT
            add(row)
        })
        content.add(section("Proxy forwarding") {
            formRow(
                label = "Intercept proxy address:",
                field = interceptAddrField,
                help = "Where your client should point (host:port). Requires extension reload.")
            formRow(
                label = "Burp proxy address:",
                field = burpAddrField,
                help = "Upstream Burp proxy (host:port), optional.")
        })
        content.add(buttonStrip(applyAdvBtn, resetAdvBtn))
        return wrapCentered(content)
    }

    // compact, readable section wrapper
    private fun section(title: String, body: JPanel.() -> Unit): JComponent {
        val inner = JPanel()
        inner.layout = BoxLayout(inner, BoxLayout.Y_AXIS)
        inner.border = EmptyBorder(8, 8, 8, 8)
        inner.body()
        val panel = JPanel(BorderLayout())
        panel.border = TitledBorder(title)
        panel.add(inner, BorderLayout.CENTER)
        panel.alignmentX = Component.LEFT_ALIGNMENT
        return panel
    }

    private fun verticalBox(): JPanel = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        alignmentX = Component.LEFT_ALIGNMENT
    }

    private fun wrapCentered(content: JComponent): JComponent {
        // Cap width so form doesn’t stretch across ultra-wide Burp window
        content.maximumSize = Dimension(760, Int.MAX_VALUE)
        val wrapper = JPanel()
        wrapper.layout = BoxLayout(wrapper, BoxLayout.X_AXIS)
        wrapper.add(Box.createHorizontalGlue())
        wrapper.add(content)
        wrapper.add(Box.createHorizontalGlue())
        wrapper.border = EmptyBorder(4, 4, 4, 12)
        return wrapper
    }

    private fun buttonStrip(vararg buttons: JButton): JComponent {
        val p = JPanel(FlowLayout(FlowLayout.RIGHT, 8, 4))
        buttons.forEach(p::add)
        p.alignmentX = Component.LEFT_ALIGNMENT
        return p
    }

    private fun makeScrollable(c: JComponent) = JScrollPane(c).apply {
        border = EmptyBorder(0, 0, 0, 0)
        horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
    }

    /** two‑column row with optional help under the field */
    private fun JPanel.formRow(label: String, field: JComponent, help: String? = null) {
        val row = JPanel(GridBagLayout())
        val c = GridBagConstraints().apply {
            insets = Insets(4, 6, 2, 6)
            anchor = GridBagConstraints.WEST
            fill = GridBagConstraints.HORIZONTAL
        }
        val lbl = JLabel(label).apply {
            preferredSize = Dimension(170, preferredSize.height) // consistent label column
            horizontalAlignment = SwingConstants.RIGHT
            labelFor = field
        }
        field.toolTipText = help
        if (field is JTextField) field.columns = 28
        if (field is JSpinner) (field.model as? SpinnerNumberModel)?.also { field.preferredSize = Dimension(120, field.preferredSize.height) }

        originalBorders.putIfAbsent(field, field.border)

        c.gridx = 0; c.gridy = 0; c.weightx = 0.0
        row.add(lbl, c)
        c.gridx = 1; c.gridy = 0; c.weightx = 1.0
        row.add(field, c)

        help?.let {
            val helpL = helpLabel(it)
            c.gridx = 1; c.gridy = 1; c.weightx = 1.0
            row.add(helpL, c)
        }
        row.alignmentX = Component.LEFT_ALIGNMENT
        this.add(row)
    }

    private fun helpLabel(text: String) = JLabel(text).apply {
        font = font.deriveFont(font.size2D - 1.5f)
        foreground = UIManager.getColor("Label.disabledForeground")
    }

    // endregion

    // region Wiring & state

    private fun wireEvents() {
        applyBtn.addActionListener { applyGeneral() }
        resetBtn.addActionListener { loadFromSettings(); updateDirty(false); clearValidation() }
        applyAdvBtn.addActionListener { applyAdvanced() }
        resetAdvBtn.addActionListener { loadFromSettings(); updateDirty(false); clearValidation() }

        fun Document.watch() = addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) = updateDirty(true)
            override fun removeUpdate(e: DocumentEvent?) = updateDirty(true)
            override fun changedUpdate(e: DocumentEvent?) = updateDirty(true)
        })
        listenField.document.watch(); hexClientHelloField.document.watch()
        interceptAddrField.document.watch(); burpAddrField.document.watch()
        timeoutSpinner.addChangeListener { updateDirty(true) }
        fingerprintCombo.addActionListener { updateDirty(true) }
        useInterceptedCheck.addItemListener { updateDirty(true) }
    }

    private fun loadFromSettings() {
        // width hints
        listOf(listenField, hexClientHelloField, interceptAddrField, burpAddrField).forEach {
            it.columns = 28
            it.maximumSize = Dimension(Int.MAX_VALUE, it.preferredSize.height)
        }

        listenField.text = settings.spoofProxyAddress
        hexClientHelloField.text = settings.hexClientHello.orEmpty()

        fingerprintCombo.removeAllItems()
        settings.fingerprints.sorted().forEach { fingerprintCombo.addItem(it) }
        fingerprintCombo.selectedItem = settings.fingerprint
        fingerprintCombo.prototypeDisplayValue = "Chrome120xxxxxxxx" // keeps a stable width

        timeoutSpinner.value = settings.httpTimeout.coerceIn(1, 300)
        interceptAddrField.text = settings.interceptProxyAddress
        burpAddrField.text = settings.burpProxyAddress
        useInterceptedCheck.isSelected = settings.useInterceptedFingerprint
    }

    private fun updateDirty(dirty: Boolean) {
        listOf(applyBtn, resetBtn, applyAdvBtn, resetAdvBtn).forEach { it.isEnabled = dirty }
    }

    private fun clearValidation() {
        (originalBorders.keys).forEach { it.border = originalBorders[it] }
    }

    // endregion

    // region Apply + validation

    private fun applyGeneral() {
        clearValidation()
        val errs = mutableListOf<String>()
        validateHostPort(listenField.text)?.also { markInvalid(listenField); errs += "Listen address: $it" }
        validateHexOrEmpty(hexClientHelloField.text)?.also { markInvalid(hexClientHelloField); errs += "Hex Client Hello: $it" }
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
        JOptionPane.showMessageDialog(this, "Saved.", "Settings", JOptionPane.INFORMATION_MESSAGE)
    }

    private fun applyAdvanced() {
        clearValidation()
        val errs = mutableListOf<String>()
        validateHostPort(interceptAddrField.text)?.also { markInvalid(interceptAddrField); errs += "Intercept proxy address: $it" }
        validateHostPort(burpAddrField.text)?.also { markInvalid(burpAddrField); errs += "Burp proxy address: $it" }

        if (errs.isNotEmpty()) {
            JOptionPane.showMessageDialog(this, errs.joinToString("\n"), "Invalid settings", JOptionPane.ERROR_MESSAGE)
            return
        }

        settings.interceptProxyAddress = interceptAddrField.text.trim()
        settings.burpProxyAddress = burpAddrField.text.trim()
        settings.useInterceptedFingerprint = useInterceptedCheck.isSelected
        updateDirty(false)
        JOptionPane.showMessageDialog(this, "Saved.", "Settings", JOptionPane.INFORMATION_MESSAGE)
    }

    private fun markInvalid(c: JComponent) {
        val danger = Color(0xCC4A4A)
        c.border = BorderFactory.createLineBorder(danger, 1)
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

    override fun uiComponent(): JComponent? {
        return ui as JComponent?
    }

    companion object {
        private val HOST_PORT = Regex("([^:\\s]+):(\\d{1,5})")
        private val HEX = Regex("^[0-9a-fA-F]+$")
    }
}
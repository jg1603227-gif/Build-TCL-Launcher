
package br.com.tcllauncher

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.LayerDrawable
import android.net.Uri

class MainActivity : Activity() {
    
    private val wallpaperRequest = 4101
    private fun openWallpaperPicker() {
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "image/*"
        addFlags(
            Intent.FLAG_GRANT_READ_URI_PERMISSION or
            Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        )
    }

    startActivityForResult(intent, wallpaperRequest)
}

override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?
) {
    super.onActivityResult(requestCode, resultCode, data)

    if (requestCode == wallpaperRequest && resultCode == RESULT_OK) {
        val uri = data?.data ?: return

        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: Exception) {}

        getSharedPreferences("launcher", MODE_PRIVATE)
            .edit()
            .putString("wallpaper_uri", uri.toString())
            .apply()

        recreate()
    }
}

private fun applyCustomWallpaper(view: View) {
    val savedUri = getSharedPreferences(
        "launcher", MODE_PRIVATE
    ).getString("wallpaper_uri", null) ?: return

    try {
        val uri = Uri.parse(savedUri)

        val bitmap = contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it)
        } ?: return

        val image = BitmapDrawable(resources, bitmap).apply {
            gravity = Gravity.FILL
        }

        view.background = LayerDrawable(
            arrayOf(
                image,
                ColorDrawable(Color.argb(155, 8, 9, 15))
            )
        )
    } catch (_: Exception) {
        Toast.makeText(
            this,
            "Não foi possível carregar o plano de fundo.",
            Toast.LENGTH_SHORT
        ).show()
    }
}
    private val bg = Color.rgb(8, 9, 15)
    private val panel = Color.rgb(19, 21, 31)
    private val card = Color.rgb(28, 31, 44)
    private val purple = Color.rgb(137, 79, 255)
    private val neon = Color.rgb(174, 112, 255)
    private val white = Color.rgb(245, 245, 255)
    private val muted = Color.rgb(145, 150, 170)

    private lateinit var content: LinearLayout
    private lateinit var sidebar: LinearLayout
    private lateinit var sidebarParams: LinearLayout.LayoutParams
    private lateinit var clock: TextView

    private val navLabels = mutableListOf<TextView>()
    private var expanded = false
    private var selected = "Início"

    private val handler = Handler(Looper.getMainLooper())

    private val clockTick = object : Runnable {
        override fun run() {
            if (::clock.isInitialized) {
                clock.text = SimpleDateFormat(
                    "EEE, dd MMM  •  HH:mm",
                    Locale("pt", "BR")
                ).format(Date())
            }
            handler.postDelayed(this, 30_000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = bg
        window.navigationBarColor = bg
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE

        buildShell()
        showSection("Início")
        handler.post(clockTick)
    }

    override fun onDestroy() {
        handler.removeCallbacks(clockTick)
        super.onDestroy()
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun rounded(
        color: Int,
        radius: Int = 18,
        stroke: Int = Color.TRANSPARENT
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
            if (stroke != Color.TRANSPARENT) {
                setStroke(dp(1), stroke)
            }
        }
    }

    private fun gradient(): GradientDrawable =
        GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(
                Color.rgb(65, 35, 125),
                Color.rgb(35, 26, 76),
                Color.rgb(20, 22, 39)
            )
        ).apply {
            cornerRadius = dp(24).toFloat()
        }

    private fun label(
        textValue: String,
        size: Float,
        color: Int,
        bold: Boolean = false
    ): TextView {
        return TextView(this).apply {
            text = textValue
            textSize = size
            setTextColor(color)
            if (bold) {
                typeface = Typeface.create(
                    "sans-serif",
                    Typeface.BOLD
                )
            }
            includeFontPadding = true
        }
    }

    private fun buildShell() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(16), dp(15), dp(20), dp(15))
            setBackgroundColor(bg)
        }

        sidebar = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            setPadding(dp(9), dp(15), dp(9), dp(12))
            background = rounded(panel, 22)
        }

        sidebarParams = LinearLayout.LayoutParams(
            dp(76), -1
        )

        root.addView(sidebar, sidebarParams)

        val brand = label("T", 27f, neon, true).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(5), 0, dp(18))
        }
        sidebar.addView(brand)

        navLabels.clear()

        val items = listOf(
    Triple("Início", "⌂", "Início"),
    Triple("Configurações", "⚙", "Configurações")
)

        items.forEach { (name, symbol, description) ->

            val item = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                isFocusable = true
                isClickable = true
                setPadding(dp(12), 0, dp(8), 0)
                background = rounded(
                    if (selected == name) purple
                    else Color.TRANSPARENT, 14
                )

                addView(
                    label(symbol, 23f, white, true),
                    LinearLayout.LayoutParams(dp(36), -2)
                )

                val title = label(
                    description, 13f, white,
                    selected == name
                )
                title.visibility = View.GONE
                navLabels.add(title)

                addView(
                    title,
                    LinearLayout.LayoutParams(0, -2, 1f)
                )

                contentDescription = name

                setOnClickListener {
                    showSection(name)
                }

                setOnFocusChangeListener { v, focused ->
                    if (focused) {
                        setSidebarExpanded(true)
                    }

                    if (name != selected) {
                        v.background = rounded(
                            if (focused) card
                            else Color.TRANSPARENT,
                            14,
                            if (focused) purple
                            else Color.TRANSPARENT
                        )
                    }

                    v.scaleX = if (focused) 1.04f else 1f
                    v.scaleY = if (focused) 1.04f else 1f
                }
            }

            val lp = LinearLayout.LayoutParams(-1, dp(51))
            lp.bottomMargin = dp(7)
            sidebar.addView(item, lp)
        }

        sidebar.addView(
            View(this),
            LinearLayout.LayoutParams(1, 0, 1f)
        )

        sidebar.addView(
            label("2.0", 10f, muted, true).apply {
                gravity = Gravity.CENTER
                setPadding(0, dp(8), 0, dp(4))
            }
        )

        val main = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(3), 0, 0)
        }

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        top.addView(
            label("TCL  /  NEON EDITION", 11f, neon, true),
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        clock = label("", 13f, muted)
        top.addView(clock)

        main.addView(top, LinearLayout.LayoutParams(-1, dp(42)))

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            clipToPadding = false
            addView(content)
        }

        main.addView(
            scroll,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        val mainLp = LinearLayout.LayoutParams(0, -1, 1f)
        mainLp.leftMargin = dp(18)
        root.addView(main, mainLp)

        applyCustomWallpaper(root)
        setContentView(root)

        setSidebarExpanded(false)
    }

    private fun setSidebarExpanded(value: Boolean) {
        if (!::sidebar.isInitialized) return

        expanded = value

        sidebarParams.width = dp(if (value) 210 else 76)
        sidebar.layoutParams = sidebarParams

        navLabels.forEach {
            it.visibility = if (value) View.VISIBLE else View.GONE
        }

        sidebar.requestLayout()
    }

    private fun showSection(section: String) {
        selected = section
        content.removeAllViews()

        val names = listOf(
            "Início", "Aplicativos",
            "Filmes e séries", "Música",
            "Configurações"
        )

        val navItems = sidebar.childrenViews()

        navItems.forEachIndexed { index, view ->
            if (index < names.size) {
                view.background = rounded(
                    if (names[index] == section) purple
                    else Color.TRANSPARENT, 14
                )
            }
        }

        navLabels.forEachIndexed { index, view ->
            view.setTypeface(
                Typeface.DEFAULT,
                if (index < names.size &&
                    names[index] == section) {
                    Typeface.BOLD
                } else {
                    Typeface.NORMAL
                }
            )
        }

        when (section) {
            "Início" -> showHome()
            "Aplicativos" ->
                showAppSection("Todos os aplicativos", allApps())
            "Filmes e séries" ->
                showAppSection(
                    "Filmes e séries",
                    allApps().filter { isVideoApp(it) }
                )
            "Música" ->
                showAppSection(
                    "Música",
                    allApps().filter { isMusicApp(it) }
                )
            "Configurações" -> showSettings()
        }

        setSidebarExpanded(false)

        content.post {
            firstFocusable(content)?.requestFocus()
        }
    }

    private fun LinearLayout.childrenViews(): List<View> {
        val result = mutableListOf<View>()
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.isFocusable) result.add(child)
        }
        return result
    }

    private fun firstFocusable(group: ViewGroup): View? {
        for (i in 0 until group.childCount) {
            val child = group.getChildAt(i)
            if (child.isFocusable) return child
            if (child is ViewGroup) {
                firstFocusable(child)?.let { return it }
            }
        }
        return null
    }

    private fun prepareContentFocus(view: View) {
        view.setOnFocusChangeListener { _, focused ->
            if (focused) setSidebarExpanded(false)
        }
    }

    private fun heading(title: String, subtitle: String) {
        content.addView(
            label(title, 28f, white, true).apply {
                setPadding(0, dp(17), 0, dp(4))
            }
        )

        content.addView(
            label(subtitle, 14f, muted).apply {
                setPadding(0, 0, 0, dp(17))
            }
        )
    }

    
private fun showHome() {

    val apps = allApps()

    val dashboard = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        setPadding(0, dp(10), 0, dp(10))
    }

    // LISTA VERTICAL DE APLICATIVOS

    val appColumn = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        background = rounded(panel, 18)
        setPadding(dp(12), dp(12), dp(12), dp(8))
    }

    appColumn.addView(
        label("APLICATIVOS", 13f, neon, true)
            .apply {
                setPadding(dp(5), dp(5), 0, dp(12))
            }
    )

    val appScroll = ScrollView(this).apply {
        isFillViewport = true
        clipToPadding = false
    }

    val appList = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
    }

    // PAINEL DE PRÉVIA

    val rightColumn = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(15), 0, 0, 0)
    }

    val preview = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
        background = gradient()
        setPadding(dp(25), dp(20), dp(25), dp(20))
    }

    
fun updatePreview(info: ResolveInfo) {
    preview.removeAllViews()

    preview.gravity = Gravity.CENTER

    val details = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
    }

    details.addView(
        ImageView(this).apply {
            setImageDrawable(info.loadIcon(packageManager))
            scaleType = ImageView.ScaleType.FIT_CENTER
        },
        LinearLayout.LayoutParams(dp(112), dp(112)).apply {
            bottomMargin = dp(18)
        }
    )

    details.addView(
        label(
            info.loadLabel(packageManager).toString(),
            25f, white, true
        ).apply {
            gravity = Gravity.CENTER
        }
    )

    details.addView(
        label(
            "Pressione OK para iniciar",
            12f, neon
        ).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(10), 0, 0)
        }
    )

    preview.addView(
        details,
        LinearLayout.LayoutParams(-1, -1)
    )
}


        preview.addView(
            ImageView(this@MainActivity).apply {
    setImageDrawable(info.loadIcon(packageManager))
    scaleType = ImageView.ScaleType.FIT_CENTER
    background = rounded(panel, 12, purple)
    clipToOutline = true
    setPadding(dp(3), dp(3), dp(3), dp(3))
            }
    if (apps.isNotEmpty()) {
        updatePreview(apps.first())
    }

    // ITENS DA LISTA

    apps.forEach { info ->

        val item = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = rounded(
                Color.TRANSPARENT, 13
            )
            setPadding(dp(10), dp(7), dp(8), dp(7))
            isFocusable = true
            isClickable = true

            addView(
                ImageView(this@MainActivity).apply {
                    setImageDrawable(
                        info.loadIcon(packageManager)
                    )
                    scaleType = ImageView.ScaleType.FIT_CENTER
                },
                LinearLayout.LayoutParams(dp(39), dp(39))
            )

            addView(
    View(this@MainActivity),
    LinearLayout.LayoutParams(0, 1, 1f)
)

            setOnFocusChangeListener { v, focused ->

                if (focused) {
                    updatePreview(info)
                    v.background = rounded(
                        purple, 13, neon
                    )
                    v.scaleX = 1.03f
                    v.scaleY = 1.03f
                } else {
                    v.background = rounded(
                        Color.TRANSPARENT, 13
                    )
                    v.scaleX = 1f
                    v.scaleY = 1f
                }
            }

            setOnClickListener {
                launchApp(info)
            }
        }

        val lp = LinearLayout.LayoutParams(
            -1, dp(56)
        )
        lp.bottomMargin = dp(5)

        appList.addView(item, lp)
    }

    appScroll.addView(appList)

    appColumn.addView(
        appScroll,
        LinearLayout.LayoutParams(-1, 0, 1f)
    )

    // PAINEL DE INFORMAÇÕES DO SISTEMA

    val widgets = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
    }

    

val memoryManager = getSystemService(
    android.content.Context.ACTIVITY_SERVICE
) as android.app.ActivityManager

val memoryInfo = android.app.ActivityManager.MemoryInfo()

memoryManager.getMemoryInfo(memoryInfo)

val ramUsed = memoryInfo.totalMem - memoryInfo.availMem

val ramPercent = if (memoryInfo.totalMem > 0L) {
    (ramUsed.toDouble() / memoryInfo.totalMem.toDouble() * 100.0).toInt()
} else {
    0
}


    val stat = android.os.StatFs(
        android.os.Environment.getDataDirectory().path
    )

    val totalStorage =
        stat.blockCountLong * stat.blockSizeLong

    val freeStorage =
        stat.availableBlocksLong * stat.blockSizeLong

    val usedStorage = totalStorage - freeStorage

    val storagePercent =
    if (totalStorage > 0L) {
        (usedStorage.toDouble() / totalStorage.toDouble() * 100.0).toInt()
    } else {
        0
    }

    fun addWidget(
        title: String,
        value: String,
        detail: String,
        symbol: String
    ) {
        val widget = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            background = rounded(panel, 16, purple)
            setPadding(dp(13), dp(12), dp(10), dp(12))

            addView(
                label(symbol, 23f, neon, true)
            )

            addView(
                label(title, 13f, white, true)
                    .apply {
                        setPadding(0, dp(7), 0, dp(5))
                    }
            )

            addView(
                label(value, 19f, neon, true)
            )

            addView(
                label(detail, 10f, muted)
                    .apply {
                        setPadding(0, dp(4), 0, 0)
                    }
            )
        }

        val lp = LinearLayout.LayoutParams(
            0, dp(135), 1f
        )
        lp.rightMargin = dp(8)

        widgets.addView(widget, lp)
    }

    addWidget(
        "Clima",
        "--",
        "Sem dados disponíveis",
        "☀"
    )

    addWidget(
        "Uso de RAM",
        "$ramPercent%",
        "${android.text.format.Formatter.formatFileSize(this, ramUsed)} utilizados",
        "▦"
    )

    addWidget(
        "Armazenamento",
        "$storagePercent%",
        "${android.text.format.Formatter.formatFileSize(this, usedStorage)} utilizados",
        "▤"
    )

    addWidget(
        "Sistema",
        "Conectado",
        "${android.os.Build.MODEL} • Android ${android.os.Build.VERSION.RELEASE}",
        "⌁"
    )

    // MONTAGEM FINAL

    val leftParams = LinearLayout.LayoutParams(
        dp(218), -1
    )

    dashboard.addView(appColumn, leftParams)

    rightColumn.addView(
    preview,
    LinearLayout.LayoutParams(-1, 0, 1f)
)

val widgetParams = LinearLayout.LayoutParams(
    -1, dp(125)
)
widgetParams.topMargin = dp(12)

rightColumn.addView(widgets, widgetParams)

    dashboard.addView(
        rightColumn,
        LinearLayout.LayoutParams(0, -1, 1f)
    )

    content.addView(
        dashboard,
        LinearLayout.LayoutParams(-1, -1)
    )
}

    private fun categoryCard(
        icon: String,
        title: String,
        action: () -> Unit
    ): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = rounded(card, 15)
            isFocusable = true
            isClickable = true

            addView(label(icon, 22f, neon, true))
            addView(
                label(title, 12f, white, true).apply {
                    gravity = Gravity.CENTER
                }
            )

            setOnClickListener { action() }

            prepareContentFocus(this)

            setOnFocusChangeListener { v, focused ->
                if (focused) setSidebarExpanded(false)
                v.background = rounded(
                    if (focused) purple else card, 15
                )
            }
        }
    }

    private fun showAppSection(
        title: String,
        apps: List<ResolveInfo>
    ) {
        heading(
            title,
            "Escolha um aplicativo usando o controle remoto."
        )

        if (apps.isEmpty()) {
            emptyMessage(
                "Nenhum aplicativo encontrado nesta categoria."
            )
        } else {
            addAppRows(apps)
        }
    }

    private fun emptyMessage(message: String) {
        content.addView(
            label(message, 14f, muted).apply {
                setPadding(dp(4), dp(15), dp(4), dp(15))
            }
        )
    }

    private fun allApps(): List<ResolveInfo> {
        val found = LinkedHashMap<String, ResolveInfo>()

        val categories = listOf(
            Intent.CATEGORY_LEANBACK_LAUNCHER,
            Intent.CATEGORY_LAUNCHER
        )

        categories.forEach { category ->
            val intent = Intent(Intent.ACTION_MAIN)
                .addCategory(category)

            @Suppress("DEPRECATION")
            val results = packageManager.queryIntentActivities(
                intent, PackageManager.MATCH_ALL
            )

            results.forEach { info ->
                val pkg = info.activityInfo.packageName
                if (pkg != packageName && !found.containsKey(pkg)) {
                    found[pkg] = info
                }
            }
        }

        return found.values.sortedBy {
            it.loadLabel(packageManager).toString()
                .lowercase(Locale.getDefault())
        }
    }

    
private fun addAppRows(apps: List<ResolveInfo>) {
    var index = 0

    while (index < apps.size) {
        val appRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val end = minOf(index + 4, apps.size)

        for (i in index until end) {
            val info = apps[i]

            val tile = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                background = rounded(Color.TRANSPARENT, 18)
                setPadding(dp(8), dp(11), dp(8), dp(10))
                isFocusable = true
                isClickable = true

                addView(
                    ImageView(this@MainActivity).apply {
                        setImageDrawable(
                            info.loadIcon(packageManager)
                        )
                        scaleType = ImageView.ScaleType.FIT_CENTER
                    },
                    LinearLayout.LayoutParams(dp(48), dp(48))
                )

                addView(
                    label(
                        info.loadLabel(packageManager).toString(),
                        12f, white, true
                    ).apply {
                        gravity = Gravity.CENTER
                        maxLines = 2
                        setPadding(0, dp(7), 0, 0)
                    },
                    LinearLayout.LayoutParams(-1, dp(36))
                )

                setOnClickListener {
                    launchApp(info)
                }

                setOnFocusChangeListener { v, focused ->
                    if (focused) setSidebarExpanded(false)

                    v.background = rounded(
                        if (focused) purple else Color.TRANSPARENT,
                        18,
                        if (focused) neon else Color.TRANSPARENT
                    )

                    v.scaleX = if (focused) 1.06f else 1f
                    v.scaleY = if (focused) 1.06f else 1f
                }
            }

            val lp = LinearLayout.LayoutParams(
                dp(135), dp(112)
            )
            lp.rightMargin = dp(11)
            lp.bottomMargin = dp(11)
            appRow.addView(tile, lp)
        }

        content.addView(appRow)
        index = end
    }
}


    private fun launchApp(info: ResolveInfo) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            setClassName(
                info.activityInfo.packageName,
                info.activityInfo.name
            )
        }

        try {
            startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(
                this,
                "Não foi possível abrir o aplicativo.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun isVideoApp(info: ResolveInfo): Boolean {
        val text = (
            info.loadLabel(packageManager).toString() +
                " " + info.activityInfo.packageName
            ).lowercase(Locale.ROOT)

        return listOf(
            "netflix", "prime", "disney", "max",
            "youtube", "globoplay", "pluto", "plex",
            "paramount", "crunchyroll", "telecine"
        ).any { text.contains(it) }
    }

    
    private fun isMusicApp(info: ResolveInfo): Boolean {
        val text = (
            info.loadLabel(packageManager).toString() +
                " " + info.activityInfo.packageName
            ).lowercase(Locale.ROOT)

        return listOf(
            "spotify", "deezer", "youtube music",
            "amazon music", "tidal", "soundcloud", "music"
        ).any { text.contains(it) }
    }

    
private fun showSettings() {
    heading(
        "Configurações",
        "Personalize sua experiência no TCL Launcher."
    )

    val options = listOf(
        "Personalizar plano de fundo" to
            "Escolha uma imagem da galeria para sua TV.",
        "Atualizar aplicativos" to
            "Recarrega a lista de aplicativos da TV.",
        "Configurações do Android" to
            "Abre as configurações do sistema.",
        "Sobre o launcher" to
            "TCL Launcher 2.0 • Neon Edition."
    )

    options.forEach { (title, description) ->
        val item = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(18), dp(12), dp(18), dp(12))
            background = rounded(card, 15)
            isFocusable = true
            isClickable = true

            addView(label(title, 16f, white, true))

            addView(
                label(description, 13f, muted).apply {
                    setPadding(0, dp(5), 0, 0)
                }
            )

            setOnFocusChangeListener { v, focused ->
                v.background = rounded(
                    if (focused) purple else card,
                    15,
                    if (focused) neon else Color.TRANSPARENT
                )
            }

            setOnClickListener {
                when (title) {
                    "Personalizar plano de fundo" ->
                        openWallpaperPicker()

                    "Atualizar aplicativos" ->
                        showSection("Aplicativos")

                    "Configurações do Android" -> {
                        try {
                            startActivity(
                                Intent(
                                    android.provider.Settings.ACTION_SETTINGS
                                )
                            )
                        } catch (_: Exception) {
                            Toast.makeText(
                                this@MainActivity,
                                "Configurações indisponíveis.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    else -> Toast.makeText(
                        this@MainActivity,
                        "TCL Launcher 2.0 • Neon Edition",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        val lp = LinearLayout.LayoutParams(-1, dp(76))
        lp.bottomMargin = dp(10)
                content.addView(item, lp)
    }
}
}


            
  

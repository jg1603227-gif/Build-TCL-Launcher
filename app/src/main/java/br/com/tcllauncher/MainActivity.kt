
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

class MainActivity : Activity() {
    private val background = Color.rgb(10, 12, 18)
    private val panel = Color.rgb(20, 24, 34)
    private val panelSoft = Color.rgb(27, 32, 44)
    private val accent = Color.rgb(111, 92, 255)
    private val white = Color.rgb(245, 247, 252)
    private val muted = Color.rgb(153, 162, 180)
    private lateinit var content: LinearLayout
    private lateinit var clock: TextView
    private var selectedSection = "Início"
    private val handler = Handler(Looper.getMainLooper())

    private val clockTick = object : Runnable {
        override fun run() {
            if (::clock.isInitialized)
                clock.text = SimpleDateFormat(
                    "EEE, dd MMM  •  HH:mm",
                    Locale("pt", "BR")
                ).format(Date())

            handler.postDelayed(this, 30_000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = background
        window.navigationBarColor = background
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

    private fun rounded(color: Int, radius: Int = 18): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
        }

    private fun label(
        value: String,
        size: Float,
        color: Int,
        bold: Boolean = false
    ): TextView = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
        if (bold) typeface =
            Typeface.create("sans-serif", Typeface.BOLD)
        includeFontPadding = true
    }

    private fun buildShell() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(background)
            setPadding(dp(22), dp(18), dp(24), dp(18))
        }

        val sidebar = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(14), dp(12), dp(14))
            background = rounded(panel, 22)
        }

        val brand = label("TCL", 27f, white, true).apply {
            setPadding(dp(12), dp(8), 0, dp(2))
        }
        sidebar.addView(brand)

        sidebar.addView(
            label("L A U N C H E R", 9f, muted, true).apply {
                setPadding(dp(13), 0, 0, dp(28))
            }
        )

        val items = listOf(
            "Início" to "⌂",
            "Aplicativos" to "▦",
            "Filmes e séries" to "▶",
            "Música" to "♫",
            "Configurações" to "⚙"
        )

        items.forEach { (name, symbol) ->
            val item = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                isFocusable = true
                isClickable = true
                setPadding(dp(13), 0, dp(10), 0)
                background = rounded(
                    if (name == selectedSection) accent
                    else Color.TRANSPARENT, 14
                )
                contentDescription = name

                addView(
                    label(symbol, 22f, white, true),
                    LinearLayout.LayoutParams(dp(34), -2)
                )

                addView(label(name, 14f, white, name == selectedSection))

                setOnClickListener { showSection(name) }

                setOnFocusChangeListener { v, focused ->
                    if (name != selectedSection)
                        v.background = rounded(
                            if (focused) panelSoft
                            else Color.TRANSPARENT, 14
                        )

                    v.scaleX = if (focused) 1.035f else 1f
                    v.scaleY = if (focused) 1.035f else 1f
                }
            }

            val lp = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(52)
            )
            lp.bottomMargin = dp(7)
            sidebar.addView(item, lp)
        }

        sidebar.addView(View(this), LinearLayout.LayoutParams(1, 0, 1f))

        sidebar.addView(
            label("VERSÃO 2.0  •  TV", 9f, muted).apply {
                setPadding(dp(12), dp(8), 0, 0)
            }
        )

        val main = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(28), dp(4), 0, 0)
        }

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        top.addView(
            label("SEU ESPAÇO", 11f, muted, true),
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        clock = label("", 14f, muted)
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

        main.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        root.addView(sidebar, LinearLayout.LayoutParams(dp(205), -1))

        val mainLp = LinearLayout.LayoutParams(0, -1, 1f)
        mainLp.leftMargin = dp(24)
        root.addView(main, mainLp)

        setContentView(root)
    }

    private fun showSection(section: String) {
        selectedSection = section
        buildShell()
        content.removeAllViews()

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

        content.post {
            firstFocusable(content)?.requestFocus()
        }
    }

    private fun firstFocusable(group: ViewGroup): View? {
        for (i in 0 until group.childCount) {
            val child = group.getChildAt(i)
            if (child.isFocusable) return child
            if (child is ViewGroup)
                firstFocusable(child)?.let { return it }
        }
        return null
    }

    private fun sectionHeading(title: String, subtitle: String) {
        content.addView(
            label(title, 31f, white, true).apply {
                setPadding(0, dp(24), 0, dp(5))
            }
        )

        content.addView(
            label(subtitle, 15f, muted).apply {
                setPadding(0, 0, 0, dp(22))
            }
        )
    }

    private fun showHome() {
        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(28), dp(22), dp(28), dp(22))
            background = rounded(Color.rgb(32, 31, 57), 22)
        }

        hero.addView(label("TCL  /  HOME", 11f, Color.rgb(178, 168, 255), true))

        hero.addView(
            label("Tudo o que você gosta.\nEm um só lugar.", 30f, white, true)
                .apply { setPadding(0, dp(10), 0, dp(8)) }
        )

        hero.addView(
            label(
                "Acesse seus aplicativos e continue de onde quiser.",
                14f, muted
            )
        )

        val heroLp = LinearLayout.LayoutParams(-1, dp(190))
        heroLp.topMargin = dp(18)
        content.addView(hero, heroLp)

        content.addView(
            label("Acesso rápido", 21f, white, true).apply {
                setPadding(0, dp(26), 0, dp(13))
            }
        )

        val quick = allApps().take(6)

        if (quick.isEmpty())
            emptyMessage("Nenhum aplicativo compatível foi encontrado.")
        else
            addAppRows(quick)

        content.addView(
            label("Explore", 21f, white, true).apply {
                setPadding(0, dp(24), 0, dp(12))
            }
        )

        val hint = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(18), dp(16), dp(18), dp(16))
            background = rounded(panel, 16)
        }

        hint.addView(
            label("▦", 25f, accent, true),
            LinearLayout.LayoutParams(dp(48), -2)
        )

        val texts = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        texts.addView(label("Sua biblioteca de aplicativos", 16f, white, true))
        texts.addView(
            label(
                "Abra Aplicativos no menu lateral para ver tudo.",
                13f, muted
            )
        )

        hint.addView(texts)
        content.addView(hint, LinearLayout.LayoutParams(-1, dp(76)))
    }

    private fun showAppSection(title: String, apps: List<ResolveInfo>) {
        sectionHeading(
            title,
            if (apps.isEmpty())
                "Não encontramos aplicativos nesta categoria."
            else
                "Selecione um aplicativo usando o controle remoto."
        )

        if (apps.isEmpty()) {
            emptyMessage(
                "Os aplicativos instalados que oferecem abertura pela tela inicial aparecerão aqui."
            )
        } else {
            addAppRows(apps)
        }
    }

    private fun emptyMessage(message: String) {
        content.addView(
            label(message, 15f, muted).apply {
                setPadding(dp(4), dp(18), dp(4), dp(18))
            }
        )
    }

    private fun allApps(): List<ResolveInfo> {
        val pm = packageManager
        val found = LinkedHashMap<String, ResolveInfo>()

        val categories = listOf(
            Intent.CATEGORY_LEANBACK_LAUNCHER,
            Intent.CATEGORY_LAUNCHER
        )

        categories.forEach { category ->
            val intent = Intent(Intent.ACTION_MAIN).addCategory(category)

            @Suppress("DEPRECATION")
            val results = pm.queryIntentActivities(
                intent, PackageManager.MATCH_ALL
            )

            results.forEach { info ->
                val pkg = info.activityInfo.packageName
                if (pkg != packageName && !found.containsKey(pkg))
                    found[pkg] = info
            }
        }

        return found.values.sortedBy {
            it.loadLabel(pm).toString().lowercase(Locale.getDefault())
        }
    }

    private fun addAppRows(apps: List<ResolveInfo>) {
        val pm = packageManager
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
                    setPadding(dp(10), dp(13), dp(10), dp(12))
                    background = rounded(panelSoft, 17)
                    isFocusable = true
                    isClickable = true
                    contentDescription = info.loadLabel(pm).toString()

                    addView(
                        ImageView(this@MainActivity).apply {
                            setImageDrawable(info.loadIcon(pm))
                            scaleType = ImageView.ScaleType.FIT_CENTER
                        },
                        LinearLayout.LayoutParams(dp(48), dp(48))
                    )

                    addView(
                        label(info.loadLabel(pm).toString(), 13f, white, true)
                            .apply {
                                gravity = Gravity.CENTER
                                maxLines = 2
                                setPadding(0, dp(8), 0, 0)
                            },
                        LinearLayout.LayoutParams(-1, dp(38))
                    )

                    setOnClickListener { launchApp(info) }

                    setOnFocusChangeListener { v, focused ->
                        v.background = rounded(
                            if (focused) accent else panelSoft, 17
                        )
                        v.scaleX = if (focused) 1.07f else 1f
                        v.scaleY = if (focused) 1.07f else 1f
                    }
                }

                val lp = LinearLayout.LayoutParams(dp(142), dp(126))
                lp.rightMargin = dp(13)
                lp.bottomMargin = dp(13)
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
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(
                this,
                "Não foi possível abrir este aplicativo.",
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
            "netflix", "prime", "disney", "max", "hbo",
            "youtube", "globoplay", "pluto", "plex",
            "paramount", "crunchyroll", "telecine", "viki"
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
        sectionHeading(
            "Configurações",
            "Ajustes básicos do TCL Launcher."
        )

        val settings = listOf(
            "Atualizar lista de aplicativos" to
                "Recarrega os aplicativos disponíveis na TV.",
            "Configurações do Android" to
                "Abre as configurações do sistema da televisão.",
            "Sobre o launcher" to
                "TCL Launcher 2.0 • versão inicial de testes."
        )

        settings.forEach { (title, description) ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(18), dp(15), dp(18), dp(15))
                background = rounded(panelSoft, 15)
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
                        if (focused) accent else panelSoft, 15
                    )
                }

                setOnClickListener {
                    when (title) {
                        "Atualizar lista de aplicativos" ->
                            showSection("Aplicativos")

                        "Configurações do Android" ->
                            try {
                                startActivity(
                                    Intent(android.provider.Settings.ACTION_SETTINGS)
                                )
                            } catch (_: Exception) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Configurações indisponíveis.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        else ->
                            Toast.makeText(
                                this@MainActivity,
                                "TCL Launcher 2.0",
                                Toast.LENGTH_SHORT
                            ).show()
                    }
                }
            }

            val lp = LinearLayout.LayoutParams(-1, dp(76))
            lp.bottomMargin = dp(10)
            content.addView(card, lp)
        }
    }
}

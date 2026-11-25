package eu.effettoventuri.control

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import org.json.JSONObject

private const val BASE_URL = "https://control.effettoventuri.eu/"
private const val LOGIN_URL = "https://control.effettoventuri.eu/auth/login?url=https%3A//control.effettoventuri.eu/"
private const val HOST = "control.effettoventuri.eu"

class MainActivity : Activity() {

    private lateinit var webView: WebView
    private lateinit var prefs: SharedPreferences
    private var loginAttempted = false
    private var dialogShown = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val masterKey = MasterKey.Builder(this)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        prefs = EncryptedSharedPreferences.create(
            this,
            "credentials",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = AuthClient()
        }
        CookieManager.getInstance().setAcceptCookie(true)

        val root = FrameLayout(this)
        root.addView(
            webView,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        root.addView(buildGearButton())
        setContentView(root)

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState)
        } else {
            webView.loadUrl(BASE_URL)
        }
    }

    private fun buildGearButton(): TextView {
        val size = (34 * resources.displayMetrics.density).toInt()
        val margin = (12 * resources.displayMetrics.density).toInt()
        return TextView(this).apply {
            text = "\u2699"
            textSize = 17f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            alpha = 0.45f
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#333333"))
            }
            layoutParams = FrameLayout.LayoutParams(size, size, Gravity.BOTTOM or Gravity.END).apply {
                setMargins(margin, margin, margin, margin)
            }
            setOnClickListener { askCredentials(failed = false) }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }

    override fun onPause() {
        super.onPause()
        CookieManager.getInstance().flush()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }

    private inner class AuthClient : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            val uri = Uri.parse(url)
            if (uri.host != HOST) return

            if (uri.path?.startsWith("/auth/login") == true) {
                handleLoginPage(view)
            } else {
                loginAttempted = false
            }
        }
    }

    private fun handleLoginPage(view: WebView) {
        val user = prefs.getString("username", null)
        val pass = prefs.getString("password", null)

        if (!user.isNullOrEmpty() && !pass.isNullOrEmpty() && !loginAttempted) {
            loginAttempted = true
            val js = """
                (function() {
                    var u = document.querySelector('input[name="username"]');
                    var p = document.querySelector('input[name="password"]');
                    if (!u || !p) return;
                    u.value = ${JSONObject.quote(user)};
                    p.value = ${JSONObject.quote(pass)};
                    if (u.form) u.form.submit();
                })();
            """.trimIndent()
            view.evaluateJavascript(js, null)
        } else {
            // Pas d'identifiants, ou la tentative précédente a échoué
            askCredentials(failed = !user.isNullOrEmpty() && !pass.isNullOrEmpty())
        }
    }

    private fun askCredentials(failed: Boolean) {
        if (dialogShown) return
        dialogShown = true

        val pad = (16 * resources.displayMetrics.density).toInt()
        val userInput = EditText(this).apply {
            hint = "Nom d'utilisateur"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            setText(prefs.getString("username", ""))
        }
        val passInput = EditText(this).apply {
            hint = "Mot de passe"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            setText(prefs.getString("password", ""))
        }
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, 0)
            addView(userInput)
            addView(passInput)
        }

        AlertDialog.Builder(this)
            .setTitle(if (failed) "Identifiants invalides" else "Identifiants de connexion")
            .setView(layout)
            .setOnDismissListener { dialogShown = false }
            .setPositiveButton("Enregistrer") { _, _ ->
                prefs.edit()
                    .putString("username", userInput.text.toString().trim())
                    .putString("password", passInput.text.toString())
                    .apply()
                resetSession()
            }
            .setNeutralButton("Oublier") { _, _ ->
                prefs.edit().clear().apply()
                resetSession()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun resetSession() {
        loginAttempted = false
        CookieManager.getInstance().removeAllCookies {
            webView.loadUrl(LOGIN_URL)
        }
    }
}

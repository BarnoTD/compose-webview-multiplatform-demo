package com.clinikdb.webviewTest
import android.content.Context
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.Log
import android.webkit.WebView as AndroidWebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.multiplatform.webview.jsbridge.IJsMessageHandler
import com.multiplatform.webview.jsbridge.JsMessage
import com.multiplatform.webview.jsbridge.rememberWebViewJsBridge
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.WebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import org.json.JSONObject
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.util.Base64
import com.multiplatform.webview.web.rememberWebViewNavigator

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PrescriptionScreen()
                }
            }
        }
    }
}

@Composable
fun PrescriptionScreen() {
    val context = LocalContext.current
    var prescriptionText by remember { mutableStateOf("") }
    var configJson by remember { mutableStateOf<String?>(null) }
    
    val webViewState = rememberWebViewState("file:///android_asset/calendar_web/prescription.html")
    val navigator = rememberWebViewNavigator()
    val jsBridge = rememberWebViewJsBridge()

    var triggerImagePicker by remember { mutableStateOf(false) }

    val contentResolver = context.contentResolver
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        triggerImagePicker = false
        if (uri != null) {
            val mimeType = contentResolver.getType(uri) ?: "image/png"
            val bytes = contentResolver.openInputStream(uri)?.readBytes()
            if (bytes != null) {
                val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                val dataUrl = "data:$mimeType;base64,$base64"
                navigator.evaluateJavaScript("window.setLogoImage(\"$dataUrl\")")
            }
        }
    }

    if (triggerImagePicker) {
        LaunchedEffect(triggerImagePicker) {
            imagePickerLauncher.launch("image/*")
        }
    }

    LaunchedEffect(jsBridge) {
        jsBridge.register(SaveConfigHandler { json ->
            Log.d("Config", json)
            configJson = json
        })
        jsBridge.register(object : IJsMessageHandler {
            override fun methodName(): String = "uploadLogo"
            override fun handle(message: JsMessage, navigator: WebViewNavigator?, callback: (String) -> Unit) {
                triggerImagePicker = true
                callback("{}")
            }
        })
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = prescriptionText,
                    onValueChange = { prescriptionText = it },
                    label = { Text("Prescription Content") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        configJson?.let { config ->
                            printHtml(context, config, prescriptionText)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = configJson != null && prescriptionText.isNotBlank()
                ) {
                    Text(if (configJson == null) "Export Template Config first" else "Print Prescription to PDF")
                }
            }
        }

        Divider()

        WebView(
            state = webViewState,
            navigator = navigator,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            webViewJsBridge = jsBridge
        )
    }
}

class SaveConfigHandler(private val onConfigReceived: (String) -> Unit) : IJsMessageHandler {
    override fun methodName(): String = "saveConfig"

    override fun handle(
        message: JsMessage,
        navigator: WebViewNavigator?,
        callback: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            onConfigReceived(message.params)
            callback("{\"success\": true}")
        }
    }
}

fun printHtml(context: Context, jsonStr: String, contentText: String) {
    val html = generateHtml(context, jsonStr, contentText)
    val webView = AndroidWebView(context)
    webView.webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: AndroidWebView, url: String) {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
            val printAdapter = view.createPrintDocumentAdapter("prescription_document")
            printManager.print("Prescription", printAdapter, PrintAttributes.Builder().build())
        }
    }
    webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
}

fun generateHtml(context: Context, configJson: String, contentText: String): String {
    val template = context.assets.open("print_template.html").bufferedReader().use { it.readText() }

    val json = JSONObject(configJson)
    val header = json.getJSONObject("header")
    val content = json.getJSONObject("content")
    val footer = json.getJSONObject("footer")

    val logoPosition = header.getString("logoPosition")

    val headerLeft = header.getString("leftContent").split("\n").joinToString("") { "<div class=\"header-text\">$it</div>" }
    val headerCenter = header.getString("centerContent").split("\n").joinToString("") { "<div class=\"header-text\">$it</div>" }
    val headerRight = header.getString("rightContent").split("\n").joinToString("") { "<div class=\"header-text\">$it</div>" }
    
    val footerContent = footer.getString("content").split("\n").joinToString("") { "<div class=\"footer-text\">$it</div>" }
    val contentLabel = content.getString("label").split("\n").joinToString("") { "<div>$it</div>" }

    val logoBase64 = header.optString("logoBase64", "")
    val logoSrc = if (logoBase64.isNotEmpty()) logoBase64 else "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'%3E%3Cpath fill='%237dbdc1' d='M50 10 L55 35 L80 35 L60 50 L70 75 L50 60 L30 75 L40 50 L20 35 L45 35 Z'/%3E%3Ccircle cx='50' cy='50' r='45' fill='none' stroke='%237dbdc1' stroke-width='3'/%3E%3C/svg%3E"
    
    val logoHtml = if (header.getBoolean("enableLogo")) """
        <div class="header-logo logo-$logoPosition">
            <img src="$logoSrc" alt="Logo">
        </div>
    """.trimIndent() else ""

    val watermarkHtml = if (content.getBoolean("enableWatermark")) """
        <div class="watermark">${content.getString("watermarkText")}</div>
    """.trimIndent() else ""

    val contentLabelHtml = if (content.getString("label").isNotEmpty()) """
        <div class="content-label">$contentLabel</div>
    """.trimIndent() else ""

    val cssVars = """
        <style id="dynamic-css-vars">
            :root {
                --header-height: ${header.getString("height")}mm;
                --header-bg-color: ${header.getString("backgroundColor")};
                --header-text-color: ${header.getString("textColor")};
                --header-font-size: ${header.getString("fontSize")}px;
                --logo-order: ${if (logoPosition == "left") "1" else if (logoPosition == "center") "2" else "3"};
                --logo-margin: ${if (logoPosition == "center") "0 auto" else if (logoPosition == "right") "0 0 0 auto" else "0 auto 0 0"};
                --header-logo-size: ${header.getString("logoSize")}px;
                --content-padding: ${content.getString("padding")}mm;
                --content-bg-color: ${content.getString("backgroundColor")};
                --content-text-color: ${content.getString("textColor")};
                --footer-height: ${footer.getString("height")}mm;
                --footer-bg-color: ${footer.getString("backgroundColor")};
                --footer-text-color: ${footer.getString("textColor")};
                --footer-font-size: ${footer.getString("fontSize")}px;
            }
        </style>
    """.trimIndent()

    return template
        .replace("<!-- DYNAMIC_CSS_VARS -->", cssVars)
        .replace("<!-- LOGO_HTML -->", logoHtml)
        .replace("<!-- HEADER_LEFT -->", headerLeft)
        .replace("<!-- HEADER_CENTER -->", headerCenter)
        .replace("<!-- HEADER_RIGHT -->", headerRight)
        .replace("<!-- WATERMARK_HTML -->", watermarkHtml)
        .replace("<!-- CONTENT_LABEL_HTML -->", contentLabelHtml)
        .replace("<!-- CONTENT_TEXT -->", contentText)
        .replace("<!-- FOOTER_CONTENT -->", footerContent)
}
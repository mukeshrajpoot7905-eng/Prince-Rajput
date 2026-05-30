package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AdsterraAdView(modifier: Modifier = Modifier) {
    val rawHtml = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
            <style>
                body, html {
                    margin: 0;
                    padding: 0;
                    width: 100%;
                    height: 100%;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    overflow: hidden;
                    background-color: transparent;
                }
            </style>
        </head>
        <body>
            <script type="text/javascript">
                atOptions = {
                    'key' : 'f1d7b19f7fd823e38f2940b3201e7252',
                    'format' : 'iframe',
                    'height' : 50,
                    'width' : 320,
                    'params' : {}
                };
            </script>
            <script type="text/javascript" src="https://www.highperformanceformat.com/f1d7b19f7fd823e38f2940b3201e7252/invoke.js"></script>
        </body>
        </html>
    """.trimIndent()

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(55.dp),
        factory = { context ->
            WebView(context).apply {
                // Enable necessary settings for loading of Adsterra's iframe and key execution
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.databaseEnabled = true
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                settings.setSupportZoom(false)
                settings.javaScriptCanOpenWindowsAutomatically = true
                
                // Allow mixed content (HTTP and HTTPS resources together) for ad redirects
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                
                // Enable third-party cookies for networks tracking and user interactions
                CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                
                // Transparent background
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                        val url = request.url.toString()
                        
                        // Let internal calls to highperformanceformat, script loaders, syndications, and doubleclick load in iframe
                        if (url.startsWith("http://") || url.startsWith("https://")) {
                            if (url.contains("highperformanceformat.com") || 
                                url.contains("syndication") || 
                                url.contains("f1d7b19f7fd823e38f2940b3201e7252") ||
                                url.contains("native") ||
                                url.contains("delivery")
                            ) {
                                return false
                            } else {
                                // For external user-click navigations, redirect safely to appropriate external browser
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                return true
                            }
                        }
                        return false
                    }
                }
                
                // Content loading
                loadDataWithBaseURL("https://www.highperformanceformat.com/", rawHtml, "text/html", "UTF-8", null)
            }
        },
        update = { /* no-op */ }
    )
}

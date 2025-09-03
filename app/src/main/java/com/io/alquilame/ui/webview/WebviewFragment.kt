package com.io.alquilame.ui.webview

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.io.alquilame.databinding.FragmentGalleryBinding
import com.io.alquilame.data.SecureCredentialsStorage
import com.io.alquilame.BuildConfig
import java.security.MessageDigest
import java.time.Instant
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.webkit.ValueCallback
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.os.Build
import android.widget.Toast
import android.webkit.DownloadListener
import android.webkit.URLUtil
import android.app.DownloadManager
import android.content.Context
import android.os.Environment

class WebviewFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorTextView: TextView
    private lateinit var credentialsStorage: SecureCredentialsStorage
    
    private var webAppUrl: String? = null
    private var fileUploadCallback: ValueCallback<Array<Uri>>? = null
    
    companion object {
        private const val FILE_CHOOSER_REQUEST_CODE = 1002
        private const val STORAGE_PERMISSION_REQUEST_CODE = 1003
        private const val DOWNLOAD_PERMISSION_REQUEST_CODE = 1004
    }
    

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews()
        initCredentialsStorage()
        setupWebView()
        loadWebApp()
    }
    
    private fun initViews() {
        webView = binding.webviewMain
        progressBar = binding.progressBar
        errorTextView = binding.tvError
    }
    
    private fun initCredentialsStorage() {
        credentialsStorage = SecureCredentialsStorage(requireContext())
        
        val username = credentialsStorage.getUsername()
        val apiKey = credentialsStorage.getApiKey()
        
        if (username != null && apiKey != null) {
            // Generate secure URL with dynamic path
            webAppUrl = generateSecureUrl(username, apiKey)
        } else {
            webAppUrl = null
        }
    }
    
    private fun generateSecureUrl(tenantSlug: String, secretSlug: String): String {
        // Generate timestamp as random key
        val randomKey = Instant.now().toEpochMilli().toString()
        
        // Generate dynamic secret: tenant_slug + secret_slug + random_key + SECRET_STRING_LOGIN
        val combinedSecret = "$tenantSlug|$secretSlug|$randomKey|${BuildConfig.SECRET_STRING_LOGIN}"
        val hashedSecret = generateSHA256Hash(combinedSecret)
        
        // Use first 16 characters for URL friendliness
        val secretTenantSlugDynamic = hashedSecret.substring(0, 16)
        
        // Build secure URL: /s/tenant/:tenant_slug/:secret_tenant_slug_dynamic/:random_key
        return "https://$tenantSlug.alquilame.io/s/tenant/$tenantSlug/$secretTenantSlugDynamic/$randomKey"
    }
    
    private fun generateSHA256Hash(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.apply {
            settings.apply {
                // Enable JavaScript
                javaScriptEnabled = true
                
                // Enable DOM storage
                domStorageEnabled = true
                
                // Enable database storage
                databaseEnabled = true
                
                // Enable zoom controls
                builtInZoomControls = true
                displayZoomControls = false
                
                // Enable responsive design
                useWideViewPort = true
                loadWithOverviewMode = true
                
                // Mixed content mode for HTTPS
                mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                
                // Enable file access
                allowFileAccess = true
                allowContentAccess = true
                
                // User agent
                userAgentString = userAgentString + " AlquilameApp/1.0"
            }
            
            // Set download listener for file downloads (especially PDFs)
            setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
                handleDownload(url, userAgent, contentDisposition, mimeType, contentLength)
            }
            
            // WebViewClient to handle page loading
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    showLoading()
                }
                
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    hideLoading()
                }
                
                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    if (request?.isForMainFrame == true) {
                        showError("Failed to load web app. Please check your connection.")
                    }
                }
                
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    // Allow navigation within alquilame.io domain (including subdomains)
                    val url = request?.url?.toString()
                    return if (url != null && url.contains(".alquilame.io")) {
                        view?.loadUrl(url)
                        true
                    } else {
                        // For external links, you might want to open in browser
                        super.shouldOverrideUrlLoading(view, request)
                    }
                }
            }
            
            // WebChromeClient to handle progress and titles
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    // You can update progress bar here if needed
                    if (newProgress == 100) {
                        hideLoading()
                    }
                }
                
                override fun onReceivedTitle(view: WebView?, title: String?) {
                    super.onReceivedTitle(view, title)
                    // Update fragment title if needed
                }
                
                override fun onShowFileChooser(
                    webView: WebView?,
                    filePathCallback: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams?
                ): Boolean {
                    // Cancel any existing file chooser
                    fileUploadCallback?.onReceiveValue(null)
                    fileUploadCallback = filePathCallback
                    
                    // Check for storage permissions
                    if (!hasStoragePermissions()) {
                        requestStoragePermissions()
                        return true
                    }
                    
                    return openFileChooser(fileChooserParams)
                }
            }
        }
    }
    
    private fun loadWebApp() {
        try {
            if (webAppUrl != null) {
                showLoading()
                webView.loadUrl(webAppUrl!!)
            } else {
                showError("No API key found. Please log in again.")
            }
        } catch (e: Exception) {
            showError("Error loading web app: ${e.message}")
        }
    }
    
    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        webView.visibility = View.GONE
        errorTextView.visibility = View.GONE
    }
    
    private fun hideLoading() {
        progressBar.visibility = View.GONE
        webView.visibility = View.VISIBLE
        errorTextView.visibility = View.GONE
    }
    
    private fun showError(message: String = "Failed to load web app") {
        progressBar.visibility = View.GONE
        webView.visibility = View.GONE
        errorTextView.text = message
        errorTextView.visibility = View.VISIBLE
    }
    
    fun canGoBack(): Boolean {
        return ::webView.isInitialized && webView.canGoBack()
    }
    
    fun goBack() {
        if (::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
        }
    }
    
    fun reload() {
        if (::webView.isInitialized) {
            webView.reload()
        }
    }
    
    private fun hasStoragePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ requires specific media permissions
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 12 and below
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    private fun requestStoragePermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        
        requestPermissions(permissions, STORAGE_PERMISSION_REQUEST_CODE)
    }
    
    private fun openFileChooser(fileChooserParams: WebChromeClient.FileChooserParams?): Boolean {
        try {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
                
                // Allow multiple file selection if supported
                if (fileChooserParams?.mode == WebChromeClient.FileChooserParams.MODE_OPEN_MULTIPLE) {
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                }
                
                // Accept specific mime types if specified
                fileChooserParams?.acceptTypes?.let { acceptTypes ->
                    if (acceptTypes.isNotEmpty()) {
                        putExtra(Intent.EXTRA_MIME_TYPES, acceptTypes)
                    }
                }
            }
            
            startActivityForResult(
                Intent.createChooser(intent, "Choose File"),
                FILE_CHOOSER_REQUEST_CODE
            )
            return true
        } catch (e: Exception) {
            fileUploadCallback?.onReceiveValue(null)
            fileUploadCallback = null
            return false
        }
    }
    
    private fun handleDownload(
        url: String,
        userAgent: String,
        contentDisposition: String,
        mimeType: String,
        contentLength: Long
    ) {
        // Check if we have write permissions for downloads
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ uses scoped storage, no permission needed for Downloads directory
            startDownload(url, userAgent, contentDisposition, mimeType)
        } else {
            // Android 9 and below need WRITE_EXTERNAL_STORAGE permission
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                == PackageManager.PERMISSION_GRANTED) {
                startDownload(url, userAgent, contentDisposition, mimeType)
            } else {
                // Store download info and request permission
                pendingDownloadUrl = url
                pendingDownloadUserAgent = userAgent
                pendingDownloadContentDisposition = contentDisposition
                pendingDownloadMimeType = mimeType
                
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    DOWNLOAD_PERMISSION_REQUEST_CODE
                )
            }
        }
    }
    
    private var pendingDownloadUrl: String? = null
    private var pendingDownloadUserAgent: String? = null
    private var pendingDownloadContentDisposition: String? = null
    private var pendingDownloadMimeType: String? = null
    
    private fun startDownload(url: String, userAgent: String, contentDisposition: String, mimeType: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setMimeType(mimeType)
                
                // Set filename - try to get from content disposition or URL
                val filename = URLUtil.guessFileName(url, contentDisposition, mimeType)
                
                // Show notification during download
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setTitle("Downloading Contract")
                setDescription("Downloading $filename")
                
                // Set destination in Downloads directory
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
                
                // Allow download over mobile and WiFi
                setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                
                // Allow roaming
                setAllowedOverRoaming(true)
                
                // Add request headers if needed
                addRequestHeader("User-Agent", userAgent)
            }
            
            val downloadManager = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = downloadManager.enqueue(request)
            
            Toast.makeText(requireContext(), "Download started...", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Download failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            if (fileUploadCallback == null) return
            
            val results = when {
                resultCode != Activity.RESULT_OK -> null
                data == null -> null
                data.dataString != null -> arrayOf(Uri.parse(data.dataString))
                data.clipData != null -> {
                    val clipData = data.clipData!!
                    Array(clipData.itemCount) { i ->
                        clipData.getItemAt(i).uri
                    }
                }
                else -> null
            }
            
            fileUploadCallback?.onReceiveValue(results)
            fileUploadCallback = null
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            STORAGE_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.any { it == PackageManager.PERMISSION_GRANTED }) {
                    // At least one permission was granted, try to open file chooser
                    if (fileUploadCallback != null) {
                        openFileChooser(null)
                    }
                } else {
                    // Permission denied
                    Toast.makeText(requireContext(), "Storage permission is required to upload files", Toast.LENGTH_LONG).show()
                    fileUploadCallback?.onReceiveValue(null)
                    fileUploadCallback = null
                }
            }
            
            DOWNLOAD_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, start the pending download
                    pendingDownloadUrl?.let { url ->
                        startDownload(
                            url,
                            pendingDownloadUserAgent ?: "",
                            pendingDownloadContentDisposition ?: "",
                            pendingDownloadMimeType ?: "application/octet-stream"
                        )
                    }
                } else {
                    // Permission denied
                    Toast.makeText(requireContext(), "Storage permission is required to download files", Toast.LENGTH_LONG).show()
                }
                
                // Clear pending download info
                clearPendingDownload()
            }
        }
    }
    
    private fun clearPendingDownload() {
        pendingDownloadUrl = null
        pendingDownloadUserAgent = null
        pendingDownloadContentDisposition = null
        pendingDownloadMimeType = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        
        // Clean up file upload callback
        fileUploadCallback?.onReceiveValue(null)
        fileUploadCallback = null
        
        // Clean up pending downloads
        clearPendingDownload()
        
        if (::webView.isInitialized) {
            webView.destroy()
        }
        _binding = null
    }
}
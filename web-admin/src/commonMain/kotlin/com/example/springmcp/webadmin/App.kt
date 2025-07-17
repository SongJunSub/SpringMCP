package com.example.springmcp.webadmin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.springmcp.webadmin.api.UrlShortenerApi
import com.example.springmcp.webadmin.api.UrlShortenerRequest
import com.example.springmcp.webadmin.api.UrlShortenerResponse
import kotlinx.coroutines.launch

@Composable
fun App() {
    val coroutineScope = rememberCoroutineScope()
    val api = remember { UrlShortenerApi(baseUrl = "http://localhost:8080/api/shorten") }
    var longUrl by remember { mutableStateOf("") }
    var customKey by remember { mutableStateOf("") }
    var urlList by remember { mutableStateOf<List<UrlShortenerResponse>>(emptyList()) }

    fun loadUrls() {
        coroutineScope.launch {
            try {
                urlList = api.getAllUrls()
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(Unit) {
        loadUrls()
    }

    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Spring MCP Admin Panel", style = MaterialTheme.typography.h4)
            Spacer(modifier = Modifier.height(16.dp))

            // URL Input Form
            Column {
                OutlinedTextField(
                    value = longUrl,
                    onValueChange = { longUrl = it },
                    label = { Text("Long URL") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = customKey,
                    onValueChange = { customKey = it },
                    label = { Text("Custom Key (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                api.shortenUrl(UrlShortenerRequest(longUrl, customKey.ifEmpty { null }))
                                longUrl = ""
                                customKey = ""
                                loadUrls()
                            } catch (e: Exception) {
                                // Handle error
                                e.printStackTrace()
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Shorten")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // URL List
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(urlList) { url ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), elevation = 2.dp) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Short Key: ${url.shortKey}", style = MaterialTheme.typography.body1)
                                Text("Original URL: ${url.longUrl}", style = MaterialTheme.typography.body2)
                                Text("Created At: ${url.createdAt}", style = MaterialTheme.typography.caption)
                            }
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        try {
                                            api.deleteUrl(url.shortKey)
                                            loadUrls()
                                        } catch (e: Exception) {
                                            // Handle error
                                            e.printStackTrace()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error)
                            ) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}

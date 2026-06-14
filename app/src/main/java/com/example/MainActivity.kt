package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import kotlinx.coroutines.delay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.PythonFile
import com.example.ui.IdeViewModel
import com.example.ui.IdeViewModelFactory
import com.example.ui.highlightPython
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as MainApplication
        val factory = IdeViewModelFactory(app.repository)

        setContent {
            var currentScreen by remember { mutableStateOf("splash") }

            when (currentScreen) {
                "splash" -> {
                    SplashScreen {
                        currentScreen = "main"
                    }
                }
                "about" -> {
                    AboutScreen(onBack = { currentScreen = "main" })
                }
                "main" -> {
                val viewModel: IdeViewModel = viewModel(factory = factory)
                val allFiles by viewModel.allFiles.collectAsState()
                val currentFile by viewModel.currentFile.collectAsState()

                var outputConsole by remember { mutableStateOf("") }
                val coroutineScope = rememberCoroutineScope()
                var showAddDialog by remember { mutableStateOf(false) }
                var newFileName by remember { mutableStateOf("") }
                val context = androidx.compose.ui.platform.LocalContext.current

            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = Color(0xFF1E1E1E),
                    surface = Color(0xFF2D2D2D)
                )
            ) {
                Scaffold(
                    topBar = {
                        Column {
                            TopAppBar(
                                title = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .background(Color(0xFFFF5555), RoundedCornerShape(8.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("PY", color = Color(0xFF1E1E1E), fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text("Mini Python IDE", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium, letterSpacing = (-0.5).sp)
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = Color(0xFF1E1E1E)
                                ),
                                actions = {
                                    IconButton(onClick = { showAddDialog = true }) {
                                        Icon(Icons.Default.Add, contentDescription = "Add File", tint = Color.White)
                                    }
                                    var showMenu by remember { mutableStateOf(false) }
                                    Box {
                                        IconButton(onClick = { showMenu = true }) {
                                            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                                        }
                                        DropdownMenu(
                                            expanded = showMenu,
                                            onDismissRequest = { showMenu = false },
                                            modifier = Modifier.background(Color(0xFF2D2D2D))
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("About App", color = Color.White) },
                                                onClick = {
                                                    showMenu = false
                                                    currentScreen = "about"
                                                }
                                            )
                                        }
                                    }
                                }
                            )
                            Divider(color = Color(0xFF333333), thickness = 1.dp)
                        }
                    },
                    floatingActionButton = {},
                    containerColor = Color(0xFF1E1E1E)
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(Color(0xFF1E1E1E))
                    ) {
                        // File Tabs
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF121212))
                        ) {
                            items(allFiles) { file ->
                                val isSelected = file.id == currentFile?.id
                                Box(
                                    modifier = Modifier
                                        .background(if (isSelected) Color(0xFF1E1E1E) else Color.Transparent)
                                        .clickable { viewModel.selectFile(file) }
                                        .height(44.dp)
                                ) {
                                    if (isSelected) {
                                        Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Color(0xFFFF5555)).align(Alignment.TopCenter))
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 16.dp).fillMaxHeight()
                                    ) {
                                        Text(
                                            text = if (file.name.endsWith(".py")) "🐍" else "📄",
                                            modifier = Modifier.padding(end = 8.dp),
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color.White.copy(alpha = 0.6f)
                                        )
                                        Text(
                                            text = file.name,
                                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                                        )
                                    }
                                    if (!isSelected) {
                                        Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(Color(0xFF333333)).align(Alignment.CenterEnd))
                                    }
                                }
                            }
                        }
                        Divider(color = Color(0xFF333333), thickness = 1.dp)

                        // Editor Area
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .background(Color(0xFF1E1E1E))
                                .padding(16.dp)
                        ) {
                            if (currentFile != null) {
                                val editorVerticalScroll = rememberScrollState()
                                val editorHorizontalScroll = rememberScrollState()
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(editorVerticalScroll)
                                ) {
                                    val lineCount = currentFile!!.content.count { it == '\n' } + 1
                                    Column(
                                        modifier = Modifier.padding(end = 16.dp),
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        for (i in 1..maxOf(lineCount, 10)) {
                                            Text(
                                                text = "$i",
                                                color = Color.White.copy(alpha = 0.3f),
                                                fontSize = 15.sp,
                                                fontFamily = FontFamily.Monospace,
                                                lineHeight = 22.sp
                                            )
                                        }
                                    }
                                    var textFieldValue by remember(currentFile!!.id) {
                                        mutableStateOf(TextFieldValue(currentFile!!.content))
                                    }

                                    LaunchedEffect(currentFile!!.content) {
                                        if (currentFile!!.content != textFieldValue.text) {
                                            textFieldValue = textFieldValue.copy(text = currentFile!!.content)
                                        }
                                    }

                                    BasicTextField(
                                        value = textFieldValue,
                                        onValueChange = { newValue ->
                                            var updatedValue = newValue
                                            if (newValue.text.length - textFieldValue.text.length == 1 && newValue.selection.start == newValue.selection.end) {
                                                val cursor = newValue.selection.start
                                                if (cursor > 0 && newValue.text[cursor - 1] == '\n') {
                                                    val textBeforeNewline = newValue.text.substring(0, cursor - 1)
                                                    val lastLine = textBeforeNewline.substringAfterLast('\n')
                                                    val indentation = lastLine.takeWhile { it == ' ' || it == '\t' }
                                                    var extraIndent = ""
                                                    if (lastLine.trimEnd().endsWith(":")) {
                                                        extraIndent = "    "
                                                    }
                                                    val indent = indentation + extraIndent
                                                    if (indent.isNotEmpty()) {
                                                        val newText = newValue.text.substring(0, cursor) + indent + newValue.text.substring(cursor)
                                                        updatedValue = newValue.copy(
                                                            text = newText,
                                                            selection = TextRange(cursor + indent.length)
                                                        )
                                                    }
                                                }
                                            }
                                            textFieldValue = updatedValue
                                            viewModel.updateContent(updatedValue.text)
                                        },
                                        textStyle = TextStyle(
                                            color = Color.White,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 15.sp,
                                            lineHeight = 22.sp
                                        ),
                                        cursorBrush = SolidColor(Color.White),
                                        visualTransformation = { text ->
                                            val highlighted = highlightPython(text.text)
                                            TransformedText(highlighted, OffsetMapping.Identity)
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .horizontalScroll(editorHorizontalScroll)
                                    )
                                }
                            } else {
                                Text(
                                    "No file selected. Create a new file.",
                                    color = Color.Gray,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }

                        if (currentFile != null) {
                            Divider(color = Color(0xFF333333), thickness = 1.dp)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1A1A1A))
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = {
                                        currentFile?.let { file ->
                                            viewModel.saveCurrentFile()
                                            coroutineScope.launch {
                                                val result = withContext(Dispatchers.IO) {
                                                    PythonRunner.runPythonCode(file.content)
                                                }
                                                outputConsole = "$result\n"
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5555), contentColor = Color(0xFF1E1E1E)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("RUN", fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { com.example.utils.FileExportUtils.savePythonFile(context, currentFile!!.name, currentFile!!.content) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333), contentColor = Color.White),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("SAVE", fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { com.example.utils.FileExportUtils.shareScript(context, currentFile!!.content) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333), contentColor = Color.White),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("SHARE", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Output Console
                        Divider(color = Color(0xFF333333), thickness = 1.dp)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(192.dp)
                                .background(Color(0xFF121212))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1A1A1A))
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "CONSOLE OUTPUT",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { outputConsole = "" },
                                        modifier = Modifier.size(24.dp).padding(end = 8.dp)
                                    ) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear Console", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                                    }
                                    Text(
                                        text = "CONNECTED",
                                        color = Color(0xFF4CAF50),
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                            Divider(color = Color(0xFF333333), thickness = 1.dp)
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = outputConsole,
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }

                    if (showAddDialog) {
                        AlertDialog(
                            // ... dialog remains unchanged, let's keep all unchanged contents but properly close the braces
                            onDismissRequest = { showAddDialog = false },
                            title = { Text("New Python File") },
                            text = {
                                OutlinedTextField(
                                    value = newFileName,
                                    onValueChange = { newFileName = it },
                                    label = { Text("File Name (e.g. script.py)") },
                                    singleLine = true
                                )
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    if (newFileName.isNotBlank()) {
                                        viewModel.createFile(newFileName)
                                        newFileName = ""
                                        showAddDialog = false
                                    }
                                }) { Text("Create") }
                            },
                            dismissButton = {
                                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
                            }
                        )
                    }
                }
            }
            } // end of main -> block
            } // end of when block
        }
    }
}

@Composable
fun SplashScreen(onFinish: () -> Unit) {

    LaunchedEffect(true) {
        delay(2000)
        onFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text(
                text = "🐍 Python Terminal IDE",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Run • Code • Learn",
                color = Color(0xFF4B8BBE),
                fontSize = 14.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About Application", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E1E1E)
                )
            )
        },
        containerColor = Color(0xFF1E1E1E)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF1E1E1E))
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text(
                text = "Python Terminal IDE",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Versi: 1.0.0",
                color = Color(0xFF4B8BBE),
                fontSize = 16.sp
            )
            Text(
                text = "Mode: Offline Learning Tool",
                color = Color.Gray,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            Divider(color = Color(0xFF333333), thickness = 1.dp)
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "DEVELOPER",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Anonim",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            Divider(color = Color(0xFF333333), thickness = 1.dp)
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "DESKRIPSI APLIKASI",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            val description = """
                Python Terminal IDE adalah aplikasi mini IDE Python untuk Android yang memungkinkan user menulis, mengedit, menjalankan, dan mengelola kode Python langsung di HP tanpa internet.
                
                Aplikasi ini fokus pada kesederhanaan, kecepatan, dan efisiensi, cocok untuk pemula maupun pengguna lanjut.
                
                Fitur multi-file memungkinkan user membuat beberapa file Python dalam satu project, mengeditnya secara terpisah, dan menjalankannya kapan saja.
                
                Engine Python menggunakan Chaquopy untuk menjalankan kode secara lokal (offline), tanpa server eksternal.
                
                Aplikasi juga mendukung export file .py, share script ke WhatsApp/Telegram/aplikasi lain, serta penyimpanan lokal di perangkat.
                
                Aplikasi dioptimalkan untuk Android 15 dan device low-end agar tetap ringan dan stabil.
                Tidak ada internet, tidak ada tracking, tidak ada pengumpulan data user.
            """.trimIndent()
            
            Text(
                text = description,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Cocok untuk:",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            val list = listOf("Belajar Python", "Testing script cepat", "Latihan coding mobile", "Eksperimen offline")
            list.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                    Box(modifier = Modifier.size(6.dp).background(Color(0xFFFF5555), RoundedCornerShape(3.dp)))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = item,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
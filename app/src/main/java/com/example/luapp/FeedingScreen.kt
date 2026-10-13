package com.example.luapp

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedingScreen(viewModel: FeedingViewModel) {
    var activeDialogType by remember { mutableStateOf<ActivityType?>(null) }
    var editingActivity by remember { mutableStateOf<Feeding?>(null) }
    var amountText by remember { mutableStateOf("") }
    
    var isNow by remember { mutableStateOf(true) }
    var selectedCalendar by remember { mutableStateOf(Calendar.getInstance()) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    // Al editar, inicializamos los valores
    LaunchedEffect(editingActivity) {
        editingActivity?.let {
            amountText = it.amountMl.toString()
            selectedCalendar = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            isNow = false
            activeDialogType = it.type
        }
    }

    // Pager state: 500 páginas para poder ir hacia atrás. La página 499 es hoy.
    val pagerState = rememberPagerState(pageCount = { 500 }, initialPage = 499)
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        MoonBackground()

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { 
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Luapp",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.NightsStay,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp),
                                    tint = Color(0xFFFFD600)
                                )
                            }
                            val dateText = remember(pagerState.currentPage) {
                                val cal = Calendar.getInstance()
                                cal.add(Calendar.DAY_OF_YEAR, pagerState.currentPage - 499)
                                val sdf = SimpleDateFormat("EEEE, d MMMM", Locale("es", "ES"))
                                sdf.format(cal.time).replaceFirstChar { it.uppercase() }
                            }
                            Text(
                                text = dateText,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                    )
                )
            },
            floatingActionButton = {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Botón Baño
                    SmallFloatingActionButton(
                        onClick = { activeDialogType = ActivityType.BATH },
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("🛁", fontSize = 24.sp)
                    }

                    // Botón Caca
                    SmallFloatingActionButton(
                        onClick = { activeDialogType = ActivityType.POOP },
                        containerColor = Color(0xFFEFEBE9), // Color cremita/café muy claro
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("💩", fontSize = 24.sp)
                    }

                    // Botón Biberón (Principal)
                    LargeFloatingActionButton(
                        onClick = { activeDialogType = ActivityType.FEEDING },
                        containerColor = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("🍼", fontSize = 36.sp)
                    }
                }
            }
        ) { padding ->
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) { page ->
                val dayOffset = page - 499
                val displayDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, dayOffset) }
                
                val dailyActivities = viewModel.feedings.filter { activity ->
                    val activityDate = Calendar.getInstance().apply { timeInMillis = activity.timestamp }
                    activityDate.get(Calendar.DAY_OF_YEAR) == displayDate.get(Calendar.DAY_OF_YEAR) &&
                    activityDate.get(Calendar.YEAR) == displayDate.get(Calendar.YEAR)
                }

                if (dailyActivities.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (dayOffset == 0) "Empieza el día con un biberón..." else "No hay registros este día",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            FeedingSummary(dailyActivities)
                        }
                        items(dailyActivities) { activity ->
                            FeedingItem(
                                activity = activity,
                                onLongClick = { editingActivity = activity }
                            )
                        }
                    }
                }
            }
        }
    }

    if (activeDialogType != null) {
        AlertDialog(
            onDismissRequest = { 
                activeDialogType = null 
                editingActivity = null
                amountText = ""
            },
            shape = RoundedCornerShape(28.dp),
            title = { 
                val title = if (editingActivity != null) "Editar registro" else when(activeDialogType) {
                    ActivityType.FEEDING -> "¿Cuánto tomó?"
                    ActivityType.POOP -> "¿A qué hora hizo caca?"
                    ActivityType.BATH -> "¿A qué hora fue el baño?"
                    null -> ""
                }
                Text(title, fontWeight = FontWeight.Bold) 
            },
            text = {
                Column {
                    if (activeDialogType == ActivityType.FEEDING) {
                        Text(text = "Introduce los mililitros:")
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { amountText = it },
                            label = { Text("Cantidad (ml)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                    
                    Text(text = "Hora del evento:", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = isNow,
                            onClick = { isNow = true },
                            label = { Text("Ahora") },
                            leadingIcon = { 
                                if (isNow) Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp)) 
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        FilterChip(
                            selected = !isNow,
                            onClick = { 
                                selectedCalendar = Calendar.getInstance()
                                showTimePicker = true 
                            },
                            label = { 
                                val timeText = if (isNow) "Elegir hora" else {
                                    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                                    sdf.format(selectedCalendar.time)
                                }
                                Text(timeText) 
                            },
                            leadingIcon = { 
                                Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(18.dp)) 
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (editingActivity != null) {
                        TextButton(
                            onClick = {
                                viewModel.deleteActivity(editingActivity!!.id)
                                editingActivity = null
                                activeDialogType = null
                                amountText = ""
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Borrar")
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    
                    Button(
                        shape = RoundedCornerShape(12.dp),
                        onClick = {
                            val amount = if (activeDialogType == ActivityType.FEEDING) (amountText.toIntOrNull() ?: 0) else 0
                            
                            val timestamp = if (isNow) {
                                if (pagerState.currentPage == 499) System.currentTimeMillis()
                                else {
                                    val cal = Calendar.getInstance()
                                    val now = Calendar.getInstance()
                                    cal.add(Calendar.DAY_OF_YEAR, pagerState.currentPage - 499)
                                    cal.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY))
                                    cal.set(Calendar.MINUTE, now.get(Calendar.MINUTE))
                                    cal.timeInMillis
                                }
                            } else {
                                val cal = Calendar.getInstance()
                                cal.add(Calendar.DAY_OF_YEAR, pagerState.currentPage - 499)
                                cal.set(Calendar.HOUR_OF_DAY, selectedCalendar.get(Calendar.HOUR_OF_DAY))
                                cal.set(Calendar.MINUTE, selectedCalendar.get(Calendar.MINUTE))
                                cal.set(Calendar.SECOND, 0)
                                cal.timeInMillis
                            }
                            
                            if (activeDialogType != ActivityType.FEEDING || amount > 0) {
                                if (editingActivity != null) {
                                    viewModel.updateActivity(editingActivity!!.id, amount, timestamp)
                                } else {
                                    viewModel.addActivity(activeDialogType!!, amount, timestamp)
                                }
                                amountText = ""
                                isNow = true
                                selectedCalendar = Calendar.getInstance()
                                activeDialogType = null
                                editingActivity = null
                            }
                        }
                    ) {
                        Text("Guardar")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    activeDialogType = null 
                    editingActivity = null
                    amountText = ""
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedCalendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = selectedCalendar.get(Calendar.MINUTE)
        )

        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val newCal = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(Calendar.MINUTE, timePickerState.minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    selectedCalendar = newCal
                    isNow = false
                    showTimePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancelar") }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        title = { Text(text = "Selecciona la hora") },
        text = { content() }
    )
}

@Composable
fun MoonBackground() {
    val moonColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
    Canvas(modifier = Modifier.fillMaxSize()) {
        translate(left = size.width * 0.7f, top = size.height * 0.15f) {
            drawCircle(
                color = moonColor,
                radius = 100.dp.toPx()
            )
            // Efecto de luna creciente quitando un circulo
            drawCircle(
                color = Color(0xFFE1F5FE), // Color del fondo para "tapar"
                radius = 80.dp.toPx(),
                center = Offset(-20.dp.toPx(), -10.dp.toPx())
            )
        }
    }
}

@Composable
fun FeedingSummary(activities: List<Feeding>) {
    val totalMl = activities.filter { it.type == ActivityType.FEEDING }.sumOf { it.amountMl }
    val poopCount = activities.count { it.type == ActivityType.POOP }
    val isBathed = activities.any { it.type == ActivityType.BATH }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f))
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Hoy ya lleva...", style = MaterialTheme.typography.labelLarge)
                Text(
                    text = "$totalMl ml",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Contador de Cacas
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "💩", fontSize = 24.sp)
                    Text(
                        text = "x$poopCount",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Indicador de Baño
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🛁", fontSize = 24.sp)
                    Text(
                        text = if (isBathed) "SÍ" else "NO",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isBathed) Color(0xFF0288D1) else Color.Gray
                    )
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun FeedingItem(activity: Feeding, onLongClick: () -> Unit) {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    val date = sdf.format(Date(activity.timestamp))
    
    val (title, icon, color) = when(activity.type) {
        ActivityType.FEEDING -> Triple("${activity.amountMl} ml", "🍼", MaterialTheme.colorScheme.primary)
        ActivityType.POOP -> Triple("Caca", "💩", Color(0xFF795548))
        ActivityType.BATH -> Triple("Baño", "🛁", Color(0xFF0288D1))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { /* Opcional: acción en click normal */ },
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = "A las $date",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }
            Text(text = icon, fontSize = 24.sp)
        }
    }
}

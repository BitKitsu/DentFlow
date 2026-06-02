package com.example.dentflow_android.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.dentflow_android.data.ViewModel.StaffViewModel
import com.example.dentflow_android.data.remote.AuthResponse
import com.example.dentflow_android.data.remote.StaffMemberResponse
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStaffDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, String, String, Boolean, Long?, String?) -> Unit,
    onCheckEmail: suspend (String) -> AuthResponse?
) {
    var email by remember { mutableStateOf("") }
    var emailChecked by remember { mutableStateOf(false) }
    var userExists by remember { mutableStateOf(false) }
    var existingUserId by remember { mutableStateOf<Long?>(null) }
    var userAvatarUrl by remember { mutableStateOf<String?>(null) }
    var isCheckingEmail by remember { mutableStateOf(false) }
    
    var fName by remember { mutableStateOf("") }
    var lName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var prof by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    val isEmailValid = email.contains("@") && email.contains(".")
    val isPassValid = pass.length >= 8 || userExists
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dodaj Pracownika", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // Email check section
                Text("1. Sprawdź email pracownika", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { 
                            email = it
                            emailChecked = false
                        },
                        label = { Text("Email") },
                        modifier = Modifier.weight(1f),
                        isError = showError && !isEmailValid,
                        shape = RoundedCornerShape(12.dp),
                        enabled = !emailChecked
                    )
                    Button(
                        onClick = {
                            if (isEmailValid) {
                                isCheckingEmail = true
                                scope.launch {
                                    val userData = onCheckEmail(email)
                                    if (userData != null) {
                                        userExists = true
                                        existingUserId = userData.userId
                                        userAvatarUrl = userData.avatarUrl
                                        fName = userData.firstName ?: ""
                                        lName = userData.lastName ?: ""
                                        phone = userData.phone ?: ""
                                    } else {
                                        userExists = false
                                        existingUserId = null
                                        userAvatarUrl = null
                                        fName = ""
                                        lName = ""
                                        phone = ""
                                    }
                                    emailChecked = true
                                    isCheckingEmail = false
                                }
                            }
                        },
                        enabled = isEmailValid && !emailChecked && !isCheckingEmail,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        if (isCheckingEmail) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Sprawdź")
                        }
                    }
                }
                
                if (emailChecked) {
                    if (userExists) {
                        Text("✓ Użytkownik istnieje - dane zostaną automatycznie uzupełnione", 
                            color = MaterialTheme.colorScheme.primary, 
                            style = MaterialTheme.typography.bodySmall)
                    } else {
                        Text("⚠ Nowy użytkownik - zostanie utworzone konto", 
                            color = MaterialTheme.colorScheme.secondary, 
                            style = MaterialTheme.typography.bodySmall)
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Personal data section
                    Text("2. Dane osobowe", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = fName,
                            onValueChange = { fName = it },
                            label = { Text("Imię") },
                            modifier = Modifier.weight(1f),
                            isError = showError && fName.isBlank(),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !userExists
                        )
                        OutlinedTextField(
                            value = lName,
                            onValueChange = { lName = it },
                            label = { Text("Nazwisko") },
                            modifier = Modifier.weight(1f),
                            isError = showError && lName.isBlank(),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !userExists
                        )
                    }
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Telefon") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !userExists
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Work info section
                    Text("3. Dane zawodowe", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    OutlinedTextField(
                        value = prof,
                        onValueChange = { prof = it },
                        label = { Text("Profesja") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = showError && prof.isBlank(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Krótkie Bio / O mnie") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    if (!userExists) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text("4. Konto logowania", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                        OutlinedTextField(
                            value = pass,
                            onValueChange = { pass = it },
                            label = { Text("Hasło tymczasowe") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            isError = showError && !isPassValid,
                            shape = RoundedCornerShape(12.dp),
                            supportingText = {
                                if (showError && !isPassValid) {
                                    Text("Hasło musi mieć co najmniej 8 znaków", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (emailChecked && fName.isNotBlank() && lName.isNotBlank() && prof.isNotBlank() && isPassValid) {
                        onConfirm(fName, lName, prof, email, pass, phone, bio, userExists, existingUserId, userAvatarUrl)
                    } else { showError = true }
                },
                shape = RoundedCornerShape(12.dp),
                enabled = emailChecked
            ) { Text("DODAJ") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("ANULUJ") }
        }
    )
}

@Composable
fun EditStaffDialog(
    member: StaffMemberResponse,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var fName by remember { mutableStateOf(member.firstName) }
    var lName by remember { mutableStateOf(member.lastName) }
    var prof by remember { mutableStateOf(member.profession) }
    var bio by remember { mutableStateOf(member.bio ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edytuj dane", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = fName, onValueChange = { fName = it }, label = { Text("Imię") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = lName, onValueChange = { lName = it }, label = { Text("Nazwisko") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = prof, onValueChange = { prof = it }, label = { Text("Profesja") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = bio, onValueChange = { bio = it }, label = { Text("Krótkie Bio / O mnie") }, modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 4, shape = RoundedCornerShape(12.dp))
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(fName, lName, prof, bio) }, shape = RoundedCornerShape(12.dp)) { Text("ZAPISZ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("ANULUJ") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffManagementScreen(
    onBackClick: () -> Unit,
    viewModel: StaffViewModel = hiltViewModel()
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingMember by remember { mutableStateOf<StaffMemberResponse?>(null) }
    var selectedMember by remember { mutableStateOf<StaffMemberResponse?>(null) }

    val staffList by viewModel.staffMembers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadStaff()
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Zarządzanie Personelem", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, null) } },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.White)
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background)) {
            if (isLoading && staffList.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(staffList, key = { it.id }) { member ->
                        StaffItem(
                            member = member,
                            onEdit = { editingMember = member },
                            onDelete = { viewModel.deleteStaff(member.id) },
                            onClick = { selectedMember = member }
                        )
                    }
                }
            }

            if (showAddDialog) {
                AddStaffDialog(
                    onDismiss = { showAddDialog = false },
                    onConfirm = { fn, ln, pr, em, ps, ph, bo, exists, userId, avatarUrl ->
                        viewModel.addStaff(fn, ln, pr, em, ps, ph, bo, exists, userId, avatarUrl)
                        showAddDialog = false
                    },
                    onCheckEmail = { email ->
                        viewModel.checkUserByEmail(email)
                    }
                )
            }

            editingMember?.let { member ->
                EditStaffDialog(
                    member = member,
                    onDismiss = { editingMember = null },
                    onConfirm = { fn, ln, pr, bo ->
                        viewModel.updateStaff(member.id, fn, ln, pr, member.userId, bo)
                        editingMember = null
                    }
                )
            }

            selectedMember?.let { member ->
                StaffDetailDialog(
                    member = member,
                    onDismiss = { selectedMember = null }
                )
            }
        }
    }
}

@Composable
fun StaffItem(
    member: StaffMemberResponse,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (!member.avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = member.avatarUrl,
                        contentDescription = "${member.firstName} avatar",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = member.firstName.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${member.firstName} ${member.lastName}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = member.profession,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Edytuj") },
                        onClick = { showMenu = false; onEdit() },
                        leadingIcon = { Icon(Icons.Default.Edit, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Usuń z gabinetu", color = Color.Red) },
                        onClick = { showMenu = false; onDelete() },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffDetailDialog(
    member: StaffMemberResponse,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Profil pracownika", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (!member.avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = member.avatarUrl,
                            contentDescription = "${member.firstName} avatar",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = member.firstName.take(1).uppercase(),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "${member.firstName} ${member.lastName}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = member.profession,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                // Info rows
                if (!member.phone.isNullOrBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Phone, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(member.phone, style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (!member.email.isNullOrBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Email, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(member.email, style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (!member.bio.isNullOrBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(member.bio, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Zamknij") }
        }
    )
}

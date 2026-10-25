package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.*
import com.example.ui.viewmodel.SwapItViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateListingScreen(
    viewModel: SwapItViewModel,
    onBack: () -> Unit,
    onListingCreated: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories by viewModel.categories.collectAsState()
    val currentUserProfile by viewModel.currentUserProfile.collectAsState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf(categories.firstOrNull()?.id ?: "cat_phones") }
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("Like New") }
    var estimatedValueInput by remember { mutableStateOf("") }
    var isNegotiable by remember { mutableStateOf(true) }

    var state by remember { mutableStateOf(currentUserProfile?.state ?: "Lagos") }
    var city by remember { mutableStateOf(currentUserProfile?.city ?: "Ikeja") }
    var lga by remember { mutableStateOf(currentUserProfile?.lga ?: "Ikeja") }

    var acceptsCashTopup by remember { mutableStateOf(true) }
    var willingToAddCash by remember { mutableStateOf(true) }
    var willingToReceiveCash by remember { mutableStateOf(true) }
    var itemForItemOnly by remember { mutableStateOf(false) }

    var preferredCategories by remember { mutableStateOf("Phones & Tablets, Computers") }
    var preferredItems by remember { mutableStateOf("") }
    var handoverMethod by remember { mutableStateOf("In-person Public Meetup") }
    var handoverAvailability by remember { mutableStateOf("Weekends & Evenings") }

    // Preset high quality photo options
    val samplePhotos = listOf(
        "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=600",
        "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=600",
        "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600",
        "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600",
        "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600",
        "https://images.unsplash.com/photo-1583394838336-acd977736f90?w=600"
    )
    var selectedPhotoUrl by remember { mutableStateOf(samplePhotos[0]) }

    val conditionsList = listOf("Brand New", "Like New", "Excellent", "Good", "Fair", "For parts or repair")
    val nigerianStates = listOf("Lagos", "Abuja FCT", "Oyo", "Rivers", "Ogun", "Kano", "Enugu", "Delta")

    var isSubmitting by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("List an Item for Swap", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                viewModel.showMessage("Please enter an item title.")
                                return@Button
                            }
                            val estVal = estimatedValueInput.toLongOrNull() ?: 0L
                            if (estVal <= 0) {
                                viewModel.showMessage("Please enter an estimated value in Naira.")
                                return@Button
                            }

                            isSubmitting = true
                            viewModel.createListing(
                                title = title,
                                description = description.ifBlank { "Clean item available for smart swap. Open to good exchange offers." },
                                categoryId = selectedCategoryId,
                                brand = brand.ifBlank { "Generic" },
                                model = model.ifBlank { "Standard" },
                                condition = condition,
                                estimatedValueNaira = estVal,
                                isValueNegotiable = isNegotiable,
                                state = state,
                                city = city,
                                lga = lga,
                                acceptsCashTopup = acceptsCashTopup,
                                willingToAddCash = willingToAddCash,
                                willingToReceiveCash = willingToReceiveCash,
                                itemForItemOnly = itemForItemOnly,
                                preferredCategories = preferredCategories,
                                preferredItems = preferredItems.ifBlank { "Open to trades in electronics, gadgets, or shoes." },
                                handoverMethod = handoverMethod,
                                handoverAvailability = handoverAvailability,
                                primaryImageUrl = selectedPhotoUrl,
                                onSuccess = { newId ->
                                    isSubmitting = false
                                    onListingCreated(newId)
                                }
                            )
                        },
                        enabled = !isSubmitting,
                        colors = ButtonDefaults.buttonColors(containerColor = SwapItGreenPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("publish_listing_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Icon(Icons.Filled.Publish, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Publish Listing & Start Swapping", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Photo Selection Section
            Text("1. Item Photograph (Required)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = selectedPhotoUrl,
                    contentDescription = "Selected photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Primary Photo Selected",
                        color = Color.White,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text("Select from item photo gallery:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(samplePhotos) { photoUrl ->
                    val isPicked = photoUrl == selectedPhotoUrl
                    Surface(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedPhotoUrl = photoUrl }
                            .border(
                                width = if (isPicked) 2.5.dp else 1.dp,
                                color = if (isPicked) SwapItGreenPrimary else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            HorizontalDivider()

            // Basic Info
            Text("2. Basic Information", fontWeight = FontWeight.Bold, fontSize = 14.sp)

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Item Title *") },
                placeholder = { Text("e.g. iPhone 13 Pro 128GB Sierra Blue") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_title_input"),
                singleLine = true
            )

            // Category Picker
            Text("Category *", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategoryId == cat.id,
                        onClick = { selectedCategoryId = cat.id },
                        label = { Text(cat.name, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SwapItGreenPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Brand") },
                    placeholder = { Text("Apple, Nike, Sony...") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("Model") },
                    placeholder = { Text("A2638, PS5...") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // Condition Dropdown
            Text("Condition *", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(conditionsList) { cond ->
                    FilterChip(
                        selected = condition == cond,
                        onClick = { condition = cond },
                        label = { Text(cond, fontSize = 12.sp) }
                    )
                }
            }

            // Estimated Value in Naira
            OutlinedTextField(
                value = estimatedValueInput,
                onValueChange = { if (it.all { ch -> ch.isDigit() }) estimatedValueInput = it },
                label = { Text("Estimated Current Value in Naira (₦) *") },
                placeholder = { Text("e.g. 250000") },
                leadingIcon = {
                    Text("₦", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = SwapItGreenPrimary, modifier = Modifier.padding(start = 12.dp))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_price_input"),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Is this value negotiable in offers?", fontSize = 13.sp)
                Switch(
                    checked = isNegotiable,
                    onCheckedChange = { isNegotiable = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = SwapItGreenPrimary, checkedTrackColor = SwapItGreenContainer)
                )
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description & Condition Notes") },
                placeholder = { Text("Include battery health, accessories, cosmetic condition...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                maxLines = 4
            )

            HorizontalDivider()

            // Location
            Text("3. Location", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("Nigerian State *", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(nigerianStates) { st ->
                    FilterChip(
                        selected = state == st,
                        onClick = { state = st },
                        label = { Text(st, fontSize = 12.sp) }
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("City / Town *") },
                    placeholder = { Text("e.g. Ikeja, Garki, Bodija") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = lga,
                    onValueChange = { lga = it },
                    label = { Text("LGA") },
                    placeholder = { Text("e.g. Ikeja, Municipal") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            HorizontalDivider()

            // What the owner wants
            Text("4. What You Want in Exchange", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            OutlinedTextField(
                value = preferredItems,
                onValueChange = { preferredItems = it },
                label = { Text("Specific Items Desired") },
                placeholder = { Text("e.g. PS5, iPad Air M1, Air Jordan 1...") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Open to Cash Top-Up (adding/receiving cash)?", fontSize = 13.sp)
                Switch(
                    checked = acceptsCashTopup,
                    onCheckedChange = { acceptsCashTopup = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = SwapItGreenPrimary, checkedTrackColor = SwapItGreenContainer)
                )
            }

            HorizontalDivider()

            // Handover Preferences
            Text("5. Handover Preferences", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            OutlinedTextField(
                value = handoverMethod,
                onValueChange = { handoverMethod = it },
                label = { Text("Handover Method") },
                placeholder = { Text("e.g. In-person Public Meetup at Mall") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = handoverAvailability,
                onValueChange = { handoverAvailability = it },
                label = { Text("General Availability") },
                placeholder = { Text("e.g. Saturdays 12-4pm, Weekday evenings") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
